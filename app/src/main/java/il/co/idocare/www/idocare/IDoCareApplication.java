package il.co.idocare.www.idocare;

import android.app.Application;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IDoCareApplication extends Application implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "IDoCareApplication";

    private List<RequestItem> mRequestItems;

    ScheduledExecutorService mRequestsUpdateScheduler;

    /**
     * This is a temporary workaround for preserving the requests fetched from the server
     * over activity's lifecycle
     * @return
     */
    public synchronized List<RequestItem> getRequests() {
        return mRequestItems;
    }

    /**
     * This is a temporary workaround for preserving the requests fetched from the server
     * over activity's lifecycle
     * TODO: implement the right approach (DB + SyncAdapter)
     * @param requestItems
     */
    public synchronized void setRequests(List<RequestItem> requestItems) {
        mRequestItems = requestItems;

        if (mRequestsUpdateScheduler == null) {
            startPariodicalUpdate();
        }
    }

    /**
     * Start periodical update of the list of the requests stored in this application object
     */
    private void startPariodicalUpdate() {
        mRequestsUpdateScheduler = Executors.newSingleThreadScheduledExecutor();

        mRequestsUpdateScheduler.scheduleAtFixedRate (new Runnable() {
            public void run() {
                    ServerRequest serverRequest = new ServerRequest(Constants.GET_ALL_REQUESTS_URL,
                            Constants.ServerRequestTag.GET_ALL_REQUESTS, IDoCareApplication.this);
                    serverRequest.addTextField("username", Constants.USERNAME);
                    serverRequest.addTextField("password", Constants.PASSWORD);
                    serverRequest.execute();

            }
        }, 30, 30, TimeUnit.SECONDS);
    }


    @Override
    public void serverResponse(Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.GET_ALL_REQUESTS) {
            List<RequestItem> requests = UtilMethods.extractRequestsFromJSON(responseData);
            setRequests(requests);
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }
}
