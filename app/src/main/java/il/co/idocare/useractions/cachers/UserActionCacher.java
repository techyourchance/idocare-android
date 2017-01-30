package il.co.idocare.useractions.cachers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

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

    public void cacheUserAction(UserActionEntity userActionEntity) {

        ContentValues userActionCV = new ContentValues(6);

        userActionCV.put(IDoCareContract.UserActions.COL_TIMESTAMP, userActionEntity.getTimestamp());

        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_TYPE, userActionEntity.getEntityType());

        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_ID, userActionEntity.getEntityId());

        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_PARAM, userActionEntity.getEntityParam());

        userActionCV.put(IDoCareContract.UserActions.COL_ACTION_TYPE, userActionEntity.getActionType());

        userActionCV.put(IDoCareContract.UserActions.COL_ACTION_PARAM, userActionEntity.getActionParam());

        mContentResolver.insert(
                IDoCareContract.UserActions.CONTENT_URI,
                userActionCV
        );
    }
}
