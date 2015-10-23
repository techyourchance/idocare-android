package il.co.idocare.networking.responsehandlers;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import il.co.idocare.Constants;
import il.co.idocare.networking.NetworkingUtils;

/**
 * This is a decorator which should be used to process server
 * responses to login requests.
 */
public class NativeLoginResponseHandler implements ServerHttpResponseHandler {

    private static final String LOG_TAG = "NativeLoginRH";

    private ServerHttpResponseHandler mDecoratedResponseHandler;

    public NativeLoginResponseHandler() {
        mDecoratedResponseHandler = new JsonResponseHandler();
    }

    @Override
    public Bundle handleResponse(HttpResponse httpResponse)
            throws ClientProtocolException, IOException {
        Bundle result = mDecoratedResponseHandler.handleResponse(httpResponse);

        // Fail fast if no data in the response
        if (!NetworkingUtils.isKeySet(result, KEY_JSON_DATA, VALUE_JSON_NO_INTERNAL_DATA)) {
            Log.e(LOG_TAG, "no data in the result returned by decorated handler");
            return result;
        }

        String jsonDataString = result.getString(KEY_JSON_DATA);

        try {
            JSONObject jsonData = new JSONObject(jsonDataString);


            String userId = jsonData.getString(Constants.FIELD_NAME_USER_ID);
            result.putString(KEY_USER_ID, userId);
            String publicKey = jsonData.getString(Constants.FIELD_NAME_USER_PUBLIC_KEY);
            result.putString(KEY_PUBLIC_KEY, publicKey);

        } catch (JSONException e) {
            NetworkingUtils.addErrorCode(result, ServerHttpResponseHandler.VALUE_JSON_PARSE_ERROR);
            Log.e(LOG_TAG, "got JSON parsing exception");
            e.printStackTrace();
        }

        return result;
    }
}
