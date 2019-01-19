package il.co.idocare.users;

import androidx.annotation.WorkerThread;

import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.util.List;


public class UsersManager {

    public interface UsersManagerListener {
        void onUsersFetched(List<UserEntity> users);
    }

    private final UsersRetriever mUsersRetriever;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UiThreadPoster mUiThreadPoster;

    public UsersManager(UsersRetriever usersRetriever,
                        BackgroundThreadPoster backgroundThreadPoster,
                        UiThreadPoster uiThreadPoster) {
        mUsersRetriever = usersRetriever;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mUiThreadPoster = uiThreadPoster;
    }

    public void fetchUsersByIdAndNotify(final List<String> userIds,
                                        final UsersManagerListener listener) {
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                fetchUsersByIdAndNotifySync(userIds, listener);
            }
        });
    }

    @WorkerThread
    private void fetchUsersByIdAndNotifySync(List<String> userIds, final UsersManagerListener listener) {
        final List<UserEntity> users = mUsersRetriever.getUsersByIds(userIds);
        mUiThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                listener.onUsersFetched(users);
            }
        });
    }
}
