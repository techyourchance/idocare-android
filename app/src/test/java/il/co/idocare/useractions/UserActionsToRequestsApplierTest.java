package il.co.idocare.useractions;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import il.co.idocare.requests.RequestEntity;
import il.co.idocare.testdoubles.entities.RequestEntityProvider;
import il.co.idocare.useractions.entities.UserActionEntity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class UserActionsToRequestsApplierTest {

    private static final String DEFAULT_TIMESTAMP = "1000";
    private UserActionsToRequestsApplier SUT;

    private UserActionEntityFactory mUserActionEntityFactory = new UserActionEntityFactory() {
        @Override
        protected String getTimestamp() {
            return DEFAULT_TIMESTAMP;
        }
    };

    @Before
    public void setup() throws Exception {
        SUT = new UserActionsToRequestsApplier();
    }

    @Test
    public void applyUserActionToRequest_voteUpForCreatedUserAction_requestAffected() throws Exception {
        // Arrange
        RequestEntity requestEntity = RequestEntityProvider.getClosedRequestEntity();
        UserActionEntity voteUpCreatedEntity =
                mUserActionEntityFactory.newVoteUpForRequestCreated(requestEntity.getId());
        // Act
        RequestEntity resultRequestEntity = SUT.applyUserActionToRequest(voteUpCreatedEntity, requestEntity);
        // Assert
        assertThat(resultRequestEntity.getCreatedVotes(), is(RequestEntityProvider.REQUEST_CREATED_VOTES + 1));
    }

    @Test
    public void applyUserActionToRequest_voteDownForCreatedUserAction_requestAffected() throws Exception {
        // Arrange
        RequestEntity requestEntity = RequestEntityProvider.getClosedRequestEntity();
        UserActionEntity voteDownCreatedEntity =
                mUserActionEntityFactory.newVoteDownForRequestCreated(requestEntity.getId());
        // Act
        RequestEntity resultRequestEntity = SUT.applyUserActionToRequest(voteDownCreatedEntity, requestEntity);
        // Assert
        assertThat(resultRequestEntity.getCreatedVotes(), is(RequestEntityProvider.REQUEST_CREATED_VOTES -1));
    }


    @Test
    public void applyUserActionToRequest_voteUpForClosedUserAction_requestAffected() throws Exception {
        // Arrange
        RequestEntity requestEntity = RequestEntityProvider.getClosedRequestEntity();
        UserActionEntity voteUpClosedEntity =
                mUserActionEntityFactory.newVoteUpForRequestClosed(requestEntity.getId());
        // Act
        RequestEntity resultRequestEntity = SUT.applyUserActionToRequest(voteUpClosedEntity, requestEntity);
        // Assert
        assertThat(resultRequestEntity.getClosedVotes(), is(RequestEntityProvider.REQUEST_CLOSED_VOTES + 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyUserActionToRequest_voteUpForClosedUserActionOnNonClosedRequest_exceptionThrown() {
        // Arrange
        RequestEntity requestEntity = RequestEntityProvider.getPickedUpRequestEntity();
        UserActionEntity voteDownClosedEntity =
                mUserActionEntityFactory.newVoteUpForRequestClosed(requestEntity.getId());
        // Act
        SUT.applyUserActionToRequest(voteDownClosedEntity, requestEntity);
        // Assert
    }

    @Test
    public void applyUserActionToRequest_voteDownForClosedUserAction_requestAffected() throws Exception {
        // Arrange
        RequestEntity requestEntity = RequestEntityProvider.getClosedRequestEntity();
        UserActionEntity voteDownClosedEntity =
                mUserActionEntityFactory.newVoteDownForRequestClosed(requestEntity.getId());
        // Act
        RequestEntity resultRequestEntity = SUT.applyUserActionToRequest(voteDownClosedEntity, requestEntity);
        // Assert
        assertThat(resultRequestEntity.getClosedVotes(), is(RequestEntityProvider.REQUEST_CLOSED_VOTES - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyUserActionToRequest_voteDownForClosedUserActionOnNonClosedRequest_exceptionThrown() {
        // Arrange
        RequestEntity requestEntity = RequestEntityProvider.getPickedUpRequestEntity();
        UserActionEntity voteDownClosedEntity =
                mUserActionEntityFactory.newVoteDownForRequestClosed(requestEntity.getId());
        // Act
        SUT.applyUserActionToRequest(voteDownClosedEntity, requestEntity);
        // Assert
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyUserActionToRequest_userActionDoesNotAffectRequestsInGeneral_exceptionThrown() {
        // Arrange
        RequestEntity requestEntity = RequestEntityProvider.getClosedRequestEntity();
        UserActionEntity dummyUserAction = new UserActionEntity("1000", "bla", "1", "blabla", "bla", "bla");
        // Act
        SUT.applyUserActionToRequest(dummyUserAction, requestEntity);
        // Assert
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyUserActionToRequest_userActionAffectsOtherRequest_exceptionThrown() {
        // Arrange
        RequestEntity requestEntity = RequestEntityProvider.getClosedRequestEntity();
        UserActionEntity dummyUserAction =
                mUserActionEntityFactory.newVoteUpForRequestCreated(requestEntity.getId() + 1);
        // Act
        SUT.applyUserActionToRequest(dummyUserAction, requestEntity);
        // Assert
    }

    @Test
    public void applyUserActionToRequest_pickUpRequestUserActionOnNewRequest_requestAffected() {
        // Arrange
        String userId = "testUserId";
        RequestEntity originalRequestEntity = RequestEntityProvider.getNewRequestEntity();
        UserActionEntity pickupUserAction =
                mUserActionEntityFactory.newPickUpRequest(originalRequestEntity.getId(), userId);
        // Act
        RequestEntity resultRequestEntity = SUT.applyUserActionToRequest(pickupUserAction, originalRequestEntity);
        // Assert
        assertThat(resultRequestEntity.getPickedUpBy(), is(userId));
        assertThat(resultRequestEntity.getPickedUpAt(), is(DEFAULT_TIMESTAMP));
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyUserActionToRequest_pickUpRequestUserActionOnPickedUpRequest_exceptionThrown() {
        // Arrange
        String userId = "testUserId";
        RequestEntity requestEntity = RequestEntityProvider.getPickedUpRequestEntity();
        UserActionEntity pickupUserAction =
                mUserActionEntityFactory.newPickUpRequest(requestEntity.getId(), userId);
        // Act
        SUT.applyUserActionToRequest(pickupUserAction, requestEntity);
        // Assert
    }


    @Test
    public void applyUserActionToRequest_closeRequestUserActionOnPickedUpRequest_requestAffected() {
        // Arrange
        String userId = RequestEntityProvider.REQUEST_PICKED_UP_BY;
        String closedComment = "closed comment";
        List<String> closedPictures = Arrays.asList("picture1", "picture2");

        RequestEntity originalRequestEntity = RequestEntityProvider.getPickedUpRequestEntity();
        UserActionEntity closeUserAction = mUserActionEntityFactory.newCloseRequest(
                originalRequestEntity.getId(), userId, closedComment, closedPictures);
        // Act
        RequestEntity resultRequestEntity = SUT.applyUserActionToRequest(closeUserAction, originalRequestEntity);
        // Assert
        assertThat(resultRequestEntity.getClosedBy(), is(userId));
        assertThat(resultRequestEntity.getClosedAt(), is(DEFAULT_TIMESTAMP));
        assertThat(resultRequestEntity.getClosedComment(), is(closedComment));
        assertEquals(resultRequestEntity.getClosedPictures(), closedPictures);
        assertThat(resultRequestEntity.getClosedVotes(), is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyUserActionToRequest_closeRequestUserActionOnNewRequest_exceptionThrown() {
        // Arrange
        String userId = "testUserId";
        String closedComment = "closed comment";
        List<String> closedPictures = Arrays.asList("picture1", "picture2");

        RequestEntity requestEntity = RequestEntityProvider.getNewRequestEntity();
        UserActionEntity closeUserAction = mUserActionEntityFactory.newCloseRequest(
                requestEntity.getId(), userId, closedComment, closedPictures);
        // Act
        SUT.applyUserActionToRequest(closeUserAction, requestEntity);
        // Assert
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyUserActionToRequest_closeRequestUserActionOnClosedRequest_exceptionThrown() {
        // Arrange
        String userId = "testUserId";
        String closedComment = "closed comment";
        List<String> closedPictures = Arrays.asList("picture1", "picture2");

        RequestEntity requestEntity = RequestEntityProvider.getClosedRequestEntity();
        UserActionEntity closeUserAction = mUserActionEntityFactory.newCloseRequest(
                requestEntity.getId(), userId, closedComment, closedPictures);
        // Act
        SUT.applyUserActionToRequest(closeUserAction, requestEntity);
        // Assert
    }

    @Test
    public void applyUserActionToRequest_createRequestUserAction_sameRequestReturned() {
        // Arrange
        String userId = "testUserId";
        RequestEntity originalRequestEntity = RequestEntityProvider.getNewRequestEntity();
        UserActionEntity pickupUserAction =
                mUserActionEntityFactory.newCreateRequest(originalRequestEntity.getId(), userId);
        // Act
        RequestEntity resultRequestEntity = SUT.applyUserActionToRequest(pickupUserAction, originalRequestEntity);
        // Assert
        assertTrue(resultRequestEntity == originalRequestEntity);
    }
}