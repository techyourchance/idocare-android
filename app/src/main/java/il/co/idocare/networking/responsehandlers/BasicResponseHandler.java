package il.co.idocare.networking.responsehandlers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.util.EntityUtils;

/**
 * This is a basic response handler - it checks response's result code and logs basic
 * information.
 */
public class BasicResponseHandler implements ServerHttpResponseHandler {

    private static final String LOG_TAG = BasicResponseHandler.class.getSimpleName();

    @Override
    public Bundle handleResponse(@NonNull HttpResponse httpResponse)
            throws ClientProtocolException, IOException {
        Bundle result = new Bundle();

        if (httpResponse.getStatusLine().getStatusCode() / 100 == 2) {
            result.putInt(KEY_RESPONSE_STATUS_OK, 1);
        }

        result.putInt(KEY_RESPONSE_STATUS_CODE, httpResponse.getStatusLine().getStatusCode());

        result.putString(KEY_RESPONSE_REASON_PHRASE, httpResponse.getStatusLine().getReasonPhrase());

        String responseEntityString = EntityUtils.toString(httpResponse.getEntity());
        Log.v(LOG_TAG, "response entity:\n" + responseEntityString);

        result.putString(KEY_RESPONSE_ENTITY, responseEntityString);

        return result;
    }
}
