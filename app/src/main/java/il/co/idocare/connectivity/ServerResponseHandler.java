package il.co.idocare.connectivity;

import android.content.ContentProviderClient;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.pojos.UserActionItem;
import il.co.idocare.utils.IDoCareJSONUtils;

/**
 * Created by Vasiliy on 6/17/2015.
 */
public interface ServerResponseHandler {


    void handleResponse(int statusCode, String reasonPhrase, String entityString, ContentProviderClient provider);




    public static class UserActionsServerResponseHandler implements ServerResponseHandler {

        public UserActionsServerResponseHandler(UserActionItem userAction) {

        }

        @Override
        public void handleResponse(int statusCode, String reasonPhrase, String entityString, ContentProviderClient provider) {

        }
    }

    public static class RequestsDownloadServerResponseHandler implements ServerResponseHandler {

        private static final String LOG_TAG = RequestsDownloadServerResponseHandler.class.getSimpleName();

        @Override
        public void handleResponse(int statusCode, String reasonPhrase, String entityString,
                                   ContentProviderClient provider) {
            if (statusCode / 100 != 2) {
                Log.e(LOG_TAG, "unsuccessful status code: " + statusCode + ". Reason phrase: " + reasonPhrase);
                return;
            }

            if (TextUtils.isEmpty(entityString)) {
                Log.e(LOG_TAG, "got an empty entity from the server. Status code: " + statusCode +
                        ". Reason phrase: " + reasonPhrase);
                return;
            }

            if (!IDoCareJSONUtils.verifySuccessfulStatus(entityString)) {
                Log.e(LOG_TAG, "server reported unsuccessful operation:\n" + entityString);
                return;
            }

            // TODO: decide how to handle JSON parsing exceptions. Maybe rerun server request?
            JSONArray requestsArray = null;
            try {
                requestsArray = IDoCareJSONUtils.extractDataJSONArray(entityString);
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

            // TODO: update the data in DB instead of just deleting it completely!
            int deleted = 0;
            try {
                deleted = provider.delete(IDoCareContract.Requests.CONTENT_URI, null, null);
                Log.v(LOG_TAG, "deleted " + deleted + " requests from the DB");
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            for(RequestItem item : requestsList) {
                // TODO: optimize this loop with batch actions
                try {
                    provider.insert(IDoCareContract.Requests.CONTENT_URI, item.toContentValues());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
