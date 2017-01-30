package il.co.idocare.useractions;

import android.support.annotation.NonNull;

import java.util.List;

import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.useractions.entities.CloseRequestUserActionEntity;
import il.co.idocare.useractions.entities.CreateRequestUserActionEntity;
import il.co.idocare.useractions.entities.PickUpRequestUserActionEntity;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.useractions.entities.VoteForRequestUserActionEntity;

/**
 * This class assists in instantiation of properly configured
 * {@link UserActionEntity} objects.
 */

public class UserActionEntityFactory {


    protected String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    public VoteForRequestUserActionEntity newVoteUpForRequestCreated(String requestId) {
        return new VoteForRequestUserActionEntity(
                getTimestamp(),
                requestId,
                VoteForRequestUserActionEntity.VOTE_UP_CREATED);
    }


    public VoteForRequestUserActionEntity newVoteDownForRequestCreated(String requestId) {
        return new VoteForRequestUserActionEntity(
                getTimestamp(),
                requestId,
                VoteForRequestUserActionEntity.VOTE_DOWN_CREATED);
    }

    public VoteForRequestUserActionEntity newVoteUpForRequestClosed(String requestId) {
        return new VoteForRequestUserActionEntity(
                getTimestamp(),
                requestId,
                VoteForRequestUserActionEntity.VOTE_UP_CLOSED);
    }

    public VoteForRequestUserActionEntity newVoteDownForRequestClosed(String requestId) {
        return new VoteForRequestUserActionEntity(
                getTimestamp(),
                requestId,
                VoteForRequestUserActionEntity.VOTE_DOWN_CLOSED);
    }

    public PickUpRequestUserActionEntity newPickUpRequest(String requestId, String pickedUpByUserId) {
        return new PickUpRequestUserActionEntity(
                getTimestamp(),
                requestId,
                pickedUpByUserId);
    }

    public CloseRequestUserActionEntity newCloseRequest(@NonNull String requestId,
                                                        @NonNull String closedByUserId,
                                                        @NonNull String closedComment,
                                                        @NonNull List<String> closedPictures) {
        return new CloseRequestUserActionEntity(
                getTimestamp(),
                requestId,
                closedByUserId,
                closedComment,
                closedPictures);
    }

    public UserActionEntity newCreateRequest(String requestId, String createdByUserId) {
        return new CreateRequestUserActionEntity(
                String.valueOf(getTimestamp()),
                requestId,
                createdByUserId);
    }
}
