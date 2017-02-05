package il.co.idocare.serversync;


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
