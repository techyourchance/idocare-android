package il.co.idocare.networking.interfaces;

import android.content.ContentProviderClient;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants;

/**
 * Object implementing this interface can be used for handling a parsed response from
 * the server
 */
public interface LegacyServerResponseHandler {


    void handleResponse(int statusCode, String reasonPhrase, String entityString,
                                 ContentProviderClient provider) throws ServerResponseHandlerException ;


    class ServerResponseHandlerException extends Exception {
    }


}
