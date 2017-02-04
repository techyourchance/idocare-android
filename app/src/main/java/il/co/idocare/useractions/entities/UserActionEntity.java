package il.co.idocare.useractions.entities;

/**
 * This is a base class for objects that encapsulates information about user's action
 */
public class UserActionEntity {

    private final long mId;
    private final String mTimestamp;
    private final String mEntityType;
    private final String mEntityId;
    private final String mEntityParam;
    private final String mActionType;
    private final String mActionParam;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(UserActionEntity userAction) {
        return newBuilder()
                .setId(userAction.getActionId())
                .setTimestamp(userAction.getTimestamp())
                .setEntityType(userAction.getEntityType())
                .setEntityId(userAction.getEntityId())
                .setEntityParam(userAction.getEntityParam())
                .setActionType(userAction.getActionType())
                .setActionParam(userAction.getActionParam());
    }

    public UserActionEntity(long id,
                            String timestamp,
                            String entityType,
                            String entityId,
                            String entityParam,
                            String actionType,
                            String actionParam) {
        mId = id;
        mTimestamp = timestamp;
        mEntityType = entityType;
        mEntityId = entityId;
        mEntityParam = entityParam;
        mActionType = actionType;
        mActionParam = actionParam;
    }

    public UserActionEntity(String timestamp,
                            String entityType,
                            String entityId,
                            String entityParam,
                            String actionType,
                            String actionParam) {
        this(0, timestamp, entityType, entityId, entityParam, actionType, actionParam);
    }

    // Getters

    public long getActionId() {
        return mId;
    }

    public String getTimestamp() {
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
    
    public static class Builder {

        private long mId;
        private String mTimestamp;
        private String mEntityType;
        private String mEntityId;
        private String mEntityParam;
        private String mActionType;
        private String mActionParam;

        public Builder setId(long id) {
            mId = id;
            return this;
        }

        public Builder setTimestamp(String timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public Builder setEntityType(String entityType) {
            mEntityType = entityType;
            return this;
        }

        public Builder setEntityId(String entityId) {
            mEntityId = entityId;
            return this;
        }

        public Builder setEntityParam(String entityParam) {
            mEntityParam = entityParam;
            return this;
        }

        public Builder setActionType(String actionType) {
            mActionType = actionType;
            return this;
        }

        public Builder setActionParam(String actionParam) {
            mActionParam = actionParam;
            return this;
        }

        public UserActionEntity build() {
            return new UserActionEntity(
                    mId,
                    mTimestamp,
                    mEntityType,
                    mEntityId,
                    mEntityParam,
                    mActionType,
                    mActionParam
            );
        }
    }
}
