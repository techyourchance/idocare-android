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


    public RequestsMVCModel(Context context) {
        // The only reason for this context is to be able to access shared preferences file
        // TODO: try to find a solution that does not require passing the context around
        mContext = context;
    }

    /**
     * Initialize this model. This method must be called before any of the data in this model is
     * accessed.
     */
    public void initialize() {
        fetchRequestsFromServer();
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
     * Get request item having a particular ID. This method will block if this model hasn't
     * completed its initialization yet.
     * NOTE: since this method may block, do not ever call it from UI thread.
     * @param id ID of the request
     * @return RequestItem object having the required ID, or null if there is no such request in
     *         the model
     * @throws java.lang.InterruptedException if the calling thread was interrupted during block
     */
    public RequestItem getRequest(long id) throws InterruptedException {
        synchronized (LOCK) {
            while (mRequestItems == null) {
                LOCK.wait();
            }
            return mRequestItems.get(Long.valueOf(id));
        }
    }


    /**
     * Execute a new server request asking to fetch all requests
     */
    private void fetchRequestsFromServer() {
        ServerRequest serverRequest = new ServerRequest(Constants.GET_ALL_REQUESTS_URL,
                Constants.ServerRequestTag.GET_ALL_REQUESTS, this);

        IDoCareHttpUtils.addStandardHeaders(mContext, serverRequest);

        serverRequest.execute();
    }

    @Override
    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.GET_ALL_REQUESTS) {
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

        // Initialize map with correct capacity
        ConcurrentHashMap<Long, RequestItem> requestsMap =
                new ConcurrentHashMap<Long, RequestItem>(requests.size());

        // Add requests to the map
        for (RequestItem request : requests) {
            requestsMap.put(request.getId(), request);
        }

        synchronized (LOCK) {
            if (mRequestItems == null) {
                // If the map hasn't been initialized yed, then there might be blocked
                // threads waiting to be notified
                LOCK.notifyAll();
            }

            // TODO: maybe need to update the map and not replace it completely?
            // Replace the map
            mRequestItems = requestsMap;
        }

    }

}
