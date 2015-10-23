package il.co.idocare.networking.responsehandlers;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import il.co.idocare.Constants;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.location.ReverseGeocoder;
import il.co.idocare.datamodels.pojos.RequestItemPojo;

/**
 * This class processes a response from the server containing array of requests
 */
public class LegacyRequestsDownloadServerResponseHandler extends LegacyAbstractServerResponseHandler {

    private static final String LOG_TAG = LegacyRequestsDownloadServerResponseHandler.class.getSimpleName();

    private ReverseGeocoder mReverseGeocoder;

    public LegacyRequestsDownloadServerResponseHandler(ReverseGeocoder reverseGeocoder) {
        if (reverseGeocoder == null)
            throw new IllegalArgumentException("must provide valid reverse geocoder");

        mReverseGeocoder = reverseGeocoder;
    }

    @Override
    public void handleResponse(int statusCode, String reasonPhrase, String entityString,
                               ContentProviderClient provider) {

        if (!ensureSuccessfulResponse(statusCode, reasonPhrase, entityString))
            return;

        JSONArray requestsJsonArray = extractJsonArrayFromData(entityString);

        if (requestsJsonArray == null) {
            Log.e(LOG_TAG, "couldn't extract JSONArray from the response data");
            return;
        }

        List<Long> requestsIdsList = new ArrayList<>(requestsJsonArray.length());

        RequestItemPojo request;
        String location;
        for (int i = 0; i < requestsJsonArray.length(); i++) {

            request = null;

            // Parse the contents of individual JSON objects in the array
            try {
                request = RequestItemPojo.create(requestsJsonArray.getJSONObject(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            requestsIdsList.add(request.getId());

            // Handle the update or the insertion of a single request entry
            updateOrInsertEntry(request, provider);

        }

        // Create a list of downloaded requests' ids that will be used in delete statement
        String idsForQuery = getIdsForQuery(requestsIdsList);

        // Delete requests that do not appear in the list of downloaded requests unless
        // the request is locally modified
        try {
            provider.delete(
                    IDoCareContract.Requests.CONTENT_URI,
                    IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG + " = 0" +
                            " AND " + IDoCareContract.Requests.COL_REQUEST_ID +
                            " NOT IN ( " + idsForQuery + " )",
                    null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private String getIdsForQuery(List<Long> idsList) {
        StringBuilder idsForQuery = new StringBuilder();
        boolean first = true;
        for (long id : idsList) {
            if (first) {
                first = false;
                idsForQuery.append("'").append(String.valueOf(id)).append("'");
            } else {
                idsForQuery.append(",'").append(String.valueOf(id)).append("'");
            }
        }
        return idsForQuery.toString();
    }

    private void updateOrInsertEntry(RequestItemPojo request, ContentProviderClient provider) {

        Cursor cursor = null;
        try {
            // We need to know whether there is a request with the same ID in DB
            cursor = provider.query(
                    ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI,
                            request.getId()),
                    new String[]{IDoCareContract.Requests.COL_LOCATION},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // Check whether a location need to be updated
                String currLocation = cursor.getString(
                        cursor.getColumnIndex(IDoCareContract.Requests.COL_LOCATION));
                if (TextUtils.isEmpty(currLocation))
                    fetchLocationInfoFromReverseGeocoder(request); // Get a new location
                else
                    request.setLocation(currLocation); // Keep the current location


                // Update the corresponding request entry unless it is locally modified
                provider.update(
                        ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI,
                                request.getId()),
                        request.toContentValues(),
                        IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG + " = 0",
                        null
                );
            } else {
                // Obtain the location
                fetchLocationInfoFromReverseGeocoder(request);
                // Insert a new request entry
                provider.insert(IDoCareContract.Requests.CONTENT_URI, request.toContentValues());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void fetchLocationInfoFromReverseGeocoder(RequestItemPojo request) {
        String location = mReverseGeocoder.getFromLocation(request.getLatitude(),
                request.getLongitude(), Locale.getDefault());
        if (!TextUtils.isEmpty(location))
            request.setLocation(location);
    }

    private JSONArray extractJsonArrayFromData(String entityString) {
        // TODO: decide how to handle JSON parsing exceptions. Maybe rerun server request?
        try {
            return new JSONObject(entityString).getJSONArray(Constants.FIELD_NAME_INTERNAL_DATA);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}