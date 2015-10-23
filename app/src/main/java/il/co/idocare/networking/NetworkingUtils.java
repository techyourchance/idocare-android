package il.co.idocare.networking;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;

import il.co.idocare.networking.responsehandlers.ServerHttpResponseHandler;

/**
 * This class contains static methods which perform a general functions which are not tied
 * to any other particular class.
 */
public class NetworkingUtils {

    /**
     *
     * @param responseStatusCode the status code of the response
     * @return true if the status code corresponds to OK
     */
    public static boolean isHttpResponseCodeSuccessful(int responseStatusCode) {
        return responseStatusCode / 2 == 100;
    }


    /**
     * Check whether the provided Bundle has a particular key set
     * @param bundle the Bundle to check
     * @param key the key to look for
     * @param errorCode if set to any value > 0, then an additional error code of this value will
     * be added to Bundle's {@link ServerHttpResponseHandler#KEY_ERRORS} key if the check fails
     * @return true if the Bundle has a mapping for the particular key
     */
    public static boolean isKeySet(@NonNull Bundle bundle, String key, int errorCode) {
        String entityString = bundle.getString(ServerHttpResponseHandler.KEY_RESPONSE_ENTITY);
        if (TextUtils.isEmpty(entityString)) {
            if (errorCode > 0) {
                NetworkingUtils.addErrorCode(bundle, errorCode);
            }
            return false;
        }
        return true;
    }


    /**
     * Add a new error code to the ArrayList referenced by
     * {@link ServerHttpResponseHandler#KEY_ERRORS} key if it isn't present already
     * @param bundle the bundle to add the error code to
     * @param errorCode the error code to add
     */
    public static void addErrorCode(@NonNull Bundle bundle, int errorCode) {
        // Get the existing mapping
        ArrayList<Integer> errorCodes
                = bundle.getIntegerArrayList(ServerHttpResponseHandler.KEY_ERRORS);
        // Create new ArrayList if there was none
        if (errorCodes == null) errorCodes = new ArrayList<>(1);
        // Add an error code and update the bundle if this particular error code wasn't present
        if (!errorCodes.contains(errorCode)) {
            errorCodes.add(errorCode);
            bundle.putIntegerArrayList(ServerHttpResponseHandler.KEY_ERRORS, errorCodes);
        }
    }

    /**
     * This method gets the list of errors referenced by {@link ServerHttpResponseHandler#KEY_ERRORS}
     * key and formats them for a human readable representation
     * @param bundle the bundle to extract the errors from
     * @return formatted list of errors, or null if there were no errors in the bundle
     */
    public static String extractErrorsToString(Bundle bundle) {
        //TODO!!
        return null;
    }
}
