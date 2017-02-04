package il.co.idocare.useractions.cachers;

import android.content.ContentResolver;
import android.content.ContentValues;

import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.utils.Logger;

/**
 * This class handles insertion of user actions into cache
 */
public class UserActionCacher {

    private static final String TAG = "UserActionCacher";

    private ContentResolver mContentResolver;
    private Logger mLogger;

    public UserActionCacher(ContentResolver contentResolver, Logger logger) {
        mContentResolver = contentResolver;
        mLogger = logger;
    }

    public void cacheUserAction(UserActionEntity userAction) {
        mLogger.d(TAG, "cacheUserAction(); user action: " + userAction);

        ContentValues userActionCV = new ContentValues(6);

        userActionCV.put(IDoCareContract.UserActions.COL_TIMESTAMP, userAction.getTimestamp());

        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_TYPE, userAction.getEntityType());

        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_ID, userAction.getEntityId());

        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_PARAM, userAction.getEntityParam());

        userActionCV.put(IDoCareContract.UserActions.COL_ACTION_TYPE, userAction.getActionType());

        userActionCV.put(IDoCareContract.UserActions.COL_ACTION_PARAM, userAction.getActionParam());

        mContentResolver.insert(
                IDoCareContract.UserActions.CONTENT_URI,
                userActionCV
        );
    }

    public void deleteUserAction(UserActionEntity userAction) {
        mLogger.d(TAG, "deleteUserAction(); user action: " + userAction);
        mContentResolver.delete(
                IDoCareContract.UserActions.CONTENT_URI,
                IDoCareContract.UserActions._ID + " = ?",
                new String[] {String.valueOf(userAction.getActionId())}
        );
    }
}
