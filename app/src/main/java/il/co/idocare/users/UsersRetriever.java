package il.co.idocare.users;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.annotation.Nullable;

import il.co.idocare.contentproviders.IDoCareContract;

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
    public @Nullable UserEntity getUserById(String userId) {
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(
                    IDoCareContract.Users.CONTENT_URI,
                    IDoCareContract.Users.PROJECTION_ALL,
                    IDoCareContract.Users.COL_USER_ID + " = ?",
                    new String[] {userId},
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                return createUserEntityFromCurrentCursorPosition(cursor);
            } else {
                return null;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private UserEntity createUserEntityFromCurrentCursorPosition(Cursor cursor) {

        return UserEntity.newBuilder()
                .setUserId(cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.Users.COL_USER_ID)))
                .setNickname(cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.Users.COL_USER_NICKNAME)))
                .setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.Users.COL_USER_FIRST_NAME)))
                .setLastName(cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.Users.COL_USER_LAST_NAME)))
                .setReputation(cursor.getInt(cursor.getColumnIndexOrThrow(IDoCareContract.Users.COL_USER_REPUTATION)))
                .setPictureUrl(cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.Users.COL_USER_PICTURE)))
                .build();
    }


}
