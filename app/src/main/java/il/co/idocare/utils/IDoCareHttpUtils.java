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
import il.co.idocare.ServerRequest;
import il.co.idocare.authenticators.AccountAuthenticator;

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
     * <b>This method must not be called from the UI thread!</b>
     * 
     * @param activity context that issued a call to this method. This context will be used for
     *                 starting login/signup activities (if required)
     * @param serverRequest ServerRequest to add the headers to
     */
    public static void addStandardHeaders(Activity activity, ServerRequest serverRequest) {
        SharedPreferences prefs =
                activity.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);


        String accountName = prefs.getString(AccountManager.KEY_ACCOUNT_NAME, "");
        String accountType = prefs.getString(AccountManager.KEY_ACCOUNT_TYPE, "");

        if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType)) {
            Log.e(LOG_TAG, "Couldn't obtain the default account data from shared preferences");
            // TODO: this error need to be handled (maybe throw an exception?)
            return;
        }

        Account account = new Account(accountName, accountType);
        Account[] existingAccounts = AccountManager.get(activity).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);

        if (!Arrays.asList(existingAccounts).contains(account)) {
            Log.e(LOG_TAG, "The default account is no longer registered on the device");
            // TODO: this error need to be handled (maybe throw an exception?)
            return;
        }

        AccountManagerFuture<Bundle> future = AccountManager.get(activity).getAuthToken(
                account,
                AccountAuthenticator.ACCOUNT_TYPE,
                null,
                activity,
                null,
                null);

        Bundle result = null;
        try {
            result = future.getResult();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }


        if (result == null || !result.containsKey(AccountManager.KEY_AUTHTOKEN)) {
            Log.e(LOG_TAG, "Couldn't obtain auth token for the specified account");
            // TODO: this error need to be handled (maybe throw an exception?)
            return;

        }

        String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);

        serverRequest.addHeader(Constants.HttpHeader.USER_ID.getValue(), accountName); // Currently what is stored in shared preferences as account name is user ID

        long timestamp = System.currentTimeMillis();
        String token = generateAuthToken(accountName +
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
