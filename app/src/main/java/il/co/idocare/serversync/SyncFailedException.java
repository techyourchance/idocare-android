package il.co.idocarecore.serversync;


public class SyncFailedException extends RuntimeException {

    public SyncFailedException() {
    }

    public SyncFailedException(String message) {
        super(message);
    }

    public SyncFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyncFailedException(Throwable cause) {
        super(cause);
    }
}
