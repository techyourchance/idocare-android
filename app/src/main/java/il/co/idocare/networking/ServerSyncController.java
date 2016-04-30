package il.co.idocare.networking;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import il.co.idocare.authentication.MyAccountManager;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;

/**
 * This class aggregates functionality related to control of server synchronization (though it is
 * not responsible to actually perform the sync)
 */
public class ServerSyncController {

    private MyAccountManager mMyAccountManager;
    private ContentResolverProxy mContentResolverProxy;

    @Inject
    public ServerSyncController(@NonNull MyAccountManager myAccountManager,
                                @NonNull ContentResolverProxy contentResolverProxy) {
        mMyAccountManager = myAccountManager;
        mContentResolverProxy = contentResolverProxy;
    }

    public void enableAutomaticSync() {
        Account acc = getActiveOrDummyAccount();
        mContentResolverProxy.setIsSyncable(acc, IDoCareContract.AUTHORITY, 1);
        mContentResolverProxy.setSyncAutomatically(acc, IDoCareContract.AUTHORITY, true);
    }

    public void disableAutomaticSync() {
        Account acc = getActiveOrDummyAccount();
        mContentResolverProxy.setIsSyncable(acc, IDoCareContract.AUTHORITY, 0);
    }

    public void requestImmediateSync() {
        requestSync(getSyncImmediateBundle());
    }

    private void requestSync(Bundle syncExtras) {
        Account acc = getActiveOrDummyAccount();
        mContentResolverProxy.requestSync(acc, IDoCareContract.AUTHORITY, syncExtras);
    }


    private Account getActiveOrDummyAccount() {
        Account account = mMyAccountManager.getActiveAccount();

        if (account != null) {
            return account;
        } else {
            return mMyAccountManager.getDummyAccount();
        }
    }

    private Bundle getSyncImmediateBundle() {
        Bundle syncImmediateBundle = new Bundle();
        syncImmediateBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        syncImmediateBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        return syncImmediateBundle;
    }

    public void syncUserDataImeediate(long userId) {
        Bundle syncExtras = getSyncImmediateBundle();
        syncExtras.putLong(SyncAdapter.SYNC_EXTRAS_USER_ID, userId);
        requestSync(syncExtras);
    }

}
