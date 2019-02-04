package il.co.idocare.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import il.co.idocare.IdcApplication;
import il.co.idocare.dependencyinjection.serversync.ServerSyncComponent;
import il.co.idocare.dependencyinjection.serversync.ServerSyncModule;
import il.co.idocare.serversync.SyncAdapter;

/**
 * Bound service to be used by the framework for SyncAdapter enablement
 */
public class SyncService extends Service {

    // Storage for an instance of the sync adapter
    private static SyncAdapter sSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();

    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                ServerSyncComponent serverSyncComponent = ((IdcApplication)getApplication())
                        .getApplicationComponent()
                        .newServerSyncComponent(new ServerSyncModule());
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
                serverSyncComponent.inject(sSyncAdapter);
            }
        }
    }
    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        synchronized (sSyncAdapterLock) {
            return sSyncAdapter.getSyncAdapterBinder();
        }
    }

}
