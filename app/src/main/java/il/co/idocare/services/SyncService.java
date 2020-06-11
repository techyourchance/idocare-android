package il.co.idocare.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import javax.inject.Inject;

import dagger.hilt.EntryPoint;
import dagger.hilt.android.AndroidEntryPoint;
import il.co.idocare.IdcApplication;
import il.co.idocare.dependencyinjection.serversync.ServerSyncModule;
import il.co.idocare.serversync.SyncAdapter;

/**
 * Bound service to be used by the framework for SyncAdapter enablement
 */
@AndroidEntryPoint
public class SyncService extends Service {

    @Inject SyncAdapter sSyncAdapter;

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
