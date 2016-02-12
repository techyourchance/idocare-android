package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.datamodels.pojos.UserSignupNativeData;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.sequences.LoginNativeSequence;
import il.co.idocare.sequences.Sequence;
import il.co.idocare.sequences.SignupNativeSequence;
import il.co.idocare.utils.Logger;

/**
 * This class manages the login state of the user - it aggregates information from all login
 * mechanisms supported by the application (native, Facebook, etc.)
 */
public class LoginStateManager {

    private static final String TAG = "LoginStateManager";


    private Context mContext;

    private AccountManager mAccountManager;
    private MyAccountManager mMyAccountManager;

    public LoginStateManager(Context context, AccountManager accountManager /*TODO: need to be removed*/) {
        mContext = context;
        mAccountManager = accountManager;
        // TODO: need to be injected
        mMyAccountManager = new MyAccountManager(accountManager, new Logger());
    }

    /**
     * Check whether there is a currently logged in user.
     * @return true if the user is logged in by ANY mean (Facebook, native, etc.)
     */
    public boolean isLoggedIn() {
        return isLoggedInWithFacebook() || isLoggedInNative();
    }


    public boolean isLoggedInWithFacebook() {
        /*
        accessToken in this situation can be null even when there is a logged in user - this can
        happen if getCurrentAccessToken() is called when onCurrentAccessTokenChanged() is in
        progress.
        Additional info:
        http://stackoverflow.com/questions/29294015/how-to-check-if-user-is-logged-in-with-fb-sdk-4-0-for-android
        http://stackoverflow.com/questions/30379616/how-to-get-current-facebook-access-token-on-app-start

        TODO: RESOLVE THIS BUG!!!!!!!!
         */
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        return accessToken != null;

        // TODO: This is a temporary workaround which should be removed also
        if (!isLoggedInNative() && AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
        return false;

    }

    public boolean isLoggedInNative() {
        return getActiveAccount() != null;
    }


    /**
     * Perform native signup
     */
    public void signUpNative(UserSignupNativeData userData) {
        final SignupNativeSequence signupNativeSequence =
                new SignupNativeSequence(userData, mMyAccountManager);

        signupNativeSequence.registerStateChangeListener(new Sequence.StateChangeListener() {
            @Override
            public void onSequenceStateChanged(int newState) {
                if (newState == Sequence.STATE_EXECUTED_SUCCEEDED) {
                    String username = signupNativeSequence.getSequenceResult().getUsername();
                    String authToken = signupNativeSequence.getSequenceResult().getAuthToken();
                    EventBus.getDefault()
                            .post(new LoginStateEvents.LoginSucceededEvent(username, authToken));
                } else if (newState == Sequence.STATE_EXECUTED_FAILED) {
                    EventBus.getDefault().post(new LoginStateEvents.LoginFailedEvent());
                }
            }
        });
        signupNativeSequence.executeInBackground();
    }

    /**
     * Attempt to perform a native log in with the provided credentials.
     * @param username
     * @param password
     */
    public void logInNative(String username, String password) {

        final LoginNativeSequence loginNativeSequence =
                new LoginNativeSequence(username, password, mAccountManager);

        loginNativeSequence.registerStateChangeListener(new Sequence.StateChangeListener() {
            @Override
            public void onSequenceStateChanged(int newState) {
                if (newState == Sequence.STATE_EXECUTED_SUCCEEDED) {
                    String username = loginNativeSequence.getSequenceResult().getUsername();
                    String authToken = loginNativeSequence.getSequenceResult().getAuthToken();
                    EventBus.getDefault()
                            .post(new LoginStateEvents.LoginSucceededEvent(username, authToken));
                } else if (newState == Sequence.STATE_EXECUTED_FAILED) {
                    EventBus.getDefault().post(new LoginStateEvents.LoginFailedEvent());
                }
            }
        });
        loginNativeSequence.executeInBackground();
    }



    /**
     * This method should be called after successful FB login. Currently it creates a new
     * native Account with credentials built from user's details as obtained from facebook.<br>
     * NOTE: this method mustn't be called from main thread
     * TODO: refactor/remove this method once correct FB login flow implemented
     * @param accessToken
     * @return true if FB account was set up successfully, false otherwise
     */
    public boolean addFacebookAccount(AccessToken accessToken) {

        // Construct a request to fetch user's details
        GraphRequest request = GraphRequest.newMeRequest(accessToken, null);
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email");
        request.setParameters(parameters);

        GraphResponse response = GraphRequest.executeAndWait(request);

        Log.v(TAG, "Contents of facebook/me response: " + response.getRawResponse());

        if (response.getError() != null) {
            Log.e(TAG, "facebook/me returned error response: " +
                    response.getError().getErrorMessage());
            // TODO: facebook errors should be processed - there is a way to recover from some of them
            return false;
        }

        JSONObject jsonResponse = response.getJSONObject();
        if (jsonResponse == null) {
            Log.e(TAG, "couldn't obtain JSON object from FB response");
            return false;
        }

        String facebookId;
        String email;
        String password;
        String firstName;
        String lastName;
        String nickname;

        try {
            facebookId = jsonResponse.getString("id");
            email = jsonResponse.getString("email");
            password = facebookId;
            firstName = jsonResponse.getString("first_name");
            lastName = jsonResponse.getString("last_name");
            nickname = firstName + " " + lastName;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        // TODO: refactor FB login into separate flow

//        // Try to log in - this will fail if a native account corresponding to this FB account
//        // hasn't been created yet
//        Bundle loginResult = logInNative(email, facebookId);
//        if (loginResult.containsKey(KEY_ERROR_MSG)) {
//            Log.v(TAG, "native login into FB shadowed account failed. Error message: " +
//                    loginResult.getString(KEY_ERROR_MSG));
//            // Login failed therefore we need to try to create a new native account for this FB user
//            // TODO: this account should have a picture
//            Bundle signupResult =
//                    signUpNative(email, password, nickname, firstName, lastName, facebookId, null);
//            if (signupResult.containsKey(KEY_ERROR_MSG)) {
//                Log.v(TAG, "native signup for FB shadowed account failed. Error message: " +
//                        signupResult.getString(KEY_ERROR_MSG));
//                // Both login and signup attempts failed - FB login flow failed
//                return false;
//            }
//        }
//
//        // We need to designate the newly created native account as FB account
//        Account account = getActiveAccount();
//        mAccountManager.setUserData(account, Constants.FIELD_NAME_USER_FACEBOOK_ID, facebookId);

        return true;
    }


    /**
     * Log out the active user
     * TODO: this method should be rewritten once proper FB authentication implemented
     */
    public void logOut() {
        final Account account = getActiveAccount();

        if (account == null) return; // Not logged in user

        final boolean isFacebookAccount =
                mAccountManager.getUserData(account, Constants.FIELD_NAME_USER_FACEBOOK_ID) != null;

        new Thread(new Runnable() {
            @Override
            public void run() {
                AccountManagerFuture<Boolean> future = mAccountManager.removeAccount(account,
                        null, null);
                try {
                    boolean accountRemoved = future.getResult();
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                }

                if (isLoggedInNative()) {
                    Log.e(TAG, "logout process failed");
                    return;
                }

                if (isFacebookAccount) {
                    LoginManager.getInstance().logOut();
                }

                // Show login screen the next time the app starts
                setLoginSkipped(false);

                EventBus.getDefault().post(new UserLoggedOutEvent());

            }
        }).start();
    }



    /**
     * Set the value of "user chose to skip login" flag
     * @param loginSkipped true in order to set the flag, false to clear it
     */
    public void setLoginSkipped(boolean loginSkipped) {
        // Write to SharedPreferences an indicator of user willing to skip login
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        if (loginSkipped)
            prefs.edit().putInt(Constants.LOGIN_SKIPPED_KEY, 1).apply();
        else
            prefs.edit().remove(Constants.LOGIN_SKIPPED_KEY).apply();
    }


    public boolean isLoginSkipped() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        return prefs.contains(Constants.LOGIN_SKIPPED_KEY);
    }

    /**
     *
     * @return active user account, or null if there is no native account registered on the device
     */
    public Account getActiveAccount() {
        Account[] accounts =
                mAccountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);

        if (accounts.length == 0) {
            // There is a shitty scenario when there is no native account, but the user is
            // logged in with facebook account - we need to account for this by logging out of FB.
            // The optimal solution would be async request to addFacebookAccount(), but this will
            // require too major code refactoring
            // TODO: remove this shitty code once proper FB login flow established
            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
            }
            return null;
        }


        if (accounts.length > 1) {
            Log.e(TAG, "There is more than one native account on the device. " +
                    "Using the first one returned." +
                    "\nTotal native accounts: " + String.valueOf(accounts.length));
        }

        // Checking for dummy account
        if (accounts[0].name.equals(AccountAuthenticator.DUMMY_ACCOUNT_NAME) &&
                mAccountManager.getUserData(accounts[0], Constants.FIELD_NAME_USER_ID) == null)
            return null;
        else
            return accounts[0];
    }

    /**
     *
     * @return user ID string associated with the active account, or null if there is no
     *         native account registered on the device
     */
    public String getActiveAccountUserId() {
        Account account = getActiveAccount();

        if (account != null)
            return mAccountManager.getUserData(account, Constants.FIELD_NAME_USER_ID);
        else
            return null;

    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    public static class UserLoggedInEvent {}

    public static class UserLoggedOutEvent {}

    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------

}
