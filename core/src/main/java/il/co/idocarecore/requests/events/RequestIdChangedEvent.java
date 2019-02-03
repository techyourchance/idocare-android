package il.co.idocarecore.requests.events;

/**
 * This event will be posted to event bus whenever request ID changed (due to sync of a newly
 * created request to the server)
 */
public class RequestIdChangedEvent {
    private final String mOldId;
    private final String mNewId;

    public RequestIdChangedEvent(String oldId, String newId) {
        mOldId = oldId;
        mNewId = newId;
    }

    public String getNewId() {
        return mNewId;
    }

    public String getOldId() {
        return mOldId;
    }
}
