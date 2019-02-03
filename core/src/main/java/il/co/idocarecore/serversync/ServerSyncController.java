package il.co.idocarecore.serversync;

public interface ServerSyncController {
    void enableAutomaticSync();

    void disableAutomaticSync();

    void requestImmediateSync();

    void syncUserDataImmediate(String userId);
}
