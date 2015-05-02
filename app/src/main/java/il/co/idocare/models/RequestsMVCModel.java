package il.co.idocare.models;

import android.content.ContentResolver;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.pojos.RequestItem;

/**
 * This model allows reading and updating requests' related information
 */
public class RequestsMVCModel extends AbstractModelMVC {

    private final static String LOG_TAG = "RequestsMVCModel";

    private static final RequestsMVCModel sInstance = new RequestsMVCModel();

    private RequestsMVCModel() {}

    /**
     *
     * @return the instance of the model
     */
    public static RequestsMVCModel getInstance() {
        return sInstance;
    }


    /**
     * Obtain a list of all the requests in this model.
     * @return a list containing all the requests in this model
     */
    public List<RequestItem> getAllRequests(ContentResolver contentResolver) {
        String[] projection = IDoCareContract.Requests.PROJECTION_ALL;

        Cursor cursor = contentResolver
                .query(IDoCareContract.Requests.CONTENT_URI, projection, null, null, null);

        List<RequestItem> requests = new ArrayList<RequestItem>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                requests.add(requestItemFromCurrentCursorEntry(cursor));
            } while(cursor.moveToNext());
        }

        if (cursor != null) cursor.close();

        return requests;
    }


    /**
     * Obtain a list of IDs of all the requests in this model.
     * @return a list containing IDs of all the requests in this model
     */
    public List<Long> getAllRequestsIds(ContentResolver contentResolver) {

        String[] projection = new String[] {IDoCareContract.Requests.REQUEST_ID};

        Cursor cursor = contentResolver
                .query(IDoCareContract.Requests.CONTENT_URI, projection, null, null, null);

        List<Long> requestsIds = new ArrayList<Long>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                requestsIds.add(cursor.getLong(cursor.getColumnIndex(IDoCareContract.Requests.REQUEST_ID)));
            } while(cursor.moveToNext());
        }

        if (cursor != null) cursor.close();

        return requestsIds;
    }


    /**
     * Get request item having a particular ID.
     * @param id ID of the request
     * @return RequestItem object having the required ID, or null if there is no such request
     */
    public RequestItem getRequest(ContentResolver contentResolver, long id) {
        String[] projection = IDoCareContract.Requests.PROJECTION_ALL;
        String selection = IDoCareContract.Requests.REQUEST_ID + "=?";
        String[] selectionArgs = new String[] {String.valueOf(id)};

        Cursor cursor = contentResolver
                .query(IDoCareContract.Requests.CONTENT_URI, projection, selection, selectionArgs, null);

        RequestItem request = null;

        if (cursor != null && cursor.moveToFirst()) {
            request = requestItemFromCurrentCursorEntry(cursor);
        }

        if (cursor != null) cursor.close();

        return request;
    }

//
//    /**
//     * Execute a new server request asking to fetch all requests
//     */
//    private void fetchRequestsFromServer() {
//
//        synchronized (LOCK) {
//            mIsUpdating = true;
//        }
//
//        ServerRequest serverRequest = new ServerRequest(Constants.GET_ALL_REQUESTS_URL,
//                Constants.ServerRequestTag.GET_ALL_REQUESTS, this);
//
//        IDoCareHttpUtils.addStandardHeaders(mContext, serverRequest);
//
//        serverRequest.execute();
//    }
//
//    @Override
//    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData) {
//        if (tag == Constants.ServerRequestTag.GET_ALL_REQUESTS) {
//
//            synchronized (LOCK) {
//                mIsUpdating = false;
//            }
//
//            if (responseStatusOk && IDoCareJSONUtils.verifySuccessfulStatus(responseData)) {
//
//                // TODO: decide how to handle JSON parsing exceptions. Maybe rerun server request?
//
//                JSONArray requestsArray = null;
//                try {
//                    requestsArray = IDoCareJSONUtils.extractDataJSONArray(responseData);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    return;
//                }
//
//                List<RequestItem> requestsList = null;
//                try {
//                    requestsList = IDoCareJSONUtils.extractRequestItemsFromJSONArray(requestsArray);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    return;
//                }
//
//                updateModel(requestsList);
//            }
//        } else {
//            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
//        }
//    }
//
//
//    private void updateModel(List<RequestItem> requests) {
//
//        boolean isInitializing = false;
//
//        // Initialize map with correct capacity
//        if (mRequestItems == null) {
//            mRequestItems = new ConcurrentHashMap<Long, RequestItem>(requests.size());
//            isInitializing = true;
//        }
//
//        // Add requests to the map
//        for (RequestItem request : requests) {
//
//            long id = request.getId();
//
//            if (!mRequestItems.containsKey(id)) {
//                mRequestItems.put(id, request);
//            } else {
//                // TODO: instead of simply replacing RequestItem, we need to make sure there was an actual change
//                mRequestItems.remove(id);
//                mRequestItems.put(id, request);
//            }
//
//            if (!isInitializing) {
//                // Send a notification about change in data (or addition) of this request
//                // if this is not the initialization of the model
//                notifyOutboxHandlers(Constants.MessageType.M_REQUEST_DATA_UPDATE.ordinal(), 0, 0,
//                        Long.valueOf(id));
//            }
//        }
//
//
//        // Maybe there are some threads waiting for the model to be initialized
//        if (isInitializing) {
//            synchronized (LOCK) {
//                LOCK.notifyAll();
//            }
//        }
//
//    }



    /**
     * This method reads the fields of a request from current cursor's position and constructs
     * a new RequestItem object.
     * @param cursor a valid cursor pointing to a valid request entry
     * @return RequestItem object
     */
    private RequestItem requestItemFromCurrentCursorEntry(Cursor cursor) {

        RequestItem item = RequestItem.createRequestItem(
                cursor.getLong(cursor.getColumnIndex(IDoCareContract.Requests.REQUEST_ID)));

        // TODO: add other fields as well

        return item;
    }

}
