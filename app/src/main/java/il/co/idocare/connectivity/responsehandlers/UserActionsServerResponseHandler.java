package il.co.idocare.connectivity.responsehandlers;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.pojos.UserActionItem;

/**
 * Instances of this class handle server's responses to user's actions uploading
 */

public class UserActionsServerResponseHandler extends ServerResponseHandler.AbstractServerResponseHandler {

    private static final String LOG_TAG = UserActionsServerResponseHandler.class.getSimpleName();

    private UserActionItem mUserAction;

    public UserActionsServerResponseHandler(UserActionItem userAction) {
        mUserAction = userAction;
    }

    @Override
    public void handleResponse(int statusCode, String reasonPhrase, String entityString,
                               ContentProviderClient provider) throws ServerResponseHandlerException{

        if (!ensureSuccessfulResponse(statusCode, reasonPhrase, entityString))
            return;

        String entityType = mUserAction.mEntityType;
        String actionType = mUserAction.mActionType;

        switch (entityType) {

            case IDoCareContract.UserActions.ENTITY_TYPE_REQUEST:
                switch (actionType) {
                    case IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST:

                        RequestItem requestItem = null;
                        try {
                            requestItem = RequestItem.createRequestItem(new JSONObject(entityString)
                                    .getJSONObject(Constants.FIELD_NAME_RESPONSE_DATA).toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            throw new ServerResponseHandlerException();
                        }

                        // Update the locally cached request with the actual data from the server
                        int updated = -1;
                        try {
                            updated = provider.update(
                                    ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI,
                                            mUserAction.mEntityId),
                                    requestItem.toContentValues(),
                                    null,
                                    null
                            );
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            throw new ServerResponseHandlerException();
                        }

                        if (updated != 1) {
                            Log.e(LOG_TAG, "the amount of updated request entries after uploading" +
                                    "a new request to the server is incorrect. Entries updated: " + updated);
                        }

                        /*
                        The above update changed the ID of the entity from the temp ID assigned
                        locally to the permanent ID assigned by the server. We need to update all
                        references to the old temp ID...
                         */
                        updateEntityIdReferences(provider, mUserAction.mEntityId, requestItem.getId());

                        break;

                    case IDoCareContract.UserActions.ACTION_TYPE_PICKUP_REQUEST:
                        throw new UnsupportedOperationException("'" + actionType + "' action type" +
                                "is not supported yet!");
                    case IDoCareContract.UserActions.ACTION_TYPE_CLOSE_REQUEST:
                        throw new UnsupportedOperationException("'" + actionType + "' action type" +
                                "is not supported yet!");
                    case IDoCareContract.UserActions.ACTION_TYPE_VOTE:
                        throw new UnsupportedOperationException("'" + actionType + "' action type" +
                                "is not supported yet!");
                    default:
                        throw new IllegalArgumentException("unknown action type '" + actionType
                                + "' for entity '" + entityType + "'");
                }
                break;

            case IDoCareContract.UserActions.ENTITY_TYPE_ARTICLE:
                switch (actionType) {
                    case "DUMMY":
                        // TODO: remove this case - it was added in order to add "break" statement
                        break;
                    case IDoCareContract.UserActions.ACTION_TYPE_VOTE:
                        throw new UnsupportedOperationException("'" + actionType + "' action type" +
                                "is not supported yet!");
                    default:
                        throw new IllegalArgumentException("unknown action type '" + actionType
                                + "' for entity '" + entityType + "'");
                }
                break;

            default:
                throw new IllegalArgumentException("unknown entity type '" + entityType + "'");
        }
    }

    private void updateEntityIdReferences(ContentProviderClient provider, long oldId,
                                          long newId) throws ServerResponseHandlerException {
        /*
        We should replace all references to the old ENTITY_ID with references to
        the new ENTITY_ID.
        */
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(IDoCareContract.UserActions.COL_ENTITY_ID, newId);
        try {
            provider.update(
                    IDoCareContract.UserActions.CONTENT_URI,
                    contentValues,
                    IDoCareContract.UserActions.COL_ENTITY_ID + " = ?",
                    new String[] {String.valueOf(oldId)}
            );
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new ServerResponseHandlerException();
        }


        /*
        However, just changing the references is not enough: in order to maintain
        consistency for data which has already been fetched from DB into memory, we
        should create a mapping from the old temp ENTITY_ID to the permanent
        ENTITY_ID assigned by the server (otherwise all data related to the old
        ENTITY_ID will be lost or cause issues).
        */
        contentValues = new ContentValues(2);
        contentValues.put(IDoCareContract.TempIdMappings.COL_TEMP_ID, oldId);
        contentValues.put(IDoCareContract.TempIdMappings.COL_PERMANENT_ID, newId);
        try {
            provider.insert(IDoCareContract.TempIdMappings.CONTENT_URI, contentValues);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new ServerResponseHandlerException();
        }

    }
}
