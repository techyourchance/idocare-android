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
import il.co.idocare.requests.retrievers.RequestsRetriever;
import il.co.idocare.testdoubles.entities.RequestEntityProvider;
import il.co.idocare.testdoubles.utils.NullLogger;
import il.co.idocare.testdoubles.utils.multithreading.ThreadPostersTestController;
import il.co.idocare.useractions.cachers.UserActionCacher;
import il.co.idocare.utils.Logger;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestsManagerTest {

    private static final String TEST_USER_ID = "test_user_id";

    @Mock UserActionCacher mUserActionCacherMock;
    @Mock RequestsRetriever mRequestsRetrieverMock;
    @Mock ServerSyncController mServerSyncControllerMock;

    @Mock RequestsManager.RequestsManagerListener mRequestsManagerListenerMock1;
    @Mock RequestsManager.RequestsManagerListener mRequestsManagerListenerMock2;


    private Logger mLogger = new NullLogger();
    private ThreadPostersTestController mThreadPostersTestController = new ThreadPostersTestController();

    @Captor
    private ArgumentCaptor<List<RequestEntity>> captor;

    RequestsManager SUT;

    @Before
    public void setup() throws Exception {
        SUT = new RequestsManager(
                mThreadPostersTestController.getBackgroundThreadPoster(),
                mThreadPostersTestController.getMainThreadPoster(),
                mUserActionCacherMock,
                mRequestsRetrieverMock,
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
        SUT.fetchRequestsAssignedToUser(TEST_USER_ID);

        // Assert
        mThreadPostersTestController.waitUntilAllActionsCompleted();

        verify(mRequestsManagerListenerMock1, times(1)).onRequestsFetched(captor.capture());
        verify(mRequestsManagerListenerMock2, times(1)).onRequestsFetched(captor.capture());

        assertEquals(assignedToTestUserRequests, captor.getValue()); // param for listener1
        assertEquals(assignedToTestUserRequests, captor.getValue()); // param for listener2
    }
}