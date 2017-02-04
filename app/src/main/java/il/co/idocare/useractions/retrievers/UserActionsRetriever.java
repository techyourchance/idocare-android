package il.co.idocare.useractions.retrievers;

import android.content.ContentResolver;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.contentproviders.IDoCareContract.UserActions;


/**
 * This class handles loading of user actions related info from the cache
 */

public class UserActionsRetriever {

    private ContentResolver mContentResolver;

    public UserActionsRetriever(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    public List<UserActionEntity> getAllUserActions() {

        String[] projection = UserActions.PROJECTION_ALL;
        String selection = null;
        String[] selectionArgs = null;

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(
                    UserActions.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    UserActions.SORT_ORDER_DEFAULT);


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
        id = cursor.getLong(cursor.getColumnIndexOrThrow(UserActions._ID));
        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_TIMESTAMP));
        entityType = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_ENTITY_TYPE));
        entityId = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_ENTITY_ID));
        entityParam = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_ENTITY_PARAM));
        actionType = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_ACTION_TYPE));
        actionParam = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_ACTION_PARAM));

        return new UserActionEntity(id, timestamp, entityType, entityId, entityParam, actionType, actionParam);
    }
}
