package il.co.idocare.networking;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.serversync.ManualSyncCompletedEvent;
import il.co.idocare.serversync.SyncFailedException;
import il.co.idocare.serversync.syncers.RequestsSyncer;
import il.co.idocare.serversync.syncers.UserActionsSyncer;
import il.co.idocare.utils.Logger;

/**
 * Our sync adapter
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {


    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();

    /**
     * This constant should be used as a key to a long in syncExtras Bundle object in order
     * to perform partial sync and only get data of one particular user.
     */
    public static final String SYNC_EXTRAS_USER_ID = "SYNC_EXTRAS_USER_ID";

    /**
     * This constant should be used as a key to a long in syncExtras Bundle object - it will be
     * posted to EventBus as async completion token when sync completes.
     */
    public static final String SYNC_EXTRAS_MANUAL_SYNC_ID = "SYNC_EXTRAS_MANUAL_SYNC_ID";

    @Inject RequestsSyncer mRequestsSyncer;
    @Inject UserActionsSyncer mUserActionsSyncer;

    @Inject LoginStateManager mLoginStateManager;
    @Inject EventBus mEventBus;
    @Inject Logger mLogger;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(@NonNull Context context,
                       boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        mLogger.d(LOG_TAG, "onPerformSync() called; sync extras bundle:" + extras);

        try {
            mUserActionsSyncer.syncUserActions();

            mRequestsSyncer.syncAllRequests();

        } catch (SyncFailedException e) {
            e.printStackTrace();
            // TODO: what to do here?
        }

        if (extras.containsKey(SYNC_EXTRAS_MANUAL_SYNC_ID)) {
            notifyManualSyncCompleted(extras.getLong(SYNC_EXTRAS_MANUAL_SYNC_ID));
        }

        Log.d(LOG_TAG, "onPerformSync() returned");

    }


//    @Override
//    public void onPerformSync(Account account, Bundle extras, String authority,
//                              ContentProviderClient provider, SyncResult syncResult) {
//
//        mLogger.d(LOG_TAG, "onPerformSync() called; \"sync extras\" bundle:\n" + extras);
//
//        LoggedInUserEntity user = mLoginStateManager.getLoggedInUser();
//        String authToken = user != null ? user.getAuthToken() : null;
//        String userId = user != null ? user.getUserId() : null;
//
//        if (!TextUtils.hasMoreActionsToDispatch(userId) && !TextUtils.hasMoreActionsToDispatch(authToken)) {
//            DataUploader dataUploader =
//                    new DataUploader(userId, authToken, provider);
//
//            // This call will block until all local actions will be synchronized to the server
//            // and the respective ContentProvider will be updated
//            dataUploader.uploadAll();
//        } else {
//            mLogger.d(LOG_TAG, "no user ID or auth token - skipping data upload and using an open API."
//                    + "\nUser ID: " + userId + "\nAuth token: " + authToken);
//        }
//
//
//        ReverseGeocoderFactory reverseGeocoderFactory =
//                new OpenStreetMapsReverseGeocoderFactory();
//
//        LegacyServerResponseHandlerFactory serverResponseHandlerFactory =
//                new LegacySimpleServerResponseHandlerFactory(reverseGeocoderFactory);
//
//        DataDownloader dataDownloader =
//                new DataDownloader(userId, authToken, provider, serverResponseHandlerFactory);
//
//
//        if (extras.containsKey(SYNC_EXTRAS_USER_ID)) {
//            // perform partial sync of users' data
//            dataDownloader.downloadUserData(extras.getString(SYNC_EXTRAS_USER_ID));
//        } else {
//            // This call will block until all relevant data will be synchronized from the server
//            // and the respective ContentProvider will be updated
//            dataDownloader.downloadAll();
//        }
//
//        if (extras.containsKey(SYNC_EXTRAS_MANUAL_SYNC_ID)) {
//            notifyManualSyncCompleted(extras.getLong(SYNC_EXTRAS_MANUAL_SYNC_ID));
//        }
//
//        Log.d(LOG_TAG, "onPerformSync() returned");
//
//    }

    private void notifyManualSyncCompleted(long manualSyncId) {
        mEventBus.post(new ManualSyncCompletedEvent(manualSyncId));
    }


}
