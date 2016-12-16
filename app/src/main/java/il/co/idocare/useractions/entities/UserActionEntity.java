package il.co.idocare.useractions.entities;

import il.co.idocare.contentproviders.IDoCareContract;

/**
 * This is a base class for objects that encapsulates information about user's action
 */
public class UserActionEntity {

    private final long mTimestamp;
    private final String mEntityType;
    private final String mEntityId;
    private final String mEntityParam;
    private final String mActionType;
    private final String mActionParam;

    public UserActionEntity(long timestamp,
                            String entityType,
                            String entityId,
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

    // Getters

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getEntityType() {
        return mEntityType;
    }

    public String getEntityId() {
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
