package il.co.idocare.networking;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

import javax.inject.Inject;

import il.co.idocare.authentication.MyAccountManager;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;

/**
 * This class aggregates functionality related to control of server synchronization (though it is
 * not responsible to actually perform the sync)
 */
public class ServerSyncController {

    private LoginStateManager mLoginStateManager;
    private MyAccountManager mMyAccountManager;
    private ContentResolverProxy mContentResolverProxy;

    @Inject
    public ServerSyncController(LoginStateManager loginStateManager,
                                MyAccountManager myAccountManager,
                                ContentResolverProxy contentResolverProxy) {
        mLoginStateManager = loginStateManager;
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
        Account acc = getActiveOrDummyAccount();

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        mContentResolverProxy.requestSync(acc, IDoCareContract.AUTHORITY, settingsBundle);
    }


    private Account getActiveOrDummyAccount() {
        Account account = mLoginStateManager.getActiveAccount();

        if (account != null) {
            return account;
        } else {
            mMyAccountManager.addDummyAccount();
            return mMyAccountManager.getDummyAccount();
        }
    }

}
