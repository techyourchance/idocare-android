package il.co.idocarecore.users;

import com.techyourchance.threadposter.ThreadPostersTestDouble;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import il.co.idocarecore.users.events.UserDataChangedEvent;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsersDataMonitoringManagerTest {

    private static final String TEST_USER_ID = "test_user_id";

    @Mock UsersRetriever mUsersRetrieverMock;
    @Mock UsersDataMonitoringManager.UsersDataMonitorListener mUsersDataMonitorListenerMock;

    private ThreadPostersTestDouble mThreadPostersTestDouble = new ThreadPostersTestDouble();

    private UsersDataMonitoringManager SUT;

    @Captor ArgumentCaptor<String> mStringCaptor;
    @Captor ArgumentCaptor<UserEntity> mUserCaptor;

    @Mock UserEntity mEntityMock;

    @Before
    public void setup() throws Exception {
        SUT = new UsersDataMonitoringManager(
                mUsersRetrieverMock,
                mThreadPostersTestDouble.getBackgroundTestDouble(),
                mThreadPostersTestDouble.getUiTestDouble());
    }

    @Test
    public void fetchUserByIdAndNotifyIfExists_userExists_listenersNotifiedWithCorrectData() {
        // Arrange
        returnEntityMockFromRetrieverForAnyUserId();
        // Act
        SUT.registerListener(mUsersDataMonitorListenerMock);
        SUT.fetchUserByIdAndNotifyIfExists(TEST_USER_ID);

        mThreadPostersTestDouble.join();
        // Assert
        verify(mUsersRetrieverMock, times(1)).getUserById(mStringCaptor.capture());
        verifyNoMoreInteractions(mUsersRetrieverMock);
        assertThat(mStringCaptor.getValue(), is(TEST_USER_ID));

        verify(mUsersDataMonitorListenerMock, times(1)).onUserDataChange(mUserCaptor.capture());
        verifyNoMoreInteractions(mUsersDataMonitorListenerMock);
        assertThat(mUserCaptor.getValue(), is(mEntityMock));
    }

    @Test
    public void fetchUserByIdAndNotifyIfExists_userDoesntExist_listenersNotNotified() {
        // Arrange
        returnNullFromRetrieverForAnyUserId();
        // Act
        SUT.registerListener(mUsersDataMonitorListenerMock);
        SUT.fetchUserByIdAndNotifyIfExists(TEST_USER_ID);

        mThreadPostersTestDouble.join();
        // Assert
        verify(mUsersRetrieverMock, times(1)).getUserById(mStringCaptor.capture());
        verifyNoMoreInteractions(mUsersRetrieverMock);
        assertThat(mStringCaptor.getValue(), is(TEST_USER_ID));

        verifyNoMoreInteractions(mUsersDataMonitorListenerMock);
    }

    @Test
    public void onUserDataChanged_userExists_listenersNotifiedWithCorrectData() {
        // Arrange
        returnEntityMockFromRetrieverForAnyUserId();
        // Act
        SUT.registerListener(mUsersDataMonitorListenerMock);
        SUT.onUserDataChanged(new UserDataChangedEvent(TEST_USER_ID));

        mThreadPostersTestDouble.join();
        // Assert
        verify(mUsersRetrieverMock, times(1)).getUserById(mStringCaptor.capture());
        verifyNoMoreInteractions(mUsersRetrieverMock);
        assertThat(mStringCaptor.getValue(), is(TEST_USER_ID));

        verify(mUsersDataMonitorListenerMock, times(1)).onUserDataChange(mUserCaptor.capture());
        verifyNoMoreInteractions(mUsersDataMonitorListenerMock);
        assertThat(mUserCaptor.getValue(), is(mEntityMock));
    }

    @Test
    public void onUserDataChanged_userDoesntExist_listenersNotNotified() {
        // Arrange
        returnNullFromRetrieverForAnyUserId();
        // Act
        SUT.registerListener(mUsersDataMonitorListenerMock);
        SUT.onUserDataChanged(new UserDataChangedEvent(TEST_USER_ID));

        mThreadPostersTestDouble.join();
        // Assert
        verify(mUsersRetrieverMock, times(1)).getUserById(mStringCaptor.capture());
        verifyNoMoreInteractions(mUsersRetrieverMock);
        assertThat(mStringCaptor.getValue(), is(TEST_USER_ID));

        verifyNoMoreInteractions(mUsersDataMonitorListenerMock);
    }

    // ---------------------------------------------------------------------------------------------
    //
    // Helper methods

    private void returnEntityMockFromRetrieverForAnyUserId() {
        when(mUsersRetrieverMock.getUserById(anyString())).thenReturn(mEntityMock);
    }

    private void returnNullFromRetrieverForAnyUserId() {
        when(mUsersRetrieverMock.getUserById(anyString())).thenReturn(null);
    }
    
}