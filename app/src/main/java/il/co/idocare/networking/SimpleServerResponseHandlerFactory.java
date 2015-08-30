package il.co.idocare.networking;

import android.util.Log;

import il.co.idocare.Constants;
import il.co.idocare.location.ReverseGeocoderFactory;
import il.co.idocare.networking.interfaces.ServerResponseHandler;
import il.co.idocare.networking.interfaces.ServerResponseHandlerFactory;
import il.co.idocare.networking.responsehandlers.RequestsDownloadServerResponseHandler;
import il.co.idocare.networking.responsehandlers.UsersDownloadServerResponseHandler;

/**
 *
 */
public class SimpleServerResponseHandlerFactory implements ServerResponseHandlerFactory {

    private static final String LOG_TAG = SimpleServerResponseHandlerFactory.class.getSimpleName();

    private ReverseGeocoderFactory mReverseGeocoderFactory;

    public SimpleServerResponseHandlerFactory(ReverseGeocoderFactory reverseGeocoderFactory) {
        if (reverseGeocoderFactory == null)
            throw new IllegalArgumentException("must provide valid reverse geocoder factory");

        mReverseGeocoderFactory = reverseGeocoderFactory;
    }


    @Override
    public ServerResponseHandler newInstance(String url) {
        ServerResponseHandler responseHandler = null;

        if (url.equals(Constants.GET_ALL_REQUESTS_URL)) {
            responseHandler =
                    new RequestsDownloadServerResponseHandler(mReverseGeocoderFactory.newInstance());
        } else if (url.equals(Constants.GET_USER_URL)) {
            responseHandler = new UsersDownloadServerResponseHandler();
        } else {
            Log.e(LOG_TAG, "unsupported URL: " + url);
        }

        return responseHandler;
    }
}
