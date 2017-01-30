package il.co.idocare.sequences;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.datamodels.pojos.UserSignupNativeData;
import il.co.idocare.utils.Logger;

/**
 * This Sequence executes sequence of steps which log the user into the system using provided
 * Facebook access token.
 */
public class LoginFacebookSequence extends AbstractSequence {

    private static final String TAG = "LoginFacebookSequence";

    private AccessToken mAccessToken;
    private Logger mLogger;

    private LoginFacebookResult mLoginFacebookResult;

    public LoginFacebookSequence(AccessToken accessToken, Logger logger) {
        mAccessToken = accessToken;
        mLogger = logger;
    }

    @Override
    protected String getName() {
        return TAG;
    }

    @Override
    protected void doWork() {

        // Construct a request to fetch user's details
        GraphRequest request = GraphRequest.newMeRequest(mAccessToken, null);
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email");
        request.setParameters(parameters);

        GraphResponse response = GraphRequest.executeAndWait(request);

        mLogger.v(TAG, "Contents of facebook/me response: " + response.getRawResponse());

        if (response.getError() != null) {
            mLogger.e(TAG, "facebook/me returned error response: " +
                    response.getError().getErrorMessage());
            // TODO: facebook errors should be processed - there is a way to recover from some of them
            return;
        }

        JSONObject jsonResponse = response.getJSONObject();
        if (jsonResponse == null) {
            mLogger.e(TAG, "couldn't obtain JSON object from FB response");
            return;
        }

        final String facebookId;
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
            mLogger.e(TAG, "failed to parse JSON object");
            return;
        }

        String authToken;
        if ((authToken = attemptLoginNative(email, password)) != null) {
            mLogger.d(TAG, "native login using FB credentials succeeded");
            loginSucceeded(email, authToken, facebookId);
        } else {
            UserSignupNativeData userData = new UserSignupNativeData(email, password, nickname,
                    firstName, lastName, facebookId, null);
            if ((authToken = attemptSignupNative(userData)) != null) {
                mLogger.d(TAG, "native signup using FB credentials succeeded");
                loginSucceeded(email, authToken, facebookId);
            } else {
                mLogger.d(TAG, "failed to login/signup using FB credentials - FB login failed");
                loginFailed();
            }
        }
    }

    private String attemptLoginNative(String email, String password) {
        final LoginNativeSequence loginNativeSequence =
                new LoginNativeSequence(email, password);

        loginNativeSequence.execute();

        if (loginNativeSequence.getSequenceResult() != null
                && loginNativeSequence.getSequenceResult().isSucceeded()) {
            return loginNativeSequence.getSequenceResult().getAuthToken();
        } else {
            return null;
        }
    }

    private String attemptSignupNative(UserSignupNativeData userData) {
        final SignupNativeSequence signupNativeSequence =
                new SignupNativeSequence(userData);

        signupNativeSequence.execute();

        if (signupNativeSequence.getSequenceResult() != null
                && signupNativeSequence.getSequenceResult().isSucceeded()) {
            return signupNativeSequence.getSequenceResult().getAuthToken();
        } else {
            return null;
        }
    }


    private void setSequenceResult(LoginFacebookResult loginFacebookResult) {
        mLoginFacebookResult = loginFacebookResult;
    }

    public LoginFacebookResult getSequenceResult() {
        return mLoginFacebookResult;
    }

    private void loginSucceeded(String username, String authToken, String facebookId) {
        mLogger.d(TAG, "loginSucceeded called");

        setSequenceResult(new LoginFacebookResult(true, username, authToken, facebookId));
        setState(Sequence.STATE_EXECUTED_SUCCEEDED);
    }

    private void loginFailed() {
        setSequenceResult(new LoginFacebookResult(false, null, null, null));
        setState(Sequence.STATE_EXECUTED_FAILED);
    }

    /**
     * This is the result of Sequence's execution
     */
    public static class LoginFacebookResult {

        private final boolean mSucceeded;
        private final String mUsername;
        private final String mAuthToken;
        private String mFacebookId;

        public LoginFacebookResult(boolean succeeded, String username, String authToken, String facebookId) {
            mSucceeded = succeeded;
            mUsername = username;
            mAuthToken = authToken;
            mFacebookId = facebookId;
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

        public String getFacebookId() {
            return mFacebookId;
        }
    }


}
