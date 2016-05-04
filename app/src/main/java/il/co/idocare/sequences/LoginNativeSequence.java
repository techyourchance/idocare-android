package il.co.idocare.sequences;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import il.co.idocare.Constants;
import il.co.idocare.URLs;
import il.co.idocare.authentication.MyAccountManager;
import il.co.idocare.networking.ServerHttpRequest;
import il.co.idocare.networking.responseparsers.HttpResponseParseException;
import il.co.idocare.networking.responseparsers.ResponseParserUtils;
import il.co.idocare.networking.responseparsers.ServerHttpResponseParser;
import il.co.idocare.networking.responseparsers.ServerResponseParsersFactory;
import il.co.idocare.utils.SecurityUtils;

/**
 * This Sequence executes sequence of steps which log the user into the system using provided
 * credentials.
 */
public class LoginNativeSequence extends AbstractSequence {

    private static final String TAG = "LoginNativeSequence";

    private final String mUsername;
    private final String mPassword;
    private MyAccountManager mMyAccountManager;

    private LoginNativeResult mLoginNativeResult;

    public LoginNativeSequence(String username, String password, MyAccountManager myAccountManager) {
        mUsername = username;
        mPassword = password;
        mMyAccountManager = myAccountManager;
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

        // TODO: maybe too wasteful to build a new client for each sequence?
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

        if (mMyAccountManager.addAccount(mUsername, userId, authToken)) {
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


    private void setSequenceResult(LoginNativeResult loginNativeResult) {
        mLoginNativeResult = loginNativeResult;
    }

    public LoginNativeResult getSequenceResult() {
        return mLoginNativeResult;
    }

    private void loginSucceeded(String username, String authToken) {
        setSequenceResult(new LoginNativeResult(true, username, authToken));
        setState(Sequence.STATE_EXECUTED_SUCCEEDED);
    }

    private void loginFailed() {
        setSequenceResult(new LoginNativeResult(false, null, null));
        setState(Sequence.STATE_EXECUTED_FAILED);
    }

    /**
     * This is the result of Sequence's execution
     */
    public static class LoginNativeResult {

        private final boolean mSucceeded;
        private final String mUsername;
        private final String mAuthToken;

        public LoginNativeResult(boolean succeeded, String username, String authToken) {
            mSucceeded = succeeded;
            mUsername = username;
            mAuthToken = authToken;
        }

        public boolean isSucceeded() {
            return mSucceeded;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getAuthToken() {
            return mAuthToken;
        }
    }


}
