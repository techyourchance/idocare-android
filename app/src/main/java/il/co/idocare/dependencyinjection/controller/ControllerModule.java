package il.co.idocare.dependencyinjection.controller;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.deviceinfo.GooglePlayServicesChecker;
import il.co.idocare.dialogs.DialogsFactory;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;
import il.co.idocare.requests.cachers.RequestsCacher;
import il.co.idocare.screens.common.MainFrameHelper;
import il.co.idocare.screens.common.toolbar.ToolbarManager;
import il.co.idocare.screens.common.toolbar.ToolbarDelegate;
import il.co.idocare.screens.navigationdrawer.NavigationDrawerDelegate;
import il.co.idocare.screens.navigationdrawer.NavigationDrawerManager;
import il.co.idocare.useractions.UserActionsManager;
import il.co.idocare.users.UsersDataMonitoringManager;
import il.co.idocare.users.UsersManager;
import il.co.idocare.users.UsersRetriever;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrator;
import il.co.idocare.utils.multithreading.MainThreadPoster;
import il.co.idocare.requests.retrievers.RequestsRetriever;
import il.co.idocare.useractions.cachers.UserActionCacher;
import il.co.idocare.requests.RequestsManager;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import il.co.idocare.serversync.ServerSyncController;
import il.co.idocare.pictures.CameraAdapter;
import il.co.idocare.utils.Logger;

@Module
public class ControllerModule {

    private final Activity mActivity;
    private final FragmentManager mFragmentManager;

    public ControllerModule(Activity activity, FragmentManager fragmentManager) {
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
    @ControllerScope
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
    MainFrameHelper frameHelper(Activity activity, FragmentManager fragmentManager) {
        return new MainFrameHelper(activity, fragmentManager);
    }

    @Provides
    @ControllerScope
    DialogsManager dialogsManager(FragmentManager fragmentManager) {
        return new DialogsManager(fragmentManager);
    }

    @Provides
    @ControllerScope
    DialogsFactory dialogsFactory() {
        return new DialogsFactory();
    }

    @Provides
    @ControllerScope
    RequestsManager requestsManager(
            BackgroundThreadPoster backgroundThreadPoster,
            MainThreadPoster mainThreadPoster,
            UserActionCacher userActionCacher,
            RequestsRetriever requestsRetriever,
            RequestsCacher requestsCacher,
            Logger logger,
            ServerSyncController serverSyncController) {
        return new RequestsManager(backgroundThreadPoster, mainThreadPoster, userActionCacher,
                requestsRetriever, requestsCacher, logger, serverSyncController);
    }

    @Provides
    UsersDataMonitoringManager usersDataMonitoringManager(UsersRetriever usersRetriever,
                                                           BackgroundThreadPoster backgroundThreadPoster,
                                                           MainThreadPoster mainThreadPoster) {
        return new UsersDataMonitoringManager(usersRetriever, backgroundThreadPoster, mainThreadPoster);
    }

    @Provides
    UsersManager usersManager(UsersRetriever usersRetriever,
                              BackgroundThreadPoster backgroundThreadPoster,
                              MainThreadPoster mainThreadPoster) {
        return new UsersManager(usersRetriever, backgroundThreadPoster, mainThreadPoster);
    }

    @Provides
    UserActionsManager userActionsManager(UserActionCacher userActionCacher,
                                          BackgroundThreadPoster backgroundThreadPoster,
                                          MainThreadPoster mainThreadPoster) {
        return new UserActionsManager(userActionCacher, backgroundThreadPoster, mainThreadPoster);
    }


    @Provides
    EventBusRegistrator eventBusRegistrator(EventBus eventBus, Logger logger) {
        return new EventBusRegistrator(eventBus, logger);
    }

    @Provides
    NavigationDrawerManager navigationDrawerManager(Logger logger) {
        return new NavigationDrawerManager((NavigationDrawerDelegate) mActivity, logger);
    }

    @Provides
    ToolbarManager toolbarManager(Logger logger) {
        return new ToolbarManager((ToolbarDelegate) mActivity, logger);
    }

    @Provides
    @ControllerScope
    ServerSyncController serverSyncController(ContentResolverProxy contentResolverProxy,
                                              LoginStateManager loginStateManager) {
        return new ServerSyncController(contentResolverProxy, loginStateManager);
    }
}
