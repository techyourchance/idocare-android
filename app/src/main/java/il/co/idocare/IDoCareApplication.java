package il.co.idocare;

import android.app.Application;

import java.util.List;

import il.co.idocare.pojos.RequestItem;

public class IDoCareApplication extends Application {

    private final static String LOG_TAG = "IDoCareApplication";

    private List<RequestItem> mRequestItems;


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
    }
}
