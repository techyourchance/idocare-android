package il.co.idocare.networking.responsehandlers;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import il.co.idocare.Constants;

/**
 * This is a decorator for {@link BasicResponseHandler} which should be used to process server
 * responses to login requests.
 */
public class NativeLoginResponseHandler implements ServerHttpResponseHandler {

    private static final String LOG_TAG = NativeLoginResponseHandler.class.getSimpleName();

    private ServerHttpResponseHandler mDecoratedResponseHandler;

    public NativeLoginResponseHandler() {
        mDecoratedResponseHandler = new BasicResponseHandler();
    }

    @Override
    public Bundle handleResponse(HttpResponse httpResponse)
            throws ClientProtocolException, IOException {
        Bundle result = mDecoratedResponseHandler.handleResponse(httpResponse);

        String entityString = result.getString(KEY_RESPONSE_ENTITY);

        try {
            JSONObject entityObj = new JSONObject(entityString);

            // Get internal status
            String internalStatus = entityObj.getString(Constants.FIELD_NAME_INTERNAL_STATUS);
            if (internalStatus.equals("success"))
                result.putInt(KEY_INTERNAL_STATUS_SUCCESS, 1);

            // Get message string
            String message = entityObj.getString(Constants.FIELD_NAME_RESPONSE_MESSAGE);
            result.putString(KEY_MESSAGE, message);

            if (entityObj.has("data")) {
                // Parse data
                JSONObject dataObj = entityObj.getJSONObject("data");
                String userId = dataObj.getString(Constants.FIELD_NAME_USER_ID);
                result.putString(KEY_USER_ID, userId);
                String publicKey = dataObj.getString(Constants.FIELD_NAME_USER_PUBLIC_KEY);
                result.putString(KEY_PUBLIC_KEY, publicKey);
            }

        } catch (JSONException e) {
            result.putString(KEY_ERROR_TYPE, VALUE_JSON_PARSE_ERROR);
            Log.e(LOG_TAG, "JSON parse error. The parsed string:\n" + entityString);
            e.printStackTrace();
        }

        return result;
    }
}
