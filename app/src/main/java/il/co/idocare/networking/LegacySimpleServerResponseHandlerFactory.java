package il.co.idocare.networking;

import android.util.Log;

import il.co.idocare.Constants;
import il.co.idocare.location.ReverseGeocoderFactory;
import il.co.idocare.networking.interfaces.LegacyServerResponseHandler;
import il.co.idocare.networking.interfaces.LegacyServerResponseHandlerFactory;
import il.co.idocare.networking.responsehandlers.LegacyRequestsDownloadServerResponseHandler;
import il.co.idocare.networking.responsehandlers.LegacyUsersDownloadServerResponseHandler;

/**
 *
 */
public class LegacySimpleServerResponseHandlerFactory implements LegacyServerResponseHandlerFactory {

    private static final String LOG_TAG = LegacySimpleServerResponseHandlerFactory.class.getSimpleName();

    private ReverseGeocoderFactory mReverseGeocoderFactory;

    public LegacySimpleServerResponseHandlerFactory(ReverseGeocoderFactory reverseGeocoderFactory) {
        if (reverseGeocoderFactory == null)
            throw new IllegalArgumentException("must provide valid reverse geocoder factory");

        mReverseGeocoderFactory = reverseGeocoderFactory;
    }


    @Override
    public LegacyServerResponseHandler newInstance(String url) {
        LegacyServerResponseHandler responseHandler = null;

        if (url.equals(Constants.GET_ALL_REQUESTS_URL)) {
            responseHandler =
                    new LegacyRequestsDownloadServerResponseHandler(mReverseGeocoderFactory.newInstance());
        } else if (url.equals(Constants.GET_NATIVE_USER_DATA_URL)) {
            responseHandler = new LegacyUsersDownloadServerResponseHandler();
        } else {
            Log.e(LOG_TAG, "unsupported URL: " + url);
        }

        return responseHandler;
    }
}
