package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import il.co.idocare.Constants;
import il.co.idocare.datamodels.pojos.UserSignupNativeData;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.sequences.LoginFacebookSequence;
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
    private Logger mLogger;
    private AccountManager mAccountManager;
    private MyAccountManager mMyAccountManager;

    public LoginStateManager(Context context, AccountManager accountManager /*TODO: need to be removed*/,
                             MyAccountManager myAccountManager, Logger logger) {
        mContext = context;
        mLogger = logger;
        mAccountManager = accountManager;
        mMyAccountManager = myAccountManager;
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

        Account activeAccount = mMyAccountManager.getActiveAccount();

        if (activeAccount == null || activeAccount.equals(mMyAccountManager.getDummyAccount())) {
            // There is a shitty scenario when there is no native account, but the user is
            // logged in with facebook account - we need to account for this by logging out of FB.
            // The optimal solution would be async request to logInFacebook(), but this will
            // require too major code refactoring
            // TODO: remove this shitty code once proper FB login flow established
            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
            }
            return false;
        } else {
            return isFacebookAccount(activeAccount);
        }
    }

    public boolean isLoggedInNative() {
        Account activeAccount = mMyAccountManager.getActiveAccount();

        if (activeAccount == null || activeAccount.equals(mMyAccountManager.getDummyAccount())) {
            return false;
        } else {
            return !isFacebookAccount(activeAccount);
        }
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
                new LoginNativeSequence(username, password, mMyAccountManager);

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
     * TODO: write javadoc
     */
    public void logInFacebook(AccessToken accessToken) {

        final LoginFacebookSequence loginFacebookSequence =
                new LoginFacebookSequence(accessToken, mMyAccountManager, mLogger);

        loginFacebookSequence.registerStateChangeListener(new Sequence.StateChangeListener() {
            @Override
            public void onSequenceStateChanged(int newState) {
                if (newState == Sequence.STATE_EXECUTED_SUCCEEDED) {
                    String username = loginFacebookSequence.getSequenceResult().getUsername();
                    String authToken = loginFacebookSequence.getSequenceResult().getAuthToken();
                    EventBus.getDefault()
                            .post(new LoginStateEvents.LoginSucceededEvent(username, authToken));
                } else if (newState == Sequence.STATE_EXECUTED_FAILED) {
                    EventBus.getDefault().post(new LoginStateEvents.LoginFailedEvent());
                }
            }
        });
        loginFacebookSequence.executeInBackground();

    }

    private boolean isFacebookAccount(Account account) {
        return  mAccountManager.getUserData(account, Constants.FIELD_NAME_USER_FACEBOOK_ID) != null;
    }



    /**
     * Log out the active user
     * TODO: this method should be rewritten once proper FB authentication implemented
     */
    public void logOut() {
        final Account account = mMyAccountManager.getActiveAccount();

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
     * @return user ID string associated with the active account, or null if there is no
     *         user registered
     */
    public String getActiveAccountUserId() {
        Account account = mMyAccountManager.getActiveAccount();

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
