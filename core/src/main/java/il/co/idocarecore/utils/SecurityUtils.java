package il.co.idocarecore.utils;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * This class contains static helper methods related to security
 */
public class SecurityUtils {

    private static final String TAG = "SecurityUtils";

    private SecurityUtils() {}

    /**
     * Encode arbitrary string as credentials.
     * @return the encoded string, or null in case of an error
     */
    public static String encodeStringAsCredential(String stringToEncode) {
        byte[] encodedStringBytes;
        try {
            encodedStringBytes = ("fuckyouhackers" + stringToEncode).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e ) {
            throw new RuntimeException(e);
        }
        return Base64.encodeToString(encodedStringBytes, Base64.NO_WRAP);
    }
}
