package il.co.idocare.sequences;

import android.accounts.Account;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import il.co.idocare.Constants;
import il.co.idocare.URLs;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.authentication.AccountManagerProxy;
import il.co.idocare.datamodels.pojos.UserSignupNativeData;
import il.co.idocare.networking.ServerHttpRequest;
import il.co.idocare.networking.responseparsers.HttpResponseParseException;
import il.co.idocare.networking.responseparsers.ResponseParserUtils;
import il.co.idocare.networking.responseparsers.ServerHttpResponseParser;
import il.co.idocare.networking.responseparsers.ServerResponseParsersFactory;
import il.co.idocare.utils.SecurityUtils;

/**
 * This Sequence executes sequence of steps which perform user signup using provided credentials
 */
public class SignupNativeSequence extends AbstractSequence {

    private static final String TAG = "SignupNativeSequence";

    private UserSignupNativeData mUserData;
    private AccountManagerProxy mAccountManagerProxy;

    private SignupNativeResult mSignupNativeResult;

    public SignupNativeSequence(UserSignupNativeData userData,
                                AccountManagerProxy accountManagerProxy) {
        mUserData = userData;
        mAccountManagerProxy = accountManagerProxy;
    }

    @Override
    protected String getName() {
        return TAG;
    }

    @Override
    protected void doWork() {

        ServerHttpRequest request = new ServerHttpRequest(URLs.getUrl(URLs.RESOURCE_SIGNUP));

        String encodedUsername = SecurityUtils.encodeStringAsCredential(mUserData.getEmail());
        String encodedPassword = SecurityUtils.encodeStringAsCredential(mUserData.getPassword());

        // add encoded parameters
        request.addTextField(Constants.FIELD_NAME_USER_EMAIL, encodedUsername);
        request.addTextField(Constants.FIELD_NAME_USER_PASSWORD_SIGNUP, encodedPassword);
        // add plain text parameters
        request.addTextField(Constants.FIELD_NAME_USER_NICKNAME, mUserData.getNickname());
        request.addTextField(Constants.FIELD_NAME_USER_FIRST_NAME, mUserData.getFirstName());
        request.addTextField(Constants.FIELD_NAME_USER_LAST_NAME, mUserData.getLastName());
        if (!TextUtils.isEmpty(mUserData.getFacebookId()))
            request.addTextField(Constants.FIELD_NAME_USER_FACEBOOK_ID, mUserData.getFacebookId());
        // add picture
        if (!TextUtils.isEmpty(mUserData.getUserPicturePath()))
            request.addPictureField(Constants.FIELD_NAME_USER_PICTURE,
                    "userPicture", mUserData.getUserPicturePath());

        CloseableHttpResponse response = request.execute(HttpClientBuilder.create().build());

        if (response == null)  {
            Log.e(TAG, "server response is null");
            signupFailed();
            return;
        }


        Bundle parsedResponse;
        ServerHttpResponseParser responseParser = ServerResponseParsersFactory.newInstance(URLs.RESOURCE_SIGNUP);

        // Parse the response
        try {
            parsedResponse = responseParser.parseResponse(response);
        } catch (HttpResponseParseException e) {
            e.printStackTrace();
            signupFailed();
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
            signupFailed();
            return;
        }


        String userId = parsedResponse.getString(ServerHttpResponseParser.KEY_USER_ID);
        String authToken = parsedResponse.getString(ServerHttpResponseParser.KEY_PUBLIC_KEY);


        if (mAccountManagerProxy.addNativeAccount(mUserData.getEmail(), userId, authToken)) {
            signupSucceeded(mUserData.getEmail(), authToken);
        } else {
            signupFailed();
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




    private void setSequenceResult(SignupNativeResult signupNativeResult) {
        mSignupNativeResult = signupNativeResult;
    }

    public SignupNativeResult getSequenceResult() {
        return mSignupNativeResult;
    }


    private void signupSucceeded(String username, String authToken) {
        setSequenceResult(new SignupNativeResult(true, username, authToken));
        setState(Sequence.STATE_EXECUTED_SUCCEEDED);
    }

    private void signupFailed() {
        setSequenceResult(new SignupNativeResult(false, null, null));
        setState(Sequence.STATE_EXECUTED_FAILED);
    }


    /**
     * This is the result of Sequence's execution
     */
    public static class SignupNativeResult {

        private final boolean mSucceeded;
        private final String mUsername;
        private final String mAuthToken;

        public SignupNativeResult(boolean succeeded, String username, String authToken) {
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
