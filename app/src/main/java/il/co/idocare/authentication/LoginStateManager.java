package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.URLs;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.sequences.LoginNativeSequence;
import il.co.idocare.networking.responseparsers.ResponseParserUtils;
import il.co.idocare.networking.ServerHttpRequest;
import il.co.idocare.networking.responseparsers.HttpResponseParseException;
import il.co.idocare.networking.responseparsers.ServerHttpResponseParser;
import il.co.idocare.networking.responseparsers.ServerResponseParsersFactory;
import il.co.idocare.sequences.Sequence;

/**
 * This class manages the login state of the user - it aggregates information from all login
 * mechanisms supported by the application (native, Facebook, etc.)
 */
public class LoginStateManager {

    public static final String KEY_ERROR_MSG = "il.co.idocare.authentication." +
            LoginStateManager.class.getSimpleName() + ".KEY_ERROR_MSG";



    private static final String LOG_TAG = LoginStateManager.class.getSimpleName();


    private Context mContext;

    private AccountManager mAccountManager;

    public LoginStateManager(Context context) {
        if (context == null)
            throw new IllegalArgumentException("valid context must be supplied!");
        mContext = context;
        mAccountManager = AccountManager.get(mContext);
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
     * Call to this method will add a new account and set its auth token. Account's details are
     * obtained from the provided Bundle. If the required account already exists - call to
     * this method will only update its auth token.<br>
     * The result is written back into the provided Bundle.
     */
    public void addNativeAccount(Bundle result) {

        String username = result.getString(ServerHttpResponseParser.KEY_USERNAME);
        String userId = result.getString(ServerHttpResponseParser.KEY_USER_ID);
        String authToken = result.getString(ServerHttpResponseParser.KEY_PUBLIC_KEY);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(userId) || TextUtils.isEmpty(authToken))
            throw new IllegalArgumentException("account name, user ID and auth token must be non-empty");

        String accountType = AccountAuthenticator.ACCOUNT_TYPE_DEFAULT;

        final Account account = new Account(username, accountType);
        Bundle userdata = new Bundle(1);
        userdata.putString(Constants.FIELD_NAME_USER_ID, userId);
        mAccountManager.addAccountExplicitly(account, null, userdata);

        Account[] existingAccounts =
                mAccountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);

        /*
         The below code both checks whether the required account exists and removes all other
         accounts, thus ensuring existence of a single account on the device...
         TODO: reconsider single account approach and this particular implementation
          */
        boolean addedSuccessfully = Arrays.asList(existingAccounts).contains(account);

        // If the required account wasn't added - set error flag and return
        if (!addedSuccessfully) {
            result.putString(KEY_ERROR_MSG, "failed to add native account");
            return;
        }

        // The required account exists - update its authToken and remove all other accounts
        for (Account acc : existingAccounts) {
            if (acc.equals(account)) {
                setNativeAccountAuthToken(username, accountType, authToken);
            } else {
                final Account finalAccount = acc;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mAccountManager.removeAccount(finalAccount, null, null);
                    }
                }).start();
            }
        }

        // Put account's details into the bundle under "global" keys
        result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
        result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
    }

    public void setNativeAccountAuthToken(String accountName, String accountType,
                                          String authToken) {
        Account account = new Account(accountName, accountType);
        mAccountManager.setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT, authToken);
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

        Log.v(LOG_TAG, "Contents of facebook/me response: " + response.getRawResponse());

        if (response.getError() != null) {
            Log.e(LOG_TAG, "facebook/me returned error response: " +
                    response.getError().getErrorMessage());
            // TODO: facebook errors should be processed - there is a way to recover from some of them
            return false;
        }

        JSONObject jsonResponse = response.getJSONObject();
        if (jsonResponse == null) {
            Log.e(LOG_TAG, "couldn't obtain JSON object from FB response");
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
//            Log.v(LOG_TAG, "native login into FB shadowed account failed. Error message: " +
//                    loginResult.getString(KEY_ERROR_MSG));
//            // Login failed therefore we need to try to create a new native account for this FB user
//            // TODO: this account should have a picture
//            Bundle signupResult =
//                    signUpNative(email, password, nickname, firstName, lastName, facebookId, null);
//            if (signupResult.containsKey(KEY_ERROR_MSG)) {
//                Log.v(LOG_TAG, "native signup for FB shadowed account failed. Error message: " +
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
     * Perform native signup<br>
     * Do not call this method from UI thread!
     */
    public Bundle signUpNative(String email, String password, String nickname, String firstName,
                                String lastName, @Nullable String facebookId,
                                @Nullable String userPicturePath) {

        Bundle signupResult = new Bundle();

        ServerHttpRequest request = new ServerHttpRequest(URLs.getUrl(URLs.RESOURCE_SIGNUP));

        byte[] emailBytes = toBytes(email);
        byte[] passwordBytes = toBytes(password);
        if (emailBytes == null || passwordBytes == null) {
            signupResult.putString(KEY_ERROR_MSG, "encoding error");
            return signupResult;
        }

        request.addTextField(Constants.FIELD_NAME_USER_EMAIL, Base64.encodeToString(emailBytes, Base64.NO_WRAP));
        request.addTextField(Constants.FIELD_NAME_USER_PASSWORD_SIGNUP, Base64.encodeToString(passwordBytes, Base64.NO_WRAP));
        request.addTextField(Constants.FIELD_NAME_USER_NICKNAME, nickname);
        request.addTextField(Constants.FIELD_NAME_USER_FIRST_NAME, firstName);
        request.addTextField(Constants.FIELD_NAME_USER_LAST_NAME, lastName);
        if (!TextUtils.isEmpty(facebookId))
            request.addTextField(Constants.FIELD_NAME_USER_FACEBOOK_ID, facebookId);

        if (!TextUtils.isEmpty(userPicturePath))
        request.addPictureField(Constants.FIELD_NAME_USER_PICTURE,
                "userPicture", userPicturePath);


        CloseableHttpResponse response = request.execute(HttpClientBuilder.create().build());


        if (response == null) {
            signupResult.putString(KEY_ERROR_MSG, "could not obtain response to signup request");
            return signupResult;
        }

        // Parse the response
        signupResult = LoginStateManager.handleResponse(response,
                ServerResponseParsersFactory.newInstance(URLs.RESOURCE_SIGNUP));

        // Check for common errors
        LoginStateManager.checkForCommonErrors(signupResult);
        if (signupResult.containsKey(KEY_ERROR_MSG))
            return signupResult;


        // Account name should be added manually because the response does not contain this data
        signupResult.putString(ServerHttpResponseParser.KEY_USERNAME, email);

        addNativeAccount(signupResult);
        if (signupResult.containsKey(KEY_ERROR_MSG))
            return signupResult;


        EventBus.getDefault().post(new UserLoggedInEvent());

        return signupResult;
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
                    Log.e(LOG_TAG, "logout process failed");
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
     * Use the provided response handle in order to parse the provided response. The resulting
     * Bundle contains all the relevant information - parsed details and error indicators (if
     * there were any errors)
     * TODO: get this method out of this class (maybe inside of Flows?)
     * @param response
     * @param responseParser
     * @return
     */
    public static Bundle handleResponse(CloseableHttpResponse response,
                                         ServerHttpResponseParser responseParser) {
        Bundle result = new Bundle();

        try {
            result = responseParser.parseResponse(response);
        } catch (HttpResponseParseException e) {
            e.printStackTrace();
            result.putString(KEY_ERROR_MSG, "could not parse response to login request");
            return result;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Check the provided Bundle for existence of common error keys and set KEY_ERROR_MESSAGE
     * field if there are any
     * TODO: remove this method from here (maybe in Flows?)
     * @param result
     */
    public static void checkForCommonErrors(Bundle result) {
        if (result.containsKey(KEY_ERROR_MSG)) {
            // the result already contains error message - nothing to do
        }
        else if (!result.containsKey(ServerHttpResponseParser.KEY_RESPONSE_STATUS_OK)) {
            result.putString(KEY_ERROR_MSG, "unsuccessful HTTP response code: "
                    + result.getInt(ServerHttpResponseParser.KEY_RESPONSE_STATUS_CODE));
        }
        else if (!result.containsKey(ServerHttpResponseParser.KEY_INTERNAL_STATUS_SUCCESS)) {
            result.putString(KEY_ERROR_MSG, "unsuccessful internal status");
        }
        else if (result.containsKey(ServerHttpResponseParser.KEY_ERRORS)) {
            result.putString(KEY_ERROR_MSG,
                    ResponseParserUtils.extractErrorsToString(result));
        }
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
            Log.e(LOG_TAG, "There is more than one native account on the device. " +
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


    /**
     *
     * @return auth token associated with the active account, or null if there is no
     *         native account registered on the device or there is no auth token for an active
     *         account
     */
    public String getActiveAccountAuthToken() {
        Account account = getActiveAccount();

        if (account != null)
            return mAccountManager.peekAuthToken(account, AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT);
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


    // TODO: remove this method from here
    private byte[] toBytes(String plainText) {
        byte[] encodedText;
        try {
            encodedText = ("fuckyouhackers" + plainText).getBytes("UTF-8");
            return encodedText;
        } catch (UnsupportedEncodingException e ) {
            // Really? Not supporting UTF-8???
            e.printStackTrace();
            return null;
        }
    }
}
