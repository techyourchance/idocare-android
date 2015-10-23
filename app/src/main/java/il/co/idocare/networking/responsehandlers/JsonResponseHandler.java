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
 * This response handler handles the general attributes of response's entity formatted as JSON.<br>
 * It is assumed that the received JSON has the following format:
 * <pre>
 *     {"status":"some_status" , "message":"some_message", "data":&lt; element|array|object &gt;}
 * </pre>
 */
public class JsonResponseHandler implements ServerHttpResponseHandler {

    private static final String LOG_TAG = "JsonResponseHandler";

    private ServerHttpResponseHandler mDecoratedResponseHandler;

    public JsonResponseHandler() {
        mDecoratedResponseHandler = new BaseResponseHandler();
    }

    @Override
    public Bundle handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
        Bundle result = mDecoratedResponseHandler.handleResponse(httpResponse);

        // Fail fast if no entity in the response
        if (!NetworkingUtils
                .isKeySet(result, KEY_RESPONSE_ENTITY, VALUE_NO_ENTITY_IN_RESPONSE_ERROR)) {
            Log.e(LOG_TAG, "no entity in the result returned by decorated handler");
            return result;
        }

        String entityString = result.getString(KEY_RESPONSE_ENTITY);

        try {
            JSONObject entityObj = new JSONObject(entityString);

            // Ensure that all require fields present and fail fast if not
            if (!areAllRequiredFieldsPresent(entityObj, result)) return result;


            // Get internal status and set the relevant keys
            String internalStatus = entityObj.getString(Constants.FIELD_NAME_INTERNAL_STATUS);
            result.putString(ServerHttpResponseHandler.KEY_INTERNAL_STATUS, internalStatus);
            if (internalStatus.equals("success"))
                result.putInt(KEY_INTERNAL_STATUS_SUCCESS, 1);

            // Get message string
            result.putString(KEY_MESSAGE, entityObj.getString(Constants.FIELD_NAME_INTERNAL_MESSAGE));

            // "data" is the optional field
            if (entityObj.has("data")) {
                // Parse data
                JSONObject dataObj = entityObj.getJSONObject("data");
                // Store data
                result.putString(ServerHttpResponseHandler.KEY_JSON_DATA, dataObj.toString());
            }

        } catch (JSONException e) {
            NetworkingUtils.addErrorCode(result, ServerHttpResponseHandler.VALUE_JSON_PARSE_ERROR);
            Log.e(LOG_TAG, "got JSON parsing exception");
            e.printStackTrace();
        }

        return result;
    }

    private boolean areAllRequiredFieldsPresent(JSONObject json, Bundle result) {

        if (!json.has(Constants.FIELD_NAME_INTERNAL_STATUS)) {
            NetworkingUtils.addErrorCode(result,
                    ServerHttpResponseHandler.VALUE_JSON_NO_INTERNAL_STATUS);
            return false;
        }
        if (!json.has(Constants.FIELD_NAME_INTERNAL_MESSAGE)) {
            NetworkingUtils.addErrorCode(result,
                    ServerHttpResponseHandler.VALUE_JSON_NO_INTERNAL_MESSAGE);
            return false;
        }

        return true;
    }
}
