package il.co.idocare.sequences;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import il.co.idocare.datamodels.pojos.UserSignupData;
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

    private UserSignupData mUserSignupData;
    private AccountManager mAccountManager;
    private SignupNativeResult mSignupNativeResult;

    public SignupNativeSequence(UserSignupData userSignupData, AccountManager accountManager) {
        mUserSignupData = userSignupData;
        mAccountManager = accountManager;
    }

    @Override
    protected String getName() {
        return TAG;
    }

    @Override
    protected void doWork() {

        ServerHttpRequest request = new ServerHttpRequest(URLs.getUrl(URLs.RESOURCE_SIGNUP));

        String encodedUsername = SecurityUtils.encodeStringAsCredential(mUserSignupData.getEmail());
        String encodedPassword = SecurityUtils.encodeStringAsCredential(mUserSignupData.getPassword());

        // add encoded parameters
        request.addTextField(Constants.FIELD_NAME_USER_EMAIL, encodedUsername);
        request.addTextField(Constants.FIELD_NAME_USER_PASSWORD_SIGNUP, encodedPassword);
        // add plain text parameters
        request.addTextField(Constants.FIELD_NAME_USER_NICKNAME, mUserSignupData.getNickname());
        request.addTextField(Constants.FIELD_NAME_USER_FIRST_NAME, mUserSignupData.getFirstName());
        request.addTextField(Constants.FIELD_NAME_USER_LAST_NAME, mUserSignupData.getLastName());
        if (!TextUtils.isEmpty(mUserSignupData.getFacebookId()))
            request.addTextField(Constants.FIELD_NAME_USER_FACEBOOK_ID, mUserSignupData.getFacebookId());
        // add picture
        if (!TextUtils.isEmpty(mUserSignupData.getUserPicturePath()))
            request.addPictureField(Constants.FIELD_NAME_USER_PICTURE,
                    "userPicture", mUserSignupData.getUserPicturePath());

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


        if (addNativeAccount(mUserSignupData.getEmail(), AccountAuthenticator.ACCOUNT_TYPE_DEFAULT,
                userId, authToken)) {
            signupSucceeded(mUserSignupData.getEmail(), authToken);
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
