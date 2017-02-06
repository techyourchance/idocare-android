package il.co.idocare.useractions;

import android.support.annotation.WorkerThread;

import il.co.idocare.useractions.cachers.UserActionCacher;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import il.co.idocare.utils.multithreading.MainThreadPoster;

public class UserActionsManager {

    public interface UserActionsManagerListener {
        void onUserActionAdded(UserActionEntity userAction);
    }

    private final UserActionCacher mUserActionCacher;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final MainThreadPoster mMainThreadPoster;

    public UserActionsManager(UserActionCacher userActionCacher,
                              BackgroundThreadPoster backgroundThreadPoster,
                              MainThreadPoster mainThreadPoster) {
        mUserActionCacher = userActionCacher;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mMainThreadPoster = mainThreadPoster;
    }

    public void addUserActionAndNotify(final UserActionEntity userAction,
                                       final UserActionsManagerListener listener) {
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                addNewUserActionSync(userAction, listener);
            }
        });
    }

    @WorkerThread
    private void addNewUserActionSync(final UserActionEntity userAction,
                                      final UserActionsManagerListener listener) {
        mUserActionCacher.cacheUserAction(userAction);
        mMainThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                listener.onUserActionAdded(userAction);
            }
        });
    }

}
