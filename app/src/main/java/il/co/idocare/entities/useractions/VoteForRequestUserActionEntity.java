package il.co.idocare.entities.useractions;

import il.co.idocare.contentproviders.IDoCareContract;

/**
 * This object represent user's vote for specific request
 */

public class VoteForRequestUserActionEntity extends UserActionEntity {

    public static final int VOTE_UP_CREATED = 1;
    public static final int VOTE_DOWN_CREATED = 2;
    public static final int VOTE_UP_CLOSED = 3;
    public static final int VOTE_DOWN_CLOSED = 4;

    public VoteForRequestUserActionEntity(long timestamp,
                                          long requestId,
                                          int voteType) {
        super(timestamp,
                IDoCareContract.UserActions.ENTITY_TYPE_REQUEST,
                requestId,
                getEntityParam(voteType),
                IDoCareContract.UserActions.ACTION_TYPE_VOTE_FOR_REQUEST,
                getActionParam(voteType)
        );
    }

    private static String getEntityParam(int voteType) {
        switch (voteType) {
            case VOTE_UP_CREATED:
            case VOTE_DOWN_CREATED:
                return IDoCareContract.UserActions.ENTITY_PARAM_REQUEST_CREATED;
            case VOTE_UP_CLOSED:
            case VOTE_DOWN_CLOSED:
                return IDoCareContract.UserActions.ENTITY_PARAM_REQUEST_CLOSED;
            default:
                throw new IllegalArgumentException("vote type must be one of the valid VOTE_TYPE constants");
        }
    }

    private static String getActionParam(int voteType) {
        switch (voteType) {
            case VOTE_UP_CREATED:
            case VOTE_UP_CLOSED:
                return "1";
            case VOTE_DOWN_CREATED:
            case VOTE_DOWN_CLOSED:
                return "-1";
            default:
                throw new IllegalArgumentException("vote type must be one of the valid VOTE_TYPE constants");
        }
    }
}
