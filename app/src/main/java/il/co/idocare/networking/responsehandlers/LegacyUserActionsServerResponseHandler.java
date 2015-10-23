package il.co.idocare.networking.responsehandlers;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.datamodels.functional.UserActionItem;
import il.co.idocare.datamodels.pojos.RequestItemPojo;

/**
 * Instances of this class handle server's responses to user's actions uploading
 */

public class LegacyUserActionsServerResponseHandler extends LegacyAbstractServerResponseHandler {

    private static final String LOG_TAG = LegacyUserActionsServerResponseHandler.class.getSimpleName();

    private UserActionItem mUserAction;

    public LegacyUserActionsServerResponseHandler(UserActionItem userAction) {
        mUserAction = userAction;
    }

    @Override
    public void handleResponse(int statusCode, String reasonPhrase, String entityString,
                               ContentProviderClient provider) throws ServerResponseHandlerException{

        if (!ensureSuccessfulResponse(statusCode, reasonPhrase, entityString))
            throw  new ServerResponseHandlerException();

        String entityType = mUserAction.mEntityType;
        String actionType = mUserAction.mActionType;

        RequestItemPojo requestItem;
        int updated;

        switch (entityType) {

            case IDoCareContract.UserActions.ENTITY_TYPE_REQUEST:
                switch (actionType) {
                    case IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST:

                        requestItem = extractRequestFromEntityString(entityString);

                        /*
                        The update of request data will change the ID of the entity from the temp ID
                        assigned locally to the permanent ID assigned by the server. We will need to
                        update all references of the old ID to the new ID... But:

                        It is crucial that the below method (which stores in DB the mapping
                        from the old ID to the new ID) will be called  BEFORE the update of
                        the actual request data - the update of the request data might trigger data
                        re-queries on the UI thread, in which case we want the mapping to the new ID
                        to already reside in DB (otherwise there will be race conditions between
                        mapping storage here and mapping queries on the UI thread). Huh...
                         */
                        updateEntityIdReferences(provider, mUserAction.mEntityId, requestItem.getId());

                        // Update the actual request data
                        updated = updateRequestData(provider, requestItem);

                        if (updated != 1) {
                            Log.e(LOG_TAG, "the amount of updated request entries after uploading" +
                                    "a new request to the server is incorrect. Entries updated: " + updated);
                        }

                        break;

                    case IDoCareContract.UserActions.ACTION_TYPE_PICKUP_REQUEST:

                        requestItem = extractRequestFromEntityString(entityString);

                        updated = updateRequestData(provider, requestItem);

                        if (updated != 1) {
                            Log.e(LOG_TAG, "the amount of updated request entries after uploading" +
                                    "a pickup to the server is incorrect. Entries updated: " + updated);
                        }

                        break;
                    case IDoCareContract.UserActions.ACTION_TYPE_CLOSE_REQUEST:

                        requestItem = extractRequestFromEntityString(entityString);

                        updated = updateRequestData(provider, requestItem);

                        if (updated != 1) {
                            Log.e(LOG_TAG, "the amount of updated request entries after uploading" +
                                    "a close to the server is incorrect. Entries updated: " + updated);
                        }

                        break;

                    case IDoCareContract.UserActions.ACTION_TYPE_VOTE:

                        requestItem = extractRequestFromEntityString(entityString);

                        updated = updateRequestData(provider, requestItem);

                        if (updated != 1) {
                            Log.e(LOG_TAG, "the amount of updated request entries after uploading" +
                                    "a vote to the server is incorrect. Entries updated: " + updated);
                        }

                        break;

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

    private int updateRequestData(ContentProviderClient provider, RequestItemPojo request) throws
            ServerResponseHandlerException{

        // Update the locally cached request with the actual data from the server
        int updated = -1;
        try {
            updated = provider.update(
                    ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI,
                            mUserAction.mEntityId),
                    request.toContentValues(),
                    null,
                    null
            );
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new ServerResponseHandlerException();
        }
        return updated;
    }

    private RequestItemPojo extractRequestFromEntityString(String entityString) throws
    ServerResponseHandlerException {
        RequestItemPojo requestItem = null;
        try {
            requestItem = RequestItemPojo.create(new JSONObject(entityString)
                    .getJSONObject(Constants.FIELD_NAME_INTERNAL_DATA).toString());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ServerResponseHandlerException();
        }
        return requestItem;
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
