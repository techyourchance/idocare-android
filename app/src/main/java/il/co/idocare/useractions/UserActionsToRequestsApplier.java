package il.co.idocare.useractions;

import android.support.annotation.NonNull;

import il.co.idocare.contentproviders.IDoCareContract.UserActions;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.useractions.entities.CloseRequestUserActionEntity;
import il.co.idocare.useractions.entities.CreateRequestUserActionEntity;
import il.co.idocare.useractions.entities.PickUpRequestUserActionEntity;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.useractions.entities.VoteForRequestUserActionEntity;

/**
 * This class handles merging information about user's actions with information about raw
 * requests, thus allowing to produce a "view" of requests which take into account possible
 * local modifications by the user.
 */

public class UserActionsToRequestsApplier {

    /**
     * @return true if the provided user action is related to requests
     */
    public boolean isUserActionAffectingRequest(@NonNull UserActionEntity userAction) {
        return userAction.getEntityType() != null
                && userAction.getEntityType().equals(UserActions.ENTITY_TYPE_REQUEST);
    }

    /**
     *
     * @param userAction the user action which should be applied to request
     * @param requestEntity the request on which to apply the user action
     * @return new request which is the merge of the provided request and the information from
     *         user action
     * @throws IllegalArgumentException if the provided user action is not applicable to the
     *                                  provided request
     */
    public RequestEntity applyUserActionToRequest(@NonNull UserActionEntity userAction,
                                                  @NonNull RequestEntity requestEntity) {
        if (!userAction.getEntityType().equals(UserActions.ENTITY_TYPE_REQUEST)
                || !userAction.getEntityId().equals(requestEntity.getId())) {
            throw new IllegalArgumentException("user action is not related to the request");
        }

        if (userAction.getActionType().equals(UserActions.ACTION_TYPE_CLOSE_REQUEST)) {
            return closeRequest(CloseRequestUserActionEntity.fromUserAction(userAction), requestEntity);
        } else if (userAction.getActionType().equals(UserActions.ACTION_TYPE_PICKUP_REQUEST)) {
            return pickUpRequest(PickUpRequestUserActionEntity.fromUserAction(userAction), requestEntity);
        } else if (userAction.getActionType().equals(UserActions.ACTION_TYPE_CREATE_REQUEST)) {
            // this action is just a "marker" - the actual data has already been written to requests cache
            return requestEntity;
        } else if (userAction.getActionType().equals(UserActions.ACTION_TYPE_VOTE_FOR_REQUEST)) {
            return voteForRequest(VoteForRequestUserActionEntity.fromUserAction(userAction), requestEntity);
        } else {
            throw new RuntimeException("invalid user action: " + userAction);
        }
    }


    private RequestEntity closeRequest(CloseRequestUserActionEntity closeRequestUserAction,
                                       RequestEntity requestEntity) {

        if (requestEntity.getClosedBy() != null && !requestEntity.getClosedBy().isEmpty()) {
            throw new IllegalArgumentException("request already closed");
        }
        if (requestEntity.getPickedUpBy() == null || requestEntity.getPickedUpBy().isEmpty()) {
            throw new IllegalArgumentException("request is not picked up");
        }
        if (!requestEntity.getPickedUpBy().equals(closeRequestUserAction.getClosedByUserId())) {
            throw new IllegalArgumentException("closing user must be the same who picked up");
        }

        return RequestEntity.getBuilder(requestEntity)
                .setClosedBy(closeRequestUserAction.getClosedByUserId())
                .setClosedAt(String.valueOf(closeRequestUserAction.getTimestamp()))
                .setClosedComment(closeRequestUserAction.getClosedComment())
                .setClosedPictures(closeRequestUserAction.getClosedPictures())
                .build();
    }

    private RequestEntity pickUpRequest(@NonNull PickUpRequestUserActionEntity pickUpRequestUserAction,
                                        @NonNull RequestEntity requestEntity) {
        if (requestEntity.isPickedUp()) {
            throw new IllegalArgumentException("request already picked up");
        }

        String pickedUpByUserId = pickUpRequestUserAction.getPickedUpByUserId();

        return RequestEntity.getBuilder(requestEntity)
                .setPickedUpBy(pickedUpByUserId)
                .setPickedUpAt(String.valueOf(pickUpRequestUserAction.getTimestamp()))
                .build();
    }


    private RequestEntity voteForRequest(@NonNull VoteForRequestUserActionEntity voteForRequestUserAction,
                                         @NonNull RequestEntity requestEntity) {
        int voteType = voteForRequestUserAction.getVoteType();

        if ((voteType == VoteForRequestUserActionEntity.VOTE_UP_CLOSED
                || voteType == VoteForRequestUserActionEntity.VOTE_DOWN_CLOSED)
                && !requestEntity.isClosed()) {
            throw new IllegalArgumentException("cant vote for closed on non-closed request");
        }

        RequestEntity.RequestEntityBuilder requestEntityBuilder = RequestEntity.getBuilder(requestEntity);
        
        switch (voteType) {
            case VoteForRequestUserActionEntity.VOTE_UP_CREATED:
                requestEntityBuilder.setCreatedVotes(requestEntity.getCreatedVotes() + 1);
                break;
            case VoteForRequestUserActionEntity.VOTE_DOWN_CREATED:
                requestEntityBuilder.setCreatedVotes(requestEntity.getCreatedVotes() - 1);
                break;
            case VoteForRequestUserActionEntity.VOTE_UP_CLOSED:
                requestEntityBuilder.setClosedVotes(requestEntity.getClosedVotes() + 1);
                break;
            case VoteForRequestUserActionEntity.VOTE_DOWN_CLOSED:
                requestEntityBuilder.setClosedVotes(requestEntity.getClosedVotes() - 1);
                break;
            default:
                throw new RuntimeException("invalid vote type: " + voteType);
        }

        return requestEntityBuilder.build();
    }
}
