package il.co.idocare.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import il.co.idocare.Constants;
import il.co.idocare.connectivity.ServerRequest;
import il.co.idocare.authentication.AccountAuthenticator;

/**
 * Created by Vasiliy on 2/17/2015.
 */
public class IDoCareHttpUtils {

    private static final String LOG_TAG = IDoCareHttpUtils.class.getSimpleName();

    /**
     * Add "standard" headers to ServerRequest object. Standard headers are:<br>
     * User ID<br>
     * Authentication token<br>
     * Timestamp<br>
     *
     * @param serverRequest ServerRequest to add the headers to
     * @param userId ID of the active account
     * @param authToken the authentication token associated with the active account
     */
    public static void addStandardHeaders(ServerRequest serverRequest, String userId, String authToken) {

        serverRequest.addHeader(Constants.HttpHeader.USER_ID.getValue(), userId);

        long timestamp = System.currentTimeMillis();
        String token = generateAuthToken(userId +
                authToken +
                String.valueOf(timestamp));

        serverRequest.addHeader(Constants.HttpHeader.USER_TOKEN.getValue(), token);
        serverRequest.addHeader(Constants.HttpHeader.USER_TIMESTAMP.getValue(), String.valueOf(timestamp));
    }


    /**
     * This method generates the authentication token from a string of text
     * @param arg string of text from which auth token will be generated
     * @return auth token
     */
    private static String generateAuthToken(String arg) {
        MessageDigest digest=null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            byte[] resultBytes = digest.digest(arg.getBytes("UTF-8"));
            return (String
                    .format("%0" + (resultBytes.length * 2) + "X", new BigInteger(1, resultBytes)))
                    .toLowerCase();
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
