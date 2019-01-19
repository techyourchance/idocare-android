package il.co.idocare.users;


import android.content.ContentResolver;
import android.content.ContentValues;
import androidx.annotation.WorkerThread;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import il.co.idocare.contentproviders.ContentProviderUtils;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.users.events.UserDataChangedEvent;
import il.co.idocare.utils.Logger;

@WorkerThread
public class UsersCacher {

    private static final String TAG = "UsersCacher";

    private final ContentResolver mContentResolver;
    private final EventBus mEventBus;
    private final Logger mLogger;

    public UsersCacher(ContentResolver contentResolver, EventBus eventBus, Logger logger) {
        mContentResolver = contentResolver;
        mEventBus = eventBus;
        mLogger = logger;
    }

    public void deleteAllUsers() {
        mLogger.d(TAG, "deleteAllUsers() called");
        mContentResolver.delete(
                IDoCareContract.Users.CONTENT_URI,
                null,
                null
        );
    }

    public void deleteAllUsersWithNonMatchingIds(List<String> usersIds) {
        mLogger.d(TAG, "deleteAllUsersWithNonMatchingIds() called");

        ContentProviderUtils.SelectionAndSelectionArgsPair selectionPair =
                ContentProviderUtils.getSelectionByColumnExceptListOfValues(
                        IDoCareContract.Users.COL_USER_ID,
                        usersIds
                );

        mContentResolver.delete(
                IDoCareContract.Users.CONTENT_URI,
                selectionPair.getSelection(),
                selectionPair.getSelectionArgs()
        );
    }

    public void updateOrInsertUserAndNotify(UserEntity user) {
        updateOrInsertUser(user);
        notifyUserDataChanged(user);
    }

    private void notifyUserDataChanged(UserEntity user) {
        mEventBus.post(new UserDataChangedEvent(user.getUserId()));
    }

    public void updateOrInsertUser(UserEntity user) {
        mLogger.d(TAG, "updateOrInsertUser() called; user ID: " + user.getUserId());
        // TODO: make operations atomic
        ContentValues cv = userToContentValues(user);

        int updateCount = mContentResolver.update(
                IDoCareContract.Users.CONTENT_URI,
                cv,
                IDoCareContract.Users.COL_USER_ID + " = ?",
                new String[] {user.getUserId()}
        );

        if (updateCount <= 0) {
            mContentResolver.insert(
                    IDoCareContract.Users.CONTENT_URI,
                    cv
            );
            mLogger.v(TAG, "new user inserted");
        } else {
            mLogger.v(TAG, "user updated");
        }
    }

    private ContentValues userToContentValues(UserEntity user) {
        ContentValues cv = new ContentValues(10);

        cv.put(IDoCareContract.Users.COL_USER_ID, user.getUserId());
        cv.put(IDoCareContract.Users.COL_USER_NICKNAME, user.getNickname());
        cv.put(IDoCareContract.Users.COL_USER_FIRST_NAME, user.getFirstName());
        cv.put(IDoCareContract.Users.COL_USER_LAST_NAME, user.getLastName());
        cv.put(IDoCareContract.Users.COL_USER_REPUTATION, user.getReputation());
        cv.put(IDoCareContract.Users.COL_USER_PICTURE, user.getPictureUrl());

        return cv;
    }

}
