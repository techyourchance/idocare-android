package il.co.idocare.entities.cachers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;

import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.entities.UserActionEntity;
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

    public boolean cacheUserAction(UserActionEntity userActionEntity) {

        ContentValues userActionCV = new ContentValues(6);

        userActionCV.put(IDoCareContract.UserActions.COL_TIMESTAMP, userActionEntity.getTimestamp());

        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_TYPE, userActionEntity.getEntityType());

        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_ID, userActionEntity.getEntityId());

        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_PARAM, userActionEntity.getEntityParam());

        userActionCV.put(IDoCareContract.UserActions.COL_ACTION_TYPE, userActionEntity.getEntityType());

        userActionCV.put(IDoCareContract.UserActions.COL_ACTION_PARAM, userActionEntity.getActionParam());

        Uri newUri = mContentResolver.insert(
                IDoCareContract.UserActions.CONTENT_URI,
                userActionCV
        );

        if (newUri != null) {
            mLogger.d(TAG, "vote cached successfully");
            ContentValues requestCV = new ContentValues(1);
            requestCV.put(IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG, 1);
            int updated = mContentResolver.update(
                    ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI, userActionEntity.getEntityId()),
                    requestCV,
                    null,
                    null
            );
            if (updated != 1) {
                mLogger.e(TAG, "couldn't set 'modified' flag on request after vote");
            }
        } else {
            mLogger.e(TAG, "vote caching failed");
            return false;
        }

        return true;
    }
}
