package il.co.idocare.utils;

import android.text.TextUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * This class contains static helper methods related to security
 */
public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Encode arbitrary string as credentials.
     * @param stringToEncode must be non-empty
     * @return the encoded string, or null in case of an error
     */
    public static String encodeStringAsCredential(String stringToEncode) {
        if (TextUtils.isEmpty(stringToEncode))
            throw new IllegalArgumentException("parameters must be non-empty");

        byte[] encodedStringBytes;
        try {
            encodedStringBytes = ("fuckyouhackers" + stringToEncode).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e ) {
            e.printStackTrace();
            return null;
        }
        return Base64.encodeToString(encodedStringBytes, Base64.NO_WRAP);
    }
}
