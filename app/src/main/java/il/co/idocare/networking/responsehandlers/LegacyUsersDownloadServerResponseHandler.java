package il.co.idocare.networking.responsehandlers;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.pojos.UserItem;

/**
 * This class processes a response from the server containing information about a particular user
 */
public class LegacyUsersDownloadServerResponseHandler extends LegacyAbstractServerResponseHandler {

    private static final String LOG_TAG = LegacyUsersDownloadServerResponseHandler.class.getSimpleName();

    @Override
    public void handleResponse(int statusCode, String reasonPhrase, String entityString,
                               ContentProviderClient provider) {

        if (!ensureSuccessfulResponse(statusCode, reasonPhrase, entityString))
            return;

        String userJsonObjectString = null;
        try {
            JSONObject jsonObject = new JSONObject(entityString);
            userJsonObjectString = jsonObject.getJSONObject(Constants.FIELD_NAME_RESPONSE_DATA).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Handle the update or the insertion of a single user entry
        updateOrInsertEntry(UserItem.create(userJsonObjectString), provider);

    }

    private void updateOrInsertEntry(UserItem userItem, ContentProviderClient provider) {

        if (userItem == null) return;

        try {

            // If there is a user with this ID in the DB - update this entry
            int updated = provider.update(
                    ContentUris.withAppendedId(IDoCareContract.Users.CONTENT_URI, userItem.getId()),
                    userItem.toContentValues(),
                    null,
                    null
            );

            if (updated == 0) {
                // No user with that ID in the DB - insert a new entry
                provider.insert(IDoCareContract.Users.CONTENT_URI, userItem.toContentValues());
            } else if (updated != 1) {
                Log.e(LOG_TAG, "inconsistent number of users entries affected by the update." +
                        "Entries affected: " + updated);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}