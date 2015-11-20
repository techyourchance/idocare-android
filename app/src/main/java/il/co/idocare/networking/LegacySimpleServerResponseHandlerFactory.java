package il.co.idocare.networking;

import android.util.Log;

import il.co.idocare.URLs;
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

        Log.d(LOG_TAG, "creatign LegacyResponseHandler for URL " + url);

        if (url.equals(URLs.getUrl(URLs.RESOURCE_ALL_REQUESTS_DATA))) {
            responseHandler =
                    new LegacyRequestsDownloadServerResponseHandler(mReverseGeocoderFactory.newInstance());
        } else if (url.equals(URLs.getUrl(URLs.RESOURCE_USERS_DATA))) {
            responseHandler = new LegacyUsersDownloadServerResponseHandler();
        } else {
            Log.e(LOG_TAG, "unsupported URL: " + url);
        }

        return responseHandler;
    }
}
