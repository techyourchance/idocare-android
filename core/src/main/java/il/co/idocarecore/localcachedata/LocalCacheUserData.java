package il.co.idocarecore.localcachedata;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import il.co.idocarecore.contentproviders.IDoCareContract;
import il.co.idocarecore.datamodels.functional.UserItem;
import il.co.idocarecore.contentproviders.IDoCareContract;
import il.co.idocarecore.datamodels.functional.UserItem;

/**
 * This class represents a data of a single user stored in the local application's cache.
 */
public class LocalCacheUserData {


    private ContentResolver mContentResolver;
    private ContentObserver mContentObserver;
    private Uri mResourceUri;
    private LocalCacheDataChangeListener mListener;

    public LocalCacheUserData(@NonNull ContentResolver contentResolver, long userId) {
        if (userId == 0)
            throw new IllegalArgumentException("0 is not a valid user ID");

        mContentResolver = contentResolver;
        mResourceUri = ContentUris.withAppendedId(IDoCareContract.Users.CONTENT_URI, userId);
    }


    /**
     * Get user's information. This method might take some time to execute, therefore it should
     * not be called on UI thread!
     * @return user's info, or null if there is none
     */
    public UserItem getInfo() {
        Cursor cursor = null;
        UserItem result = null;
        try {
             cursor = mContentResolver.query(
                    mResourceUri,
                    IDoCareContract.Users.PROJECTION_ALL,
                    null,
                    null,
                    null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = UserItem.create(cursor);
            }
            return result;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Register a single listener that will be notified if user's data in a local cache changes
     * @param listener the listener to notify
     * @param handler if provided, notifications will be posted to this handler; if null - main (UI)
     *                handler will be used
     */
    public void setChangeListener(LocalCacheDataChangeListener listener, Handler handler) {
        if (listener != null) {
            unregisterContentObserver();
            mListener = listener;
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            registerContentObserver(handler);
        } else {
            unregisterContentObserver();
        }
    }

    /**
     * Should be used in order to cancel the currently executing call to {@link #getInfo()}. Will
     * cause {@link #getInfo()} to return null value immediately. This method might be called from
     * any thread.
     */
    public void cancel() {
        // TODO: should use cancellation signal in order to cancel load from sqlite (must be thread safe)
    }

    private void unregisterContentObserver() {
        if (mContentObserver != null)
            mContentResolver.unregisterContentObserver(mContentObserver);
    }

    private void registerContentObserver(Handler handler) {
        mContentObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                this.onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                mListener.onDataChanged();
            }
        };
        mContentResolver.registerContentObserver(mResourceUri, false, mContentObserver);
    }
}
