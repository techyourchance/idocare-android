package il.co.idocare.networking.responsehandlers;

import android.os.Bundle;

import java.io.IOException;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;

/**
 * Created by Vasiliy on 9/12/2015.
 */
public class NativeSignupResponseHandler implements ServerHttpResponseHandler {

    private ServerHttpResponseHandler mDecoratedResponseHandler;

    public NativeSignupResponseHandler() {
        mDecoratedResponseHandler = new NativeLoginResponseHandler();
    }

    @Override
    public Bundle handleResponse(HttpResponse httpResponse)
            throws ClientProtocolException, IOException {
        return mDecoratedResponseHandler.handleResponse(httpResponse);
    }
}
