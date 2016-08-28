package il.co.idocare.entities;

/**
 * This object encapsulates information about user's action
 */
public class UserActionEntity {

    private final long mTimestamp;
    private final String mEntityType;
    private final long mEntityId;
    private final String mEntityParam;
    private final String mActionType;
    private final String mActionParam;

    public UserActionEntity(long timestamp,
                            String entityType,
                            long entityId,
                            String entityParam,
                            String actionType,
                            String actionParam) {
        mTimestamp = timestamp;
        mEntityType = entityType;
        mEntityId = entityId;
        mEntityParam = entityParam;
        mActionType = actionType;
        mActionParam = actionParam;
    }


    public long getTimestamp() {
        return mTimestamp;
    }

    public String getEntityType() {
        return mEntityType;
    }

    public long getEntityId() {
        return mEntityId;
    }

    public String getEntityParam() {
        return mEntityParam;
    }

    public String getActionType() {
        return mActionType;
    }

    public String getActionParam() {
        return mActionParam;
    }
}
