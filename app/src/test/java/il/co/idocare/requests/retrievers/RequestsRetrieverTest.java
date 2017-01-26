package il.co.idocare.requests.retrievers;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import il.co.idocare.requests.RequestEntity;
import il.co.idocare.testdoubles.entities.RequestEntityProvider;
import il.co.idocare.useractions.UserActionEntityFactory;
import il.co.idocare.useractions.UserActionsToRequestsApplier;
import il.co.idocare.useractions.entities.CloseRequestUserActionEntity;
import il.co.idocare.useractions.entities.PickUpRequestUserActionEntity;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.useractions.entities.VoteForRequestUserActionEntity;
import il.co.idocare.useractions.retrievers.UserActionsRetriever;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestsRetrieverTest {

    private static final String TEST_USER_ID = "test_user_id";

    @Mock RawRequestRetriever mRawRequestRetrieverMock;
    @Mock UserActionsRetriever mUserActionsRetrieverMock;
    UserActionsToRequestsApplier mUserActionsToRequestsApplier = new UserActionsToRequestsApplier();

    UserActionEntityFactory mUserActionEntityFactory = new UserActionEntityFactory();

    RequestsRetriever SUT;

    @Before
    public void setup() throws Exception {
        SUT = new RequestsRetriever(mRawRequestRetrieverMock, mUserActionsRetrieverMock,
                mUserActionsToRequestsApplier);



    }

    @Test
    public void getRequestsAssignedToUser_allUserActionEffectsPresent() {
        // Arrange

        RequestEntity initiallyClosedRequest = RequestEntityProvider.getClosedRequestEntity();
        RequestEntity initiallyNewRequest = RequestEntityProvider.getNewRequestEntity();
        RequestEntity initiallyPickedUpRequest = RequestEntity
                .getBuilder(RequestEntityProvider.getPickedUpRequestEntity())
                .setPickedUpBy(TEST_USER_ID)
                .build();

        List<RequestEntity> rawRequests = new ArrayList<>(3);
        rawRequests.add(initiallyClosedRequest);
        rawRequests.add(initiallyNewRequest);
        rawRequests.add(initiallyPickedUpRequest);

        CloseRequestUserActionEntity closePickedUp = mUserActionEntityFactory.newCloseRequest(
                initiallyPickedUpRequest.getId(), TEST_USER_ID, "closed_comment", Arrays.asList("one", "two"));
        PickUpRequestUserActionEntity pickUpNew =
                mUserActionEntityFactory.newPickUpRequest(initiallyNewRequest.getId(), TEST_USER_ID);
        VoteForRequestUserActionEntity voteForClosed =
                mUserActionEntityFactory.newVoteUpForRequestClosed(initiallyClosedRequest.getId());

        List<UserActionEntity> userActions = new ArrayList<>(3);
        userActions.add(voteForClosed);
        userActions.add(pickUpNew);
        userActions.add(closePickedUp);

        when(mRawRequestRetrieverMock.getRequestsAssignedToUser(TEST_USER_ID)).thenReturn(rawRequests);
        when(mUserActionsRetrieverMock.getAllUserActions()).thenReturn(userActions);

        // Act
        List<RequestEntity> resultRequests = SUT.getRequestsAssignedToUser(TEST_USER_ID);

        // Assert
        assertNotNull(resultRequests);
        assertThat(resultRequests.size(), is(3));

        RequestEntity updatedInitiallyNewRequest = getRequestById(resultRequests, initiallyNewRequest.getId());
        assertNotNull(updatedInitiallyNewRequest);
        assertRequestsEqual(updatedInitiallyNewRequest, RequestEntity.getBuilder(initiallyNewRequest)
                .setPickedUpBy(pickUpNew.getPickedUpByUserId())
                .setPickedUpAt(pickUpNew.getPickedUpAt())
                .build());

        RequestEntity updatedInitiallyPickedUpRequest = getRequestById(resultRequests, initiallyPickedUpRequest.getId());
        assertNotNull(updatedInitiallyPickedUpRequest);
        assertRequestsEqual(updatedInitiallyPickedUpRequest, RequestEntity.getBuilder(initiallyPickedUpRequest)
                .setClosedBy(closePickedUp.getClosedByUserId())
                .setClosedAt(closePickedUp.getClosedAt())
                .setClosedComment(closePickedUp.getClosedComment())
                .setClosedPictures(closePickedUp.getClosedPictures())
                .setClosedVotes(0)
                .build());

        RequestEntity updatedInitiallyClosedRequest = getRequestById(resultRequests, initiallyClosedRequest.getId());
        assertNotNull(updatedInitiallyClosedRequest);
        assertRequestsEqual(updatedInitiallyClosedRequest, RequestEntity.getBuilder(initiallyClosedRequest)
                .setClosedVotes(initiallyClosedRequest.getClosedVotes() + 1)
                .build());

    }

    private void assertRequestsEqual(RequestEntity request1, RequestEntity request2) {
        assertThat(request1.getId(), is(request2.getId()));
        assertThat(request1.getCreatedBy(), is(request2.getCreatedBy()));
        assertThat(request1.getCreatedAt(), is(request2.getCreatedAt()));
        assertThat(request1.getCreatedComment(), is(request2.getCreatedComment()));
        assertThat(request1.getCreatedVotes(), is(request2.getCreatedVotes()));
        assertEquals(request1.getCreatedPictures(), request2.getCreatedPictures());
        assertThat(request1.getLatitude(), is(request2.getLatitude()));
        assertThat(request1.getLongitude(), is(request2.getLongitude()));
        assertThat(request1.getLocation(), is(request2.getLocation()));
        assertThat(request1.getPickedUpBy(), is(request2.getPickedUpBy()));
        assertThat(request1.getPickedUpAt(), is(request2.getPickedUpAt()));
        assertThat(request1.getClosedBy(), is(request2.getClosedBy()));
        assertThat(request1.getClosedAt(), is(request2.getClosedAt()));
        assertThat(request1.getClosedComment(), is(request2.getClosedComment()));
        assertThat(request1.getClosedVotes(), is(request2.getClosedVotes()));
        assertEquals(request1.getClosedPictures(), request2.getClosedPictures());
        assertThat(request1.getId(), is(request2.getId()));
        assertThat(request1.getId(), is(request2.getId()));
    }

    private RequestEntity getRequestById(@NonNull List<RequestEntity> requests, String id) {
        for (RequestEntity requestEntity : requests) {
            if (requestEntity.getId().equals(id)) {
                return requestEntity;
            }
        }
        return null;
    }



}