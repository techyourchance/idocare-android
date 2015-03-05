package il.co.idocare.models;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import il.co.idocare.Constants;
import il.co.idocare.ServerRequest;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.IDoCareJSONUtils;

/**
 * Created by Vasiliy on 2/19/2015.
 */
public class RequestsMVCModel extends AbstractModelMVC implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "RequestsMVCModel";

    /*
     This object will be used for synchronization of the map underlying this model. We need this
     lock because even though the map which is used (ConcurrentHashMap) is thread safe, it is not
     synchronized in a sense that allows for modifications to be made while the map (or the
     Collection returned by its values() method) is being read.
     In cases when few modifications to the map should be done atomically (if such cases will be
     present at all), this lock object will be used.
     This object will also be used for blocking execution of threads using wait/notify mechanism.
      */
    private final Object LOCK = new Object();

    private ConcurrentHashMap<Long, RequestItem> mRequestItems;
    private Context mContext;

    private boolean mIsUpdating;


    public RequestsMVCModel(Context context) {
        // The only reason for this context is to be able to access shared preferences file
        // TODO: try to find a solution that does not require passing the context around
        mContext = context;

        mIsUpdating = false;
    }

    /**
     * Initialize this model. This method must be called before any of the data in this model is
     * accessed.
     */
    public void initialize() {
        fetchRequestsFromServer();
    }

    /**
     * Update this model with the up-to-date data from the server
     */
    public void update() {
        synchronized (LOCK) {
            if (!mIsUpdating) fetchRequestsFromServer();
        }
    }

    /**
     * Obtain a list of all the requests in this model. This method will block if this model hasn't
     * completed its initialization yet.
     * NOTE: since this method may block, do not ever call it from UI thread.
     * @return a list containing all the requests in this model
     * @throws java.lang.InterruptedException if the calling thread was interrupted during block
     */
    public List<RequestItem> getAllRequests() throws InterruptedException {
        synchronized (LOCK) {
            while (mRequestItems == null) {
                LOCK.wait();
            }
            return new ArrayList<RequestItem>(mRequestItems.values());
        }
    }


    /**
     * Obtain a list of IDs of all the requests in this model. This method will block if this
     * model hasn't completed its initialization yet.
     * NOTE: since this method may block, do not ever call it from UI thread.
     * @return a list containing IDs of all the requests in this model
     * @throws java.lang.InterruptedException if the calling thread was interrupted during block
     */
    public List<Long> getAllRequestsIds()  throws InterruptedException {
        synchronized (LOCK) {
            while (mRequestItems == null) {
                LOCK.wait();
            }
            return new ArrayList<Long>(mRequestItems.keySet());
        }
    }


    /**
     * Get request item having a particular ID.
     * @param id ID of the request
     * @return RequestItem object having the required ID, or null if there is no such request in
     *         the model, or the model hasn't completed its initialization
     */
    public RequestItem getRequest(long id) {
        if (mRequestItems == null)
            return null;
        else
            return mRequestItems.get(Long.valueOf(id));
    }


    /**
     * Execute a new server request asking to fetch all requests
     */
    private void fetchRequestsFromServer() {

        synchronized (LOCK) {
            mIsUpdating = true;
        }

        ServerRequest serverRequest = new ServerRequest(Constants.GET_ALL_REQUESTS_URL,
                Constants.ServerRequestTag.GET_ALL_REQUESTS, this);

        IDoCareHttpUtils.addStandardHeaders(mContext, serverRequest);

        serverRequest.execute();
    }

    @Override
    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.GET_ALL_REQUESTS) {

            synchronized (LOCK) {
                mIsUpdating = false;
            }

            if (responseStatusOk && IDoCareJSONUtils.verifySuccessfulStatus(responseData)) {

                // TODO: decide how to handle JSON parsing exceptions. Maybe rerun server request?

                JSONArray requestsArray = null;
                try {
                    requestsArray = IDoCareJSONUtils.extractDataJSONArray(responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                List<RequestItem> requestsList = null;
                try {
                    requestsList = IDoCareJSONUtils.extractRequestItemsFromJSONArray(requestsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                updateModel(requestsList);
            }
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }


    private void updateModel(List<RequestItem> requests) {

        boolean isInitializing = false;

        // Initialize map with correct capacity
        if (mRequestItems == null) {
            mRequestItems = new ConcurrentHashMap<Long, RequestItem>(requests.size());
            isInitializing = true;
        }

        // Add requests to the map
        for (RequestItem request : requests) {

            long id = request.getId();

            if (!mRequestItems.containsKey(id)) {
                mRequestItems.put(id, request);
            } else {
                // TODO: instead of simply replacing RequestItem, we need to make sure there was an actual change
                mRequestItems.remove(id);
                mRequestItems.put(id, request);
            }

            if (!isInitializing) {
                // Send a notification about change in data (or addition) of this request
                // if this is not the initialization of the model
                notifyOutboxHandlers(Constants.MessageType.M_REQUEST_DATA_UPDATE.ordinal(), 0, 0,
                        Long.valueOf(id));
            }
        }


        // Maybe there are some threads waiting for the model to be initialized
        if (isInitializing) {
            synchronized (LOCK) {
                LOCK.notifyAll();
            }
        }

    }

}
