package il.co.idocare.users;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import il.co.idocare.testdoubles.utils.multithreading.ThreadPostersTestController;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsersManagerTest {

    @Mock UsersRetriever mUsersRetrieverMock;
    @Mock UsersManager.UsersManagerListener mUsersManagerListenerMock;

    private ThreadPostersTestController mThreadPostersTestController = new ThreadPostersTestController();

    private UsersManager SUT;

    @Captor ArgumentCaptor<String> mStringCaptor;
    @Captor ArgumentCaptor<UserEntity> mUserCaptor;

    @Before
    public void setup() throws Exception {
        SUT = new UsersManager(
                mUsersRetrieverMock,
                mThreadPostersTestController.getBackgroundThreadPoster(),
                mThreadPostersTestController.getMainThreadPoster());
    }

    @Test
    public void fetchUserByIdAndNotify_userExists_listenersNotifiedWithCorrectData() {
        // Arrange
        String testUserId = "testUserId";
        UserEntity testEntity = mock(UserEntity.class);
        when(mUsersRetrieverMock.getUserById(anyString())).thenReturn(testEntity);
        // Act
        SUT.registerListener(mUsersManagerListenerMock);
        SUT.fetchUserByIdAndNotify(testUserId);

        mThreadPostersTestController.waitUntilAllActionsCompleted();
        // Assert
        verify(mUsersRetrieverMock, times(1)).getUserById(mStringCaptor.capture());
        verifyNoMoreInteractions(mUsersRetrieverMock);
        assertThat(mStringCaptor.getValue(), is(testUserId));

        verify(mUsersManagerListenerMock, times(1)).onUserDataFetched(mUserCaptor.capture());
        verifyNoMoreInteractions(mUsersManagerListenerMock);
        assertThat(mUserCaptor.getValue(), is(testEntity));
    }

    @Test
    public void fetchUserByIdAndNotify_userDoesntExist_listenersNotifiedNoData() {
        // Arrange
        String testUserId = "testUserId";
        when(mUsersRetrieverMock.getUserById(anyString())).thenReturn(null);
        // Act
        SUT.registerListener(mUsersManagerListenerMock);
        SUT.fetchUserByIdAndNotify(testUserId);

        mThreadPostersTestController.waitUntilAllActionsCompleted();
        // Assert
        verify(mUsersRetrieverMock, times(1)).getUserById(mStringCaptor.capture());
        verifyNoMoreInteractions(mUsersRetrieverMock);
        assertThat(mStringCaptor.getValue(), is(testUserId));

        verify(mUsersManagerListenerMock, times(1)).onUserDataNotFound(mStringCaptor.capture());
        verifyNoMoreInteractions(mUsersManagerListenerMock);
        assertThat(mStringCaptor.getValue(), is(testUserId));
    }
}