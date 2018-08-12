package il.co.idocare.useractions;

import android.support.annotation.WorkerThread;

import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import il.co.idocare.useractions.cachers.UserActionCacher;
import il.co.idocare.useractions.entities.UserActionEntity;

public class UserActionsManager {

    public interface UserActionsManagerListener {
        void onUserActionAdded(UserActionEntity userAction);
    }

    private final UserActionCacher mUserActionCacher;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UiThreadPoster mUiThreadPoster;

    public UserActionsManager(UserActionCacher userActionCacher,
                              BackgroundThreadPoster backgroundThreadPoster,
                              UiThreadPoster uiThreadPoster) {
        mUserActionCacher = userActionCacher;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mUiThreadPoster = uiThreadPoster;
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
        mUiThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                listener.onUserActionAdded(userAction);
            }
        });
    }

}
