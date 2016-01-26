package il.co.idocare.flows;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.URLs;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.networking.ServerHttpRequest;
import il.co.idocare.networking.responseparsers.HttpResponseParseException;
import il.co.idocare.networking.responseparsers.ResponseParserUtils;
import il.co.idocare.networking.responseparsers.ServerHttpResponseParser;
import il.co.idocare.networking.responseparsers.ServerResponseParsersFactory;
import il.co.idocare.utils.SecurityUtils;

/**
 * This Flow executes sequence of steps which log the user into the system using provided
 * credentials.
 */
public class LoginNativeFlow extends AbstractFlow {

    private static final String TAG = "LoginNativeFlow";

    private final String mUsername;
    private final String mPassword;
    private AccountManager mAccountManager;

    public LoginNativeFlow(String username, String password, AccountManager accountManager) {
        mUsername = username;
        mPassword = password;
        mAccountManager = accountManager;
    }

    @Override
    protected String getName() {
        return TAG;
    }

    @Override
    protected void doWork() {

        String encodedUsername = SecurityUtils.encodeStringAsCredential(mUsername);
        String encodedPassword = SecurityUtils.encodeStringAsCredential(mPassword);

        ServerHttpRequest request = new ServerHttpRequest(URLs.getUrl(URLs.RESOURCE_LOGIN));

        // Add encoded header
        request.addHeader(Constants.HttpHeader.USER_USERNAME.getValue(), encodedUsername);

        // Add encoded parameter
        request.addTextField(Constants.FIELD_NAME_USER_PASSWORD_LOGIN, encodedPassword);

        // TODO: maybe too wasteful to build a new client for each flow?
        CloseableHttpResponse response = request.execute(HttpClientBuilder.create().build());

        if (response == null)  {
            Log.e(TAG, "server response is null");
            loginFailed();
            return;
        }

        Bundle parsedResponse;
        ServerHttpResponseParser responseParser = ServerResponseParsersFactory.newInstance(URLs.RESOURCE_LOGIN);

        // Parse the response
        try {
            parsedResponse = responseParser.parseResponse(response);
        } catch (HttpResponseParseException e) {
            e.printStackTrace();
            loginFailed();
            return;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Check for common errors
        if (parsedResponse == null || !isValidResponse(parsedResponse)) {
            loginFailed();
            return;
        }

        String userId = parsedResponse.getString(ServerHttpResponseParser.KEY_USER_ID);
        String authToken = parsedResponse.getString(ServerHttpResponseParser.KEY_PUBLIC_KEY);


        if (addNativeAccount(mUsername, AccountAuthenticator.ACCOUNT_TYPE_DEFAULT,
                userId, authToken)) {
            loginSucceeded(mUsername, authToken);
        } else {
            loginFailed();
        }

    }

    /**
     * Ensure that parsed response includes all the required information and doesn't have errors
     */
    private boolean isValidResponse(Bundle result) {
        if (!result.containsKey(ServerHttpResponseParser.KEY_RESPONSE_STATUS_OK)) {
            Log.d(TAG, "unsuccessful HTTP response code: "
                    + result.getInt(ServerHttpResponseParser.KEY_RESPONSE_STATUS_CODE));
            return false;
        }
        if (!result.containsKey(ServerHttpResponseParser.KEY_INTERNAL_STATUS_SUCCESS)) {
            Log.d(TAG, "unsuccessful internal status: "
                    + result.getString(ServerHttpResponseParser.KEY_INTERNAL_STATUS));
            return false;
        }
        if (result.containsKey(ServerHttpResponseParser.KEY_ERRORS)) {
            Log.d(TAG, "parsed response contains errors: "
                    + ResponseParserUtils.extractErrorsToString(result));
            return false;
        }

        return true;
    }

    /**
     * Call to this method will add a new account and set its auth token. If the required account
     * already exists - call to this method will only update its auth token.
     */
    private boolean addNativeAccount(String username, String accountType,
                                    String userId, String authToken) {

        Log.d(TAG, "attempting to add a native account; username: " + username + "; account type: "
                + accountType + "; user ID: " + userId + "; authToken: " + authToken);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(accountType)
                || TextUtils.isEmpty(userId) || TextUtils.isEmpty(authToken)) {
            Log.e(TAG, "account addition failed - invalid parameters");
            return false;
        }

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
        boolean targetAccountExists = Arrays.asList(existingAccounts).contains(account);

        if (!targetAccountExists) {
            Log.d(TAG, "failed to add native account");
            return false;
        }

        // The required account exists - update its authToken and remove all other accounts
        for (Account acc : existingAccounts) {
            if (acc.equals(account)) {
                setNativeAccountAuthToken(username, accountType, authToken);
            } else {
                mAccountManager.removeAccount(acc, null, null);
            }
        }

        return true;
    }

    private void setNativeAccountAuthToken(String accountName, String accountType,
                                          String authToken) {
        Account account = new Account(accountName, accountType);
        mAccountManager.setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT, authToken);
    }

    private void loginSucceeded(String username, String authToken) {
        EventBus.getDefault().post(new LoginStateEvents.LoginSucceededEvent(username, authToken));
    }

    private void loginFailed() {
        EventBus.getDefault().post(new LoginStateEvents.LoginFailedEvent());
    }


}
