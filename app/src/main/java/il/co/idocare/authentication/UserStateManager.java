package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import java.util.ArrayList;
import java.util.Arrays;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.networking.ServerHttpRequest;
import il.co.idocare.networking.responsehandlers.NativeLoginResponseHandler;
import il.co.idocare.networking.responsehandlers.NativeSignupResponseHandler;
import il.co.idocare.networking.responsehandlers.ServerHttpResponseHandler;
import il.co.idocare.utils.IDoCareJSONUtils;

/**
 * This class manages the login state of the user - it aggregates information from all login
 * mechanisms supported by the application (native, Facebook, etc.)
 */
public class UserStateManager {

    public static final String KEY_ERROR_MSG = "il.co.idocare.authentication." +
            UserStateManager.class.getSimpleName() + ".KEY_ERROR_MSG";

    private static final String LOG_TAG = UserStateManager.class.getSimpleName();



    private Context mContext;

    private AccountManager mAccountManager;

    public UserStateManager(Context context) {
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
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;

    }

    public boolean isLoggedInNative() {
        Account[] accounts =
                mAccountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);

        if (accounts.length == 0) return false;

        if (accounts.length > 1) {
            Log.e(LOG_TAG, "There is more than one native account on the device. " +
                    "Using the first one returned." +
                    "\nTotal native accounts: " + String.valueOf(accounts.length));
        }
        return true;
    }

    /**
     * Try to perform a native log in with the provided credentials. The result info is
     * encapsulated in the returned Bundle object.<br>
     * NOTE: this method mustn't be called from UI thread!
     * @param username
     * @param password
     * @return a Bundle having at least the following fields in case of successful login:<br>
     *     {@link AccountManager#KEY_ACCOUNT_NAME}<br>
     *     {@link AccountManager#KEY_AUTHTOKEN}<br>
     *     or, in case login attempt failed:<br>
     *     {@link UserStateManager#KEY_ERROR_MSG}
     */
    public Bundle logInNative(String username, String password) {

        Bundle loginResult = new Bundle();

        // Encode username and password
        byte[] usernameBytes;
        byte[] passwordBytes;
        try {
            usernameBytes = ("fuckyouhackers" + username).getBytes("UTF-8");
            passwordBytes = ("fuckyouhackers" + password).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e ) {
            // Really? Not supporting UTF-8???
            e.printStackTrace();
            loginResult.putString(KEY_ERROR_MSG, e.getMessage());
            return loginResult;
        }

        ServerHttpRequest request = new ServerHttpRequest(Constants.LOG_IN_NATIVE_URL);

        // Add encoded header
        request.addHeader(Constants.HttpHeader.USER_USERNAME.getValue(),
                Base64.encodeToString(usernameBytes, Base64.NO_WRAP));

        // Add encoded parameter
        request.addTextField(Constants.FIELD_NAME_USER_PASSWORD_LOGIN,
                Base64.encodeToString(passwordBytes, Base64.NO_WRAP));

        CloseableHttpResponse response = request.execute();

        if (response == null) {
            loginResult.putString(KEY_ERROR_MSG, "could not obtain response to login request");
            return loginResult;
        }

        // Parse the response
        loginResult = UserStateManager.handleResponse(response, new NativeLoginResponseHandler());

        // Check for common errors
        UserStateManager.checkForCommonErrors(loginResult);
        if (loginResult.containsKey(KEY_ERROR_MSG))
            return loginResult;

        addNativeAccount(loginResult);
        if (loginResult.containsKey(KEY_ERROR_MSG))
            return loginResult;


        EventBus.getDefault().post(new UserLoggedInEvent());

        return loginResult;
    }


    /**
     * Call to this method will add a new account and set its auth token. Account's details are
     * obtained from the provided Bundle. If the required account already exists - call to
     * this method will only update its auth token.<br>
     * The result is written back into the provided Bundle.
     */
    public void addNativeAccount(Bundle result) {

        String accountName = result.getString(ServerHttpResponseHandler.KEY_USER_ID);
        String authToken = result.getString(ServerHttpResponseHandler.KEY_PUBLIC_KEY);

        if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(authToken))
            throw new IllegalArgumentException("account name and auth token must be non-empty");

        String accountType = AccountAuthenticator.ACCOUNT_TYPE_DEFAULT;

        Account account = new Account(accountName, accountType);
        mAccountManager.addAccountExplicitly(account, null, null);

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
                setNativeAccountAuthToken(accountName, accountType, authToken);
            } else {
                mAccountManager.removeAccountExplicitly(acc);
            }
        }

        // Put account's details into the bundle under "global" keys
        result.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
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

        // Try to log in - this will fail if a native account corresponding to this FB account
        // hasn't been created yet
        Bundle loginResult = logInNative(email, facebookId);
        if (loginResult.containsKey(KEY_ERROR_MSG)) {
            Log.v(LOG_TAG, "native login into FB shadowed account failed. Error message: " +
                    loginResult.getString(KEY_ERROR_MSG));
            // Login failed therefore we need to try to create a new native account for this FB user
            Bundle signupResult =
                    signUpNative(email, password, nickname, firstName, lastName, facebookId);
            if (signupResult.containsKey(KEY_ERROR_MSG)) {
                Log.v(LOG_TAG, "native signup for FB shadowed account failed. Error message: " +
                        signupResult.getString(KEY_ERROR_MSG));
                // Both login and signup attempts failed - FB login flow failed
                return false;
            }
        }

        return true;
    }

    /**
     * Do not call this method from UI thread!
     * @return
     */
    public Bundle signUpNative(String email, String password, String nickname, String firstName,
                                String lastName, @Nullable String facebookId) {

        ServerHttpRequest request = new ServerHttpRequest(Constants.SIGN_UP_NATIVE_URL);

        request.addTextField(Constants.FIELD_NAME_USER_EMAIL, email);
        request.addTextField(Constants.FIELD_NAME_USER_PASSWORD_SIGNUP, password);
        request.addTextField(Constants.FIELD_NAME_USER_NICKNAME, nickname);
        request.addTextField(Constants.FIELD_NAME_USER_FIRST_NAME, firstName);
        request.addTextField(Constants.FIELD_NAME_USER_LAST_NAME, lastName);
        if (!TextUtils.isEmpty(facebookId))
            request.addTextField(Constants.FIELD_NAME_USER_FACEBOOK_ID, facebookId);


        CloseableHttpResponse response = request.execute();

        Bundle signupResult = new Bundle();

        if (response == null) {
            signupResult.putString(KEY_ERROR_MSG, "could not obtain response to signup request");
            return signupResult;
        }

        // Parse the response
        signupResult = UserStateManager.handleResponse(response, new NativeSignupResponseHandler());

        // Check for common errors
        UserStateManager.checkForCommonErrors(signupResult);
        if (signupResult.containsKey(KEY_ERROR_MSG))
            return signupResult;

        addNativeAccount(signupResult);
        if (signupResult.containsKey(KEY_ERROR_MSG))
            return signupResult;


        EventBus.getDefault().post(new UserLoggedInEvent());

        return signupResult;
    }

    /**
     * Log out currently logged in user. This method assumes that the user will be logged in using
     * only one mechanism - either native, or Facebook, or...
     */
    public void logOut() {
        boolean loggedOut = false;
        if (isLoggedInWithFacebook()) {
            logOutFacebook();
            // Need to logout native because FB login also creates native acc on device (workaround)
            // TODO: remove this statement once proper FB login implemented
            logOutNative();
            loggedOut = true;
        } else if (isLoggedInNative()) {
            logOutNative();
        }

        if (loggedOut) {
            EventBus.getDefault().post(new UserLoggedOutEvent());
        }
    }

    private void logOutNative() {
        // TODO: write code that removes native account
    }

    private void logOutFacebook() {
        LoginManager.getInstance().logOut();
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
     * @param response
     * @param responseHandler
     * @return
     */
    private static Bundle handleResponse(CloseableHttpResponse response,
                                         ServerHttpResponseHandler responseHandler) {
        Bundle result = new Bundle();

        try {
            result = responseHandler.handleResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            result.putString(KEY_ERROR_MSG, "could not handle response to login request");
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
     * @param result
     */
    private static void checkForCommonErrors(Bundle result) {
        if (result.containsKey(KEY_ERROR_MSG)) {
            // the result already contains error message - nothing to do
        }
        else if (!result.containsKey(ServerHttpResponseHandler.KEY_RESPONSE_STATUS_OK)) {
            result.putString(KEY_ERROR_MSG, "unsuccessful HTTP response code: "
                    + result.getInt(ServerHttpResponseHandler.KEY_RESPONSE_STATUS_CODE));
        }
        else if (!result.containsKey(ServerHttpResponseHandler.KEY_INTERNAL_STATUS_SUCCESS)) {
            result.putString(KEY_ERROR_MSG, "unsuccessful internal status");
        }
        else if (result.containsKey(ServerHttpResponseHandler.KEY_ERROR_TYPE)) {
            result.putString(KEY_ERROR_MSG,
                    result.getString(ServerHttpResponseHandler.KEY_ERROR_TYPE));
        }
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
