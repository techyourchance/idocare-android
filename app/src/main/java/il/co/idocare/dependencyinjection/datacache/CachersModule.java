package il.co.idocare.dependencyinjection.datacache;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.dependencyinjection.controllerscope.ControllerScope;
import il.co.idocare.deviceinfo.GooglePlayServicesChecker;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.entities.cachers.UserActionCacher;
import il.co.idocare.managers.VoteManager;
import il.co.idocare.multithreading.BackgroundThreadPoster;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.pictures.CameraAdapter;
import il.co.idocare.utils.Logger;

@Module
public class CachersModule {

    private Activity mActivity;
    private FragmentManager mFragmentManager;

    public CachersModule(@NonNull Activity activity, FragmentManager fragmentManager) {
        mActivity = activity;
        mFragmentManager = fragmentManager;
    }

    @Provides
    @ControllerScope
    Context context() {
        return mActivity;
    }

    @Provides
    @ControllerScope
    Activity activity() {
        return mActivity;
    }

    @Provides
    FragmentManager fragmentManager() {
        return mFragmentManager;
    }

    @Provides
    @ControllerScope
    GooglePlayServicesChecker googlePlayServicesChecker(Activity activity) {
        return new GooglePlayServicesChecker(activity);
    }

    @Provides
    CameraAdapter cameraAdapter(Activity activity) {
        return new CameraAdapter(activity);
    }

    @Provides
    @ControllerScope
    DialogsManager dialogsManager(FragmentManager fragmentManager) {
        return new DialogsManager(fragmentManager);
    }

    @Provides
    @ControllerScope
    VoteManager voteManager(BackgroundThreadPoster backgroundThreadPoster,
                            LoginStateManager loginStateManager,
                            UserActionCacher userActionCacher,
                            Logger logger,
                            ServerSyncController serverSyncController) {
        return new VoteManager(backgroundThreadPoster, loginStateManager, userActionCacher, logger,
                serverSyncController);
    }
}
