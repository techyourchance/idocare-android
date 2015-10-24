package il.co.idocare.networking.responseparsers;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * This class contains static methods which perform a general actions for ResponseParsers
 */
public class ResponseParserUtils {

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
     * be added to Bundle's {@link ServerHttpResponseParser#KEY_ERRORS} key if the check fails
     * @return true if the Bundle has a mapping for the particular key
     */
    public static boolean isKeySet(@NonNull Bundle bundle, String key, int errorCode) {
        if (bundle.containsKey(key)) {
            return true;
        } else {
            if (errorCode > 0) {
                ResponseParserUtils.addErrorCode(bundle, errorCode);
            }
            return false;
        }
    }


    /**
     * Add a new error code to the ArrayList referenced by
     * {@link ServerHttpResponseParser#KEY_ERRORS} key if it isn't present already
     * @param bundle the bundle to add the error code to
     * @param errorCode the error code to add
     */
    public static void addErrorCode(@NonNull Bundle bundle, int errorCode) {
        // Get the existing mapping
        ArrayList<Integer> errorCodes
                = bundle.getIntegerArrayList(ServerHttpResponseParser.KEY_ERRORS);
        // Create new ArrayList if there was none
        if (errorCodes == null) errorCodes = new ArrayList<>(1);
        // Add an error code and update the bundle if this particular error code wasn't present
        if (!errorCodes.contains(errorCode)) {
            errorCodes.add(errorCode);
            bundle.putIntegerArrayList(ServerHttpResponseParser.KEY_ERRORS, errorCodes);
        }
    }

    /**
     * This method gets the list of errors referenced by {@link ServerHttpResponseParser#KEY_ERRORS}
     * key and formats them for a human readable representation
     * @param bundle the bundle to extract the errors from
     * @return formatted list of errors, or null if there were no errors in the bundle
     */
    public static String extractErrorsToString(Bundle bundle) {
        //TODO!!
        return null;
    }
}
