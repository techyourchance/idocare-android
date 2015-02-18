package il.co.idocare.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import il.co.idocare.Constants;
import il.co.idocare.ServerRequest;

/**
 * Created by Vasiliy on 2/17/2015.
 */
public class IDoCareHttpUtils {

    /**
     * Add "standard" headers to ServerRequest object. Standard headers are:<br>
     * User ID<br>
     * Authentication token<br>
     * Timestamp<br>
     * @param context context for accessing a shared preferences file (user data is taken from it)
     * @param serverRequest ServerRequest to add the headers to
     */
    public static void addStandardHeaders(Context context, ServerRequest serverRequest) {
        SharedPreferences prefs =
                context.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

        String userIDString = String.valueOf(prefs.getLong(Constants.FieldName.USER_ID.getValue(), 0));

        serverRequest.addHeader(Constants.HttpHeader.USER_ID.getValue(), userIDString);

        long timestamp = System.currentTimeMillis();
        String token = generateAuthToken(userIDString +
                prefs.getString(Constants.FieldName.USER_PUBLIC_KEY.getValue(), "no_key") +
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
