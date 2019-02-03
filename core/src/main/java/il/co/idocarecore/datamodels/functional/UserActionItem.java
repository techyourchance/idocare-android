package il.co.idocarecore.datamodels.functional;

import android.database.Cursor;

import il.co.idocarecore.contentproviders.IDoCareContract.UserActions;

/**
 * This POJO contains data about a single user's action
 */
public class UserActionItem {


    public final long mId;
    public long mTimestamp;
    public String mEntityType;
    public long mEntityId;
    public String mEntityParam;
    public String mActionType;
    public String mActionParam;

    private UserActionItem(long id, long timestamp, String entityType,
                           long entityId, String entityParam,
                           String actionType, String actionParam) {

        mId = id;
        mTimestamp = timestamp;
        mEntityType = entityType;
        mEntityId = entityId;
        mEntityParam = entityParam;
        mActionType = actionType;
        mActionParam = actionParam;
    }


    /**
     * Create a new UserActionItem form the data contained in a cursor
     * @param cursor the cursor at the position of the data for the user action item
     * @return a newly created UserActionItem
     * @throws IllegalArgumentException in case any of the required fields are missing from
     *         the cursor
     */
    public static UserActionItem create(Cursor cursor) throws IllegalArgumentException {

        long id;
        long timestamp;
        String entityType;
        long entityId;
        String entityParam;
        String actionType;
        String actionParam;
        
        try {
            id = cursor.getLong(cursor.getColumnIndexOrThrow(UserActions._ID));
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(UserActions.COL_TIMESTAMP));
            entityType = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_ENTITY_TYPE));
            entityId = cursor.getLong(cursor.getColumnIndexOrThrow(UserActions.COL_ENTITY_ID));
            entityParam = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_ENTITY_PARAM));
            actionType = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_ACTION_TYPE));
            actionParam = cursor.getString(cursor.getColumnIndexOrThrow(UserActions.COL_ACTION_PARAM));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
        
        return new UserActionItem(id, timestamp, entityType, entityId, entityParam, actionType, actionParam);
    }

}
