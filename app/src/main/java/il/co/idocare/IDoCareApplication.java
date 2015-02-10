package il.co.idocare;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
