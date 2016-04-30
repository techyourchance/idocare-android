package il.co.idocare.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.localcachedata.LocalCacheDataChangeListener;
import il.co.idocare.localcachedata.LocalCacheUserData;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.serversync.ServerSyncUserData;

/**
 * This Loader is responsible for loading user's data. It searches for the data in the local cache,
 * and if it is not found there - it will get the data from the server into cache and then load it.
 */
public class UserInfoLoader extends AsyncTaskLoader<UserItem> implements LocalCacheDataChangeListener {

    @NonNull
    private final ServerSyncController mServerSyncController;
    private LocalCacheUserData mLocalCacheUserData;
    private UserItem mUserItem;
    private long mUserId;

    public UserInfoLoader(@NonNull Context context,
                          @NonNull ContentResolver contentResolver,
                          @NonNull ServerSyncController serverSyncController,
                          long userId) {
        super(context);
        if (userId == 0)
            throw new IllegalArgumentException("0 is not a valid user ID");
        mServerSyncController = serverSyncController;
        mUserId = userId;
        mLocalCacheUserData = new LocalCacheUserData(contentResolver, userId);
    }

    @Override
    public UserItem loadInBackground() {
        mUserItem = mLocalCacheUserData.getInfo();

        if (mUserItem != null) return mUserItem;

        mServerSyncController.syncUserDataImeediate(mUserId);

        /*
        Return null because we assume that when user's data will be synced to local cache,
        onDataChanged() will be called and loadInBackground will be executed again with the correct data.
         */
        return null;
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();
        mLocalCacheUserData.cancel();
    }

    @Override
    public void deliverResult(UserItem userItem) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            return;
        }

        if (isStarted()) {
            super.deliverResult(userItem);
        }
    }


    @Override
    protected void onStartLoading() {
        mLocalCacheUserData.setChangeListener(this, new Handler(Looper.getMainLooper()));
        if (mUserItem != null) {
            deliverResult(mUserItem);
        }
        if (takeContentChanged() || mUserItem == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }



    @Override
    public void onCanceled(UserItem userItem) {
        // nothing to do
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        mLocalCacheUserData.setChangeListener(null, null);

        mUserItem = null;
    }

    @Override
    public void onDataChanged() {
        onContentChanged();
    }
}
