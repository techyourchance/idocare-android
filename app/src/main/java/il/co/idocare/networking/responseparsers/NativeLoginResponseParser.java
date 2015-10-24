package il.co.idocare.networking.responseparsers;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ch.boye.httpclientandroidlib.HttpResponse;
import il.co.idocare.Constants;

/**
 * This is a decorator which should be used to process server
 * responses to login requests.
 */
public class NativeLoginResponseParser implements ServerHttpResponseParser {

    private static final String LOG_TAG = "NativeLoginRH";

    private ServerHttpResponseParser mDecoratedResponseHandler;

    public NativeLoginResponseParser() {
        mDecoratedResponseHandler = new JsonResponseParser();
    }

    @Override
    public Bundle parseResponse(HttpResponse httpResponse) {
        Bundle result = mDecoratedResponseHandler.parseResponse(httpResponse);

        // Fail fast if no data in the response
        if (!ResponseParserUtils.isKeySet(result, KEY_INTERNAL_DATA_JSON, VALUE_JSON_NO_INTERNAL_DATA)) {
            Log.e(LOG_TAG, "no data in the result returned by decorated handler");
            return result;
        }

        String jsonDataString = result.getString(KEY_INTERNAL_DATA_JSON);

        try {
            JSONObject jsonData = new JSONObject(jsonDataString);


            String userId = jsonData.getString(Constants.FIELD_NAME_USER_ID);
            result.putString(KEY_USER_ID, userId);
            String publicKey = jsonData.getString(Constants.FIELD_NAME_USER_PUBLIC_KEY);
            result.putString(KEY_PUBLIC_KEY, publicKey);

        } catch (JSONException e) {
            throw new HttpResponseParseException("failed to parse JSON", e);
        }

        return result;
    }
}
