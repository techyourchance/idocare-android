package il.co.idocare.serversync;

/**
 * This event will be posted to EventBus upon completion of a manual sync
 */
public class ManualSyncCompletedEvent {

    private long mManualSyncId;

    public ManualSyncCompletedEvent(long manualSyncId) {
        mManualSyncId = manualSyncId;
    }

    public long getManualSyncId() {
        return mManualSyncId;
    }
}
