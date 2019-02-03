package il.co.idocarecore.useractions.retrievers;

import android.content.ContentResolver;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import il.co.idocarecore.useractions.entities.UserActionEntity;
import il.co.idocarecore.contentproviders.IDoCareContract.UserActions;
import il.co.idocarecore.contentproviders.IDoCareContract;


/**
 * This class handles loading of user actions related info from the cache
 */

public class UserActionsRetriever {

    private ContentResolver mContentResolver;

    public UserActionsRetriever(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    public List<UserActionEntity> getAllUserActions() {

        return getUserActionsWithSelection(null, null);

    }

    public List<UserActionEntity> getUserActionsAffectingEntity(String entityId) {

        String selection = IDoCareContract.UserActions.COL_ENTITY_ID + " = ?";
        String[] selectionArgs = new String[] {entityId};

        return getUserActionsWithSelection(selection, selectionArgs);
    }

    private List<UserActionEntity> getUserActionsWithSelection(
            @Nullable String selection, @Nullable String[] selectionArgs) {
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(
                    IDoCareContract.UserActions.CONTENT_URI,
                    IDoCareContract.UserActions.PROJECTION_ALL,
                    selection,
                    selectionArgs,
                    IDoCareContract.UserActions.SORT_ORDER_DEFAULT);


            if (cursor != null && cursor.moveToFirst()) {
                List<UserActionEntity> userActions = new ArrayList<>(cursor.getCount());
                do {
                    UserActionEntity userAction = createUserActionEntityFromCurrentCursorPosition(cursor);
                    userActions.add(userAction);
                } while (cursor.moveToNext());
                return userActions;
            } else {
                return new ArrayList<>(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private UserActionEntity createUserActionEntityFromCurrentCursorPosition(Cursor cursor) {
        long id;
        String timestamp;
        String entityType;
        String entityId;
        String entityParam;
        String actionType;
        String actionParam;

        // mandatory fields
        id = cursor.getLong(cursor.getColumnIndexOrThrow(IDoCareContract.UserActions._ID));
        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.UserActions.COL_TIMESTAMP));
        entityType = cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.UserActions.COL_ENTITY_TYPE));
        entityId = cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.UserActions.COL_ENTITY_ID));
        entityParam = cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.UserActions.COL_ENTITY_PARAM));
        actionType = cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.UserActions.COL_ACTION_TYPE));
        actionParam = cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.UserActions.COL_ACTION_PARAM));

        return new UserActionEntity(id, timestamp, entityType, entityId, entityParam, actionType, actionParam);
    }
}
