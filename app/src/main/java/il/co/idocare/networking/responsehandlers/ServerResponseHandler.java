package il.co.idocare.networking.responsehandlers;

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
public interface ServerResponseHandler {


    void handleResponse(int statusCode, String reasonPhrase, String entityString,
                                 ContentProviderClient provider) throws ServerResponseHandlerException ;


    class ServerResponseHandlerException extends Exception {
    }



    public abstract class AbstractServerResponseHandler implements ServerResponseHandler {

        private static final String LOG_TAG = AbstractServerResponseHandler.class.getSimpleName();


        public boolean ensureSuccessfulResponse(int statusCode, String reasonPhrase, String entityString) {

            if (statusCode / 100 != 2) {
                Log.e(LOG_TAG, "unsuccessful status code: " + statusCode + ". Reason phrase: " + reasonPhrase);
                return false;
            }

            if (TextUtils.isEmpty(entityString)) {
                Log.e(LOG_TAG, "got an empty entity from the server. Status code: " + statusCode +
                        ". Reason phrase: " + reasonPhrase);
                return false;
            }


            try {
                JSONObject jsonObj = new JSONObject(entityString);

                String status = jsonObj.getString(Constants.FIELD_NAME_RESPONSE_STATUS);
                String message = jsonObj.getString(Constants.FIELD_NAME_RESPONSE_MESSAGE);

                if (!status.equals("success")) {
                    Log.e(LOG_TAG, "server reported unsuccessful operation:\n" + entityString);
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }


    }

}
