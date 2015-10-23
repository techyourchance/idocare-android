package il.co.idocare.networking.responsehandlers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import il.co.idocare.networking.NetworkingUtils;

/**
 * This response handler handles the general HTTP attributes of the response (i.e. status code)
 */
public class BaseResponseHandler implements ServerHttpResponseHandler {

    private static final String LOG_TAG = "BaseResponseHandler";

    @Override
    public Bundle handleResponse(@NonNull HttpResponse httpResponse)
            throws ClientProtocolException, IOException {
        Bundle result = new Bundle();
        int responseCode = httpResponse.getStatusLine().getStatusCode();

        if (NetworkingUtils.isHttpResponseCodeSuccessful(responseCode))
            result.putInt(KEY_RESPONSE_STATUS_OK, 1);

        result.putInt(KEY_RESPONSE_STATUS_CODE, responseCode);

        result.putString(KEY_RESPONSE_REASON_PHRASE, httpResponse.getStatusLine().getReasonPhrase());

        String responseEntityString = EntityUtils.toString(httpResponse.getEntity());

        if (!TextUtils.isEmpty(responseEntityString))
            result.putString(KEY_RESPONSE_ENTITY, responseEntityString);

        return result;
    }
}
