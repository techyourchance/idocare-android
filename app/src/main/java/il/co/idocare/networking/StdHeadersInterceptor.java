package il.co.idocare.networking;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import il.co.idocare.authentication.LoggedInUserEntity;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.utils.Logger;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This implementation of interceptor injects standard headers into HTTP requests
 */

public class StdHeadersInterceptor implements Interceptor {

    private static final String TAG = "StdHeadersInterceptor";

    private final LoginStateManager mLoginStateManager;
    private final Logger mLogger;

    public StdHeadersInterceptor(LoginStateManager loginStateManager, Logger logger) {
        mLoginStateManager = loginStateManager;
        mLogger = logger;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request requestWithStandardHeaders = buildNewRequestWithStandardHeaders(chain.request());
        return chain.proceed(requestWithStandardHeaders);
    }

    private Request buildNewRequestWithStandardHeaders(Request originalRequest) {

        LoggedInUserEntity user = mLoginStateManager.getLoggedInUser();
        String userId = user.getUserId();
        String authToken = user.getAuthToken();

        if (userId == null || userId.isEmpty()) {
            mLogger.d(TAG, "no logged in user - skipping addition of standard HTTP headers");
            return originalRequest;
        }

        long timestamp = System.currentTimeMillis();

        String userToken =  generateUserToken(userId + authToken + String.valueOf(timestamp));


        return originalRequest.newBuilder()
                .addHeader("Idc-user-id", userId)
                .addHeader("Idc-user-token", userToken)
                .addHeader("Idc-user-timestamp", String.valueOf(timestamp))
                .build();
    }

    /**
     * This method generates the authentication token from a string of text
     * @param arg string of text from which auth token will be generated
     * @return auth token
     */
    private String generateUserToken(String arg) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            byte[] resultBytes = digest.digest(arg.getBytes("UTF-8"));
            return (String
                    .format("%0" + (resultBytes.length * 2) + "X", new BigInteger(1, resultBytes)))
                    .toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
