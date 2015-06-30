package il.co.idocare.networking;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.util.Log;

import il.co.idocare.Constants;
import il.co.idocare.networking.responsehandlers.RequestsDownloadServerResponseHandler;
import il.co.idocare.networking.responsehandlers.ServerResponseHandler;

/**
 * This class downloads data from the server and updates the local application's cache
 */
public class DataDownloader implements ServerHttpRequest.OnServerResponseCallback {


    public final static String GET_ALL_REQUESTS_URL = Constants.ROOT_URL + "/api-04/request";

    private static final String LOG_TAG = DataDownloader.class.getSimpleName();

    private static final int SERVER_REQUEST_TIMEOUT_MILLIS = 30000;

    private Account mAccount;
    private String mAuthToken;
    private ContentProviderClient mProvider;

    public DataDownloader(Account account, String authToken, ContentProviderClient provider) {
        mAccount = account;
        mAuthToken = authToken;
        mProvider = provider;
    }

    public void downloadAll() {

        ServerHttpRequest serverRequest = new ServerHttpRequest(GET_ALL_REQUESTS_URL,
                mAccount, mAuthToken, this, GET_ALL_REQUESTS_URL);

        if (mAccount != null) serverRequest.addStandardHeaders();

        Thread workerThread = new Thread(serverRequest);
        workerThread.start();
        try {
            // Wait for the worker thread to complete
            // TODO: test what happens if the timeout is reached - does this code fail gracefully?
            workerThread.join(SERVER_REQUEST_TIMEOUT_MILLIS);
            if (workerThread.isAlive()) {
                workerThread.interrupt();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void serverResponse(int statusCode, String reasonPhrase, String entityString,
                               Object asyncCompletionToken) {

        String url = (String) asyncCompletionToken;

        if (url.equals(GET_ALL_REQUESTS_URL)) {

            // Create an appropriate response handler
            ServerResponseHandler responseHandler =
                    new RequestsDownloadServerResponseHandler();

            try {
                responseHandler.handleResponse(statusCode, reasonPhrase, entityString, mProvider);
            } catch (Exception e) { // TODO: catch concrete exceptions thrown by handleResponse()
                e.printStackTrace();
            }

        } else {
            Log.e(LOG_TAG, "receiver serverResponse() callback for unrecognized URL: " + url);
        }
    }
}
