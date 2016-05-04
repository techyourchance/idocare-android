package il.co.idocare.networking.interfaces;

import android.content.ContentProviderClient;

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
