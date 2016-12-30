package il.co.idocare.users;

import android.support.annotation.WorkerThread;

import il.co.idocare.common.BaseManager;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import il.co.idocare.utils.multithreading.MainThreadPoster;

/**
 * This manager encapsulates logic related to users
 */
public class UsersManager extends BaseManager<UsersManager.UsersManagerListener> {

    public interface UsersManagerListener {
        void onUserDataFetched(UserEntity user);
        void onUserDataNotFound(String userId);
    }

    private UsersRetriever mUsersRetriever;
    private BackgroundThreadPoster mBackgroundThreadPoster;
    private MainThreadPoster mMainThreadPoster;

    public UsersManager(UsersRetriever usersRetriever,
                        BackgroundThreadPoster backgroundThreadPoster,
                        MainThreadPoster mainThreadPoster) {
        mUsersRetriever = usersRetriever;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mMainThreadPoster = mainThreadPoster;
    }

    /**
     * Asynchronously fetch user's data and notify listeners upon completion
     */
    public void fetchUserByIdAndNotify(final String userId) {
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                fetchUserByIdAndNotifyInternal(userId);
            }
        });
    }

    @WorkerThread
    private void fetchUserByIdAndNotifyInternal(String userId) {
        UserEntity userEntity = mUsersRetriever.getUserById(userId);
        if (userEntity != null) {
            notifyUserDataFetched(userEntity);
        } else {
            notifyUserDataNotFound(userId);
        }
    }


    private void notifyUserDataFetched(final UserEntity userEntity) {
        mMainThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                for (UsersManagerListener listener : getListeners()) {
                    listener.onUserDataFetched(userEntity);
                }
            }
        });
    }

    private void notifyUserDataNotFound(final String userId) {
        mMainThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                for (UsersManagerListener listener : getListeners()) {
                    listener.onUserDataNotFound(userId);
                }
            }
        });
    }

}
