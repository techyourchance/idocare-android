package il.co.idocare.users;

import android.content.ContentResolver;
import android.database.Cursor;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.contentproviders.ContentProviderUtils;

import static il.co.idocare.contentproviders.IDoCareContract.*;

/**
 * Instances of this class can be used in order to retrieve information about users from
 * the cache.
 */
public class UsersRetriever {

    private ContentResolver mContentResolver;

    public UsersRetriever(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    /**
     * @return user's data, or null if no data for user ID was found in cache
     */
    @WorkerThread
    public @Nullable UserEntity getUserById(String userId) {
        String selection = Users.COL_USER_ID + " = ?";
        String[] selectionArgs = new String[] {userId};

        List<UserEntity> users = getUsersWithSelection(selection, selectionArgs);

        if (users.isEmpty()) {
            return null;
        } else {
            return users.get(0);
        }
    }

    @WorkerThread
    public List<UserEntity> getUsersByIds(List<String> userIds) {
        if (userIds.isEmpty()) {
            return new ArrayList<>(0);
        }

        ContentProviderUtils.SelectionAndSelectionArgsPair selectionsPair =
                ContentProviderUtils.getSelectionByColumnForListOfValues(
                        Users.COL_USER_ID, userIds);

        return getUsersWithSelection(selectionsPair.getSelection(), selectionsPair.getSelectionArgs());
    }

    private List<UserEntity> getUsersWithSelection(String selection, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(
                    Users.CONTENT_URI,
                    Users.PROJECTION_ALL,
                    selection,
                    selectionArgs,
                    Users.SORT_ORDER_DEFAULT);

            if (cursor != null && cursor.moveToFirst()) {

                List<UserEntity> results = new ArrayList<>(cursor.getCount());

                do {
                    results.add(createUserEntityFromCurrentCursorPosition(cursor));
                } while (cursor.moveToNext());

                return results;
            } else {
                return new ArrayList<>(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private UserEntity createUserEntityFromCurrentCursorPosition(Cursor cursor) {

        return UserEntity.newBuilder()
                .setUserId(cursor.getString(cursor.getColumnIndexOrThrow(Users.COL_USER_ID)))
                .setNickname(cursor.getString(cursor.getColumnIndexOrThrow(Users.COL_USER_NICKNAME)))
                .setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(Users.COL_USER_FIRST_NAME)))
                .setLastName(cursor.getString(cursor.getColumnIndexOrThrow(Users.COL_USER_LAST_NAME)))
                .setReputation(cursor.getInt(cursor.getColumnIndexOrThrow(Users.COL_USER_REPUTATION)))
                .setPictureUrl(cursor.getString(cursor.getColumnIndexOrThrow(Users.COL_USER_PICTURE)))
                .build();
    }

    @WorkerThread
    public List<String> getAllUniqueUsersIdsRelatedToRequests() {
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(
                    UniqueUserIds.CONTENT_URI,
                    UniqueUserIds.PROJECTION_ALL,
                    null,
                    null,
                    UniqueUserIds.SORT_ORDER_DEFAULT);

            if (cursor != null && cursor.moveToFirst()) {

                List<String> results = new ArrayList<>(cursor.getCount());

                do {
                    results.add(cursor.getString(cursor.getColumnIndexOrThrow(UniqueUserIds.COL_USER_ID)));
                } while (cursor.moveToNext());

                return results;
            } else {
                return new ArrayList<>(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

}
