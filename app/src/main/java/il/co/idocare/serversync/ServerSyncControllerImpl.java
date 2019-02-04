package il.co.idocare.serversync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

import javax.inject.Inject;

import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.contentproviders.IDoCareContract;
import il.co.idocarecore.nonstaticproxies.ContentResolverProxy;
import il.co.idocarecore.serversync.ServerSyncController;

/**
 * This class aggregates functionality related to control of server synchronization (though it is
 * not responsible to actually perform the sync)
 */
public class ServerSyncControllerImpl implements ServerSyncController {

    private ContentResolverProxy mContentResolverProxy;
    private LoginStateManager mLoginStateManager;

    @Inject
    public ServerSyncControllerImpl(ContentResolverProxy contentResolverProxy,
                                    LoginStateManager loginStateManager) {
        mContentResolverProxy = contentResolverProxy;
        mLoginStateManager = loginStateManager;
    }

    @Override
    public void enableAutomaticSync() {
        mContentResolverProxy.setIsSyncable(getAccount(), IDoCareContract.AUTHORITY, 1);
        mContentResolverProxy.setSyncAutomatically(getAccount(), IDoCareContract.AUTHORITY, true);
    }

    @Override
    public void disableAutomaticSync() {
        mContentResolverProxy.setIsSyncable(getAccount(), IDoCareContract.AUTHORITY, 0);
    }

    @Override
    public void requestImmediateSync() {
        requestSync(getSyncImmediateBundle());
    }

    private void requestSync(Bundle syncExtras) {
        mContentResolverProxy.requestSync(getAccount(), IDoCareContract.AUTHORITY, syncExtras);
    }

    private Bundle getSyncImmediateBundle() {
        Bundle syncImmediateBundle = new Bundle();
        syncImmediateBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        syncImmediateBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        return syncImmediateBundle;
    }

    @Override
    public void syncUserDataImmediate(String userId) {
        Bundle syncExtras = getSyncImmediateBundle();
        syncExtras.putString(SyncAdapter.SYNC_EXTRAS_USER_ID, userId);
        requestSync(syncExtras);
    }

    private Account getAccount() {
        return mLoginStateManager.getAccountManagerAccount();
    }

}
