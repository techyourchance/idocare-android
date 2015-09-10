package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.utils.IDoCareJSONUtils;

/**
 * This class manages the login state of the user - it aggregates information from all login
 * mechanisms supported by the application (native, Facebook, etc.)
 */
public class UserStateManager {

    private static final String LOG_TAG = UserStateManager.class.getSimpleName();

    private static final String SIGN_UP_NATIVE_URL = Constants.ROOT_URL + "/api-04/user/add";


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
            Log.e(LOG_TAG, "There is more than a single native account on the device. " +
                    "Using the first one returned." +
                    "\nTotal native accounts: " + String.valueOf(accounts.length));
        }
        return true;
    }


    /**
     * Call to this method will add a new account and set its auth token. If the required account
     * already exists - call to this method will only update its auth token
     * @param accountName
     * @param accountType
     * @param authToken
     * @return true if the account was successfully added; false if could not add the account for
     *         any reason
     */
    public boolean addNativeAccount(String accountName, String accountType, String authToken) {

        if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType) || TextUtils.isEmpty(authToken))
            throw new IllegalArgumentException("all parameters must be non-empty");

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
        // If the required account wasn't added - return
        if (!addedSuccessfully) return false;
        // If the required account exists - update its authToken and remove all other accounts
        for (Account acc : existingAccounts) {
            if (acc.equals(account)) {
                setNativeAccountAuthToken(accountName, accountType, authToken);
            } else {
                mAccountManager.removeAccountExplicitly(acc);
            }
        }

        EventBus.getDefault().post(new UserLoginEvent());

        return true;
    }

    public void setNativeAccountAuthToken(String accountName, String accountType,
                                          String authToken) {
        Account account = new Account(accountName, accountType);
        mAccountManager.setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT, authToken);
    }


    public boolean addFacebookAccount(AccessToken accessToken) {

        // Construct a request to fetch user's details
        GraphRequest request = GraphRequest.newMeRequest(accessToken, null);
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email");
        request.setParameters(parameters);

        GraphResponse response = GraphRequest.executeAndWait(request);

        Log.v(LOG_TAG, response.getRawResponse());

        if (response.getError() != null) {
            Log.e(LOG_TAG, response.getRawResponse());
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

        if (!signUpNative(email, password, nickname, firstName, lastName, facebookId)) {
            Log.e(LOG_TAG, "native signup failed!");
            return false;
        }

        EventBus.getDefault().post(new UserLoginEvent());
        return true;
    }

    /**
     * Do not call this method from UI thread!
     * @param email
     * @param password
     * @param nickname
     * @param firstName
     * @param lastName
     * @param facebookId
     * @return
     */
    public boolean signUpNative(String email, String password, String nickname, String firstName,
                                String lastName, String facebookId) {
        HttpPost request = new HttpPost(SIGN_UP_NATIVE_URL);

        ArrayList<NameValuePair> parameters = new ArrayList<>(6);
        parameters.add(new BasicNameValuePair(Constants.FIELD_NAME_USER_EMAIL, email));
        parameters.add(new BasicNameValuePair(Constants.FIELD_NAME_USER_PASSWORD_SIGNUP, password));
        parameters.add(new BasicNameValuePair(Constants.FIELD_NAME_USER_NICKNAME, nickname));
        parameters.add(new BasicNameValuePair(Constants.FIELD_NAME_USER_FIRST_NAME, firstName));
        parameters.add(new BasicNameValuePair(Constants.FIELD_NAME_USER_LAST_NAME, lastName));
        if (!TextUtils.isEmpty(facebookId))
            parameters.add(new BasicNameValuePair(Constants.FIELD_NAME_USER_FACEBOOK_ID, facebookId));

        try {
            request.setEntity(new UrlEncodedFormEntity(parameters));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpResponse response;
        try {
             response = httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (response.getStatusLine().getStatusCode() / 100 != 2) {
            Log.e(LOG_TAG, "unsuccessfull server response on sign up: " + response.getStatusLine().toString());
            return false;
        }

        String jsonData = null;
        try {
            jsonData = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        String userId;
        String authToken;

        try {

            if (!IDoCareJSONUtils.verifySuccessfulStatus(jsonData)) {
                Log.e(LOG_TAG, "unsuccessfull server status in JSON: " + jsonData);
                return false;
            }

            JSONObject dataObj = IDoCareJSONUtils.extractDataJSONObject(jsonData);

            userId = dataObj.getString(Constants.FIELD_NAME_USER_ID);
            authToken = dataObj.getString(Constants.FIELD_NAME_USER_AUTH_TOKEN);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        return addNativeAccount(userId, AccountAuthenticator.ACCOUNT_TYPE_DEFAULT, authToken);

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
            EventBus.getDefault().post(new UserLogoutEvent());
        }
    }

    private void logOutNative() {
        // TODO: write code that removes native account
    }

    private void logOutFacebook() {
        LoginManager.getInstance().logOut();
    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    private static class UserLoginEvent {}

    public static class UserLogoutEvent {}

    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------


}
