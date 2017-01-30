package il.co.idocare.requests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.requests.cachers.RequestsCacher;
import il.co.idocare.requests.retrievers.RequestsRetriever;
import il.co.idocare.testdoubles.entities.RequestEntityProvider;
import il.co.idocare.testdoubles.utils.NullLogger;
import il.co.idocare.testdoubles.utils.multithreading.ThreadPostersTestController;
import il.co.idocare.useractions.cachers.UserActionCacher;
import il.co.idocare.useractions.entities.CreateRequestUserActionEntity;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.useractions.entities.VoteForRequestUserActionEntity;
import il.co.idocare.utils.Logger;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestsManagerTest {

    private static final String TEST_USER_ID = "test_user_id";
    private static final String TEST_USER_ID_2 = "test_user_id_2";

    @Mock UserActionCacher mUserActionCacherMock;
    @Mock RequestsRetriever mRequestsRetrieverMock;
    @Mock RequestsCacher mRequestsCacherMock;
    @Mock ServerSyncController mServerSyncControllerMock;

    @Mock RequestsManager.RequestsManagerListener mRequestsManagerListenerMock1;
    @Mock RequestsManager.RequestsManagerListener mRequestsManagerListenerMock2;


    private Logger mLogger = new NullLogger();
    private ThreadPostersTestController mThreadPostersTestController = new ThreadPostersTestController();

    @Captor private ArgumentCaptor<List<RequestEntity>> mRequestsListCaptor;
    @Captor private  ArgumentCaptor<RequestEntity> mRequestsCaptor;
    @Captor private  ArgumentCaptor<? extends UserActionEntity> mUserActionsCaptor;



    RequestsManager SUT;

    @Before
    public void setup() throws Exception {
        SUT = new RequestsManager(
                mThreadPostersTestController.getBackgroundThreadPoster(),
                mThreadPostersTestController.getMainThreadPoster(),
                mUserActionCacherMock,
                mRequestsRetrieverMock,
                mRequestsCacherMock,
                mLogger,
                mServerSyncControllerMock);
    }

    @Test
    public void fetchRequestsAssignedToUser_listenersNotified() {
        // Arrange

        RequestEntity pickedUpRequest =
                RequestEntity.getBuilder(RequestEntityProvider.getPickedUpRequestEntity())
                .setPickedUpBy(TEST_USER_ID)
                .build();

        RequestEntity closedRequest =
                RequestEntity.getBuilder(RequestEntityProvider.getClosedRequestEntity())
                .setPickedUpBy(TEST_USER_ID)
                .build();

        List<RequestEntity> assignedToTestUserRequests = Arrays.asList(pickedUpRequest, closedRequest);

        when(mRequestsRetrieverMock.getRequestsAssignedToUser(TEST_USER_ID))
                .thenReturn(assignedToTestUserRequests);

        // Act
        SUT.registerListener(mRequestsManagerListenerMock1);
        SUT.registerListener(mRequestsManagerListenerMock2);
        SUT.fetchRequestsAssignedToUserAndNotify(TEST_USER_ID);

        // Assert
        mThreadPostersTestController.waitUntilAllActionsCompleted();

        verify(mRequestsManagerListenerMock1, times(1)).onRequestsFetched(mRequestsListCaptor.capture());
        verify(mRequestsManagerListenerMock2, times(1)).onRequestsFetched(mRequestsListCaptor.capture());

        assertEquals(assignedToTestUserRequests, mRequestsListCaptor.getValue()); // param for listener1
        assertEquals(assignedToTestUserRequests, mRequestsListCaptor.getValue()); // param for listener2
    }

    @Test
    public void fetchAllRequests_listenersNotified() {
        // Arrange

        RequestEntity pickedUpRequest =
                RequestEntity.getBuilder(RequestEntityProvider.getPickedUpRequestEntity())
                        .setPickedUpBy(TEST_USER_ID)
                        .build();

        RequestEntity closedRequest =
                RequestEntity.getBuilder(RequestEntityProvider.getClosedRequestEntity())
                        .setPickedUpBy(TEST_USER_ID_2)
                        .build();

        List<RequestEntity> allRequests = Arrays.asList(pickedUpRequest, closedRequest);

        when(mRequestsRetrieverMock.getAllRequests()).thenReturn(allRequests);

        // Act
        SUT.registerListener(mRequestsManagerListenerMock1);
        SUT.registerListener(mRequestsManagerListenerMock2);
        SUT.fetchAllRequestsAndNotify();

        // Assert
        mThreadPostersTestController.waitUntilAllActionsCompleted();

        verify(mRequestsManagerListenerMock1, times(1)).onRequestsFetched(mRequestsListCaptor.capture());
        verify(mRequestsManagerListenerMock2, times(1)).onRequestsFetched(mRequestsListCaptor.capture());

        assertEquals(allRequests, mRequestsListCaptor.getValue()); // param for listener1
        assertEquals(allRequests, mRequestsListCaptor.getValue()); // param for listener2
    }

    @Test
    public void voteForRequest_requestExists_userActionCached() {
        // Arrange
        VoteForRequestUserActionEntity voteForRequestUserAction =
                new VoteForRequestUserActionEntity("1000", "request_id", 1);
        when(mRequestsRetrieverMock.getRequestById(anyString()))
                .thenReturn(RequestEntityProvider.getNewRequestEntity());
        // Act
        SUT.voteForRequest(voteForRequestUserAction);

        // Assert
        mThreadPostersTestController.waitUntilAllActionsCompleted();

        verify(mUserActionCacherMock).cacheUserAction(mUserActionsCaptor.capture());
        assertUserActionsEqual(mUserActionsCaptor.getValue(), voteForRequestUserAction);
    }

    @Test
    public void voteForRequest_requestExists_requestCachedAsLocallyModified() {
        // Arrange
        String requestId = "request_id";

        VoteForRequestUserActionEntity voteForRequestUserAction =
                new VoteForRequestUserActionEntity("1000", requestId, 1);

        RequestEntity pickedUpRequest =
                RequestEntity.getBuilder(RequestEntityProvider.getPickedUpRequestEntity())
                        .setId(requestId)
                        .setModifiedLocally(false)
                        .build();

        when(mRequestsRetrieverMock.getRequestById(requestId)).thenReturn(pickedUpRequest);

        // Act
        SUT.voteForRequest(voteForRequestUserAction);

        // Assert
        mThreadPostersTestController.waitUntilAllActionsCompleted();

        verify(mRequestsCacherMock).updateOrInsert(mRequestsCaptor.capture());

        RequestEntity capturedRequest = mRequestsCaptor.getValue();
        RequestEntity expectedRequest =
                RequestEntity.getBuilder(pickedUpRequest).setModifiedLocally(true).build();

        assertRequestsEqual(capturedRequest, expectedRequest);
    }

    @Test
    public void addNewRequest_requestCached() {
        // Arrange
        RequestEntity newRequest = RequestEntityProvider.getNewRequestEntity();

        // Act
        SUT.addNewRequest(newRequest);

        // Assert
        mThreadPostersTestController.waitUntilAllActionsCompleted();

        verify(mRequestsCacherMock).updateOrInsert(mRequestsCaptor.capture());
        RequestEntity cachedRequest = mRequestsCaptor.getValue();
        assertRequestsEqual(cachedRequest, newRequest);
    }

    @Test
    public void addNewRequest_createRequestUserActionCached() {
        // Arrange
        RequestEntity newRequest = RequestEntityProvider.getNewRequestEntity();

        // Act
        SUT.addNewRequest(newRequest);

        // Assert
        mThreadPostersTestController.waitUntilAllActionsCompleted();

        CreateRequestUserActionEntity expectedUserAction =
                new CreateRequestUserActionEntity(newRequest.getCreatedAt(), newRequest.getId(), newRequest.getCreatedBy());

        verify(mUserActionCacherMock).cacheUserAction(mUserActionsCaptor.capture());
        UserActionEntity cachedUserAction = mUserActionsCaptor.getValue();
        assertUserActionsEqual(cachedUserAction, expectedUserAction);

    }

    private void assertUserActionsEqual(UserActionEntity actual, UserActionEntity expected) {
        assertThat(actual.getActionParam(), is(expected.getActionParam()));
        assertThat(actual.getActionType(), is(expected.getActionType()));
        assertThat(actual.getEntityId(), is(expected.getEntityId()));
        assertThat(actual.getEntityParam(), is(expected.getEntityParam()));
        assertThat(actual.getEntityType(), is(expected.getEntityType()));
        assertThat(actual.getTimestamp(), is(expected.getTimestamp()));
    }

    private void assertRequestsEqual(RequestEntity actual, RequestEntity expected) {
        assertThat(actual.getId(), is(expected.getId()));
        assertThat(actual.getCreatedBy(), is(expected.getCreatedBy()));
        assertThat(actual.getCreatedAt(), is(expected.getCreatedAt()));
        assertThat(actual.getCreatedComment(), is(expected.getCreatedComment()));
        assertEquals(actual.getCreatedPictures(), expected.getCreatedPictures());
        assertThat(actual.getCreatedVotes(), is(expected.getCreatedVotes()));
        assertThat(actual.getLatitude(), is(expected.getLatitude()));
        assertThat(actual.getLongitude(), is(expected.getLongitude()));
        assertThat(actual.getPickedUpBy(), is(expected.getPickedUpBy()));
        assertThat(actual.getPickedUpAt(), is(expected.getPickedUpAt()));
        assertThat(actual.getClosedBy(), is(expected.getClosedBy()));
        assertThat(actual.getClosedAt(), is(expected.getClosedAt()));
        assertThat(actual.getClosedComment(), is(expected.getClosedComment()));
        assertEquals(actual.getClosedPictures(), expected.getClosedPictures());
        assertThat(actual.getClosedVotes(), is(expected.getClosedVotes()));
        assertThat(actual.getLocation(), is(expected.getLocation()));
        assertThat(actual.isModifiedLocally(), is(expected.isModifiedLocally()));
    }
}