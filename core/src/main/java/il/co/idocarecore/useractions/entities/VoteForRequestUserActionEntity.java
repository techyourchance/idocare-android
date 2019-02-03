package il.co.idocarecore.useractions.entities;

import il.co.idocarecore.contentproviders.IDoCareContract;
import il.co.idocarecore.contentproviders.IDoCareContract;

/**
 * This object represent user's vote for specific request
 */

public class VoteForRequestUserActionEntity extends UserActionEntity {

    public static final int VOTE_UP_CREATED = 1;
    public static final int VOTE_DOWN_CREATED = 2;
    public static final int VOTE_UP_CLOSED = 3;
    public static final int VOTE_DOWN_CLOSED = 4;

    private final int mVoteType;

    public static VoteForRequestUserActionEntity fromUserAction(UserActionEntity userAction) {
        return new VoteForRequestUserActionEntity(
                userAction.getDatetime(),
                userAction.getEntityId(),
                getVoteTypeFromUserAction(userAction)
        );
    }

    public VoteForRequestUserActionEntity(String timestamp,
                                          String requestId,
                                          int voteType) {
        super(timestamp,
              IDoCareContract.UserActions.ENTITY_TYPE_REQUEST,
              requestId,
              getEntityParam(voteType),
              IDoCareContract.UserActions.ACTION_TYPE_VOTE_FOR_REQUEST,
              getActionParam(voteType)
        );
        mVoteType = voteType;
    }

    /**
     * @return value corresponding to one of the VOTE_ constants defined in this class
     */
    public int getVoteType() {
        return mVoteType;
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

    private static int getVoteTypeFromUserAction(UserActionEntity userAction) {
        String entityParam = userAction.getEntityParam();
        String actionParam = userAction.getActionParam();
        switch (entityParam) {
            case IDoCareContract.UserActions.ENTITY_PARAM_REQUEST_CREATED:
                switch (actionParam) {
                    case "1":
                        return VOTE_UP_CREATED;
                    case "-1":
                        return VOTE_DOWN_CREATED;
                    default:
                        throw new RuntimeException();
                }
            case IDoCareContract.UserActions.ENTITY_PARAM_REQUEST_CLOSED:
                switch (actionParam) {
                    case "1":
                        return VOTE_UP_CLOSED;
                    case "-1":
                        return VOTE_DOWN_CLOSED;
                    default:
                        throw new RuntimeException();
                }
            default:
                throw new RuntimeException();
        }
    }

    public int getVoteScore() {
        return Integer.valueOf(getActionParam());
    }

    public String getCreatedOrClosed() {
        return getEntityParam();
    }
}
