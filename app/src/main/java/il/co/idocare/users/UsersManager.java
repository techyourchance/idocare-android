package il.co.idocare.users;

import android.support.annotation.WorkerThread;

import java.util.List;

import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import il.co.idocare.utils.multithreading.MainThreadPoster;

public class UsersManager {

    public interface UsersManagerListener {
        void onUsersFetched(List<UserEntity> users);
    }

    private final UsersRetriever mUsersRetriever;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final MainThreadPoster mMainThreadPoster;

    public UsersManager(UsersRetriever usersRetriever,
                        BackgroundThreadPoster backgroundThreadPoster,
                        MainThreadPoster mainThreadPoster) {
        mUsersRetriever = usersRetriever;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mMainThreadPoster = mainThreadPoster;
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
        mMainThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                listener.onUsersFetched(users);
            }
        });
    }
}
