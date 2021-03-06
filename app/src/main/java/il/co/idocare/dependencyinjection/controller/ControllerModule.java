package il.co.idocare.dependencyinjection.controller;

import android.app.Activity;
import android.content.Context;

import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import org.greenrobot.eventbus.EventBus;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import dagger.Module;
import dagger.Provides;
import il.co.idocare.screens.common.dialogs.DialogsFactoryImpl;
import il.co.idocare.screens.common.toolbar.ToolbarDelegate;
import il.co.idocare.screens.common.toolbar.ToolbarManager;
import il.co.idocare.screens.navigationdrawer.NavigationDrawerDelegate;
import il.co.idocare.screens.navigationdrawer.NavigationDrawerManager;
import il.co.idocare.serversync.ServerSyncControllerImpl;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.deviceinfo.GooglePlayServicesChecker;
import il.co.idocarecore.nonstaticproxies.ContentResolverProxy;
import il.co.idocarecore.pictures.CameraAdapter;
import il.co.idocarecore.requests.RequestsManager;
import il.co.idocarecore.requests.cachers.RequestsCacher;
import il.co.idocarecore.requests.retrievers.RequestsRetriever;
import il.co.idocarecore.screens.common.dialogs.DialogsFactory;
import il.co.idocarecore.screens.common.dialogs.DialogsManager;
import il.co.idocarecore.serversync.ServerSyncController;
import il.co.idocarecore.useractions.UserActionsManager;
import il.co.idocarecore.useractions.cachers.UserActionCacher;
import il.co.idocarecore.users.UsersDataMonitoringManager;
import il.co.idocarecore.users.UsersManager;
import il.co.idocarecore.users.UsersRetriever;
import il.co.idocarecore.utils.Logger;
import il.co.idocarecore.utils.eventbusregistrator.EventBusRegistrator;

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
    DialogsManager dialogsManager(FragmentManager fragmentManager) {
        return new DialogsManager(fragmentManager);
    }

    @Provides
    @ControllerScope
    DialogsFactory dialogsFactory(FragmentFactory fragmentFactory, Activity activity) {
        return new DialogsFactoryImpl(fragmentFactory, activity);
    }

    @Provides
    @ControllerScope
    RequestsManager requestsManager(
            BackgroundThreadPoster backgroundThreadPoster,
            UiThreadPoster uiThreadPoster,
            UserActionCacher userActionCacher,
            RequestsRetriever requestsRetriever,
            RequestsCacher requestsCacher,
            Logger logger,
            ServerSyncController serverSyncController) {
        return new RequestsManager(backgroundThreadPoster, uiThreadPoster, userActionCacher,
                requestsRetriever, requestsCacher, logger, serverSyncController);
    }

    @Provides
    UsersDataMonitoringManager usersDataMonitoringManager(UsersRetriever usersRetriever,
                                                           BackgroundThreadPoster backgroundThreadPoster,
                                                           UiThreadPoster uiThreadPoster) {
        return new UsersDataMonitoringManager(usersRetriever, backgroundThreadPoster, uiThreadPoster);
    }

    @Provides
    UsersManager usersManager(UsersRetriever usersRetriever,
                              BackgroundThreadPoster backgroundThreadPoster,
                              UiThreadPoster uiThreadPoster) {
        return new UsersManager(usersRetriever, backgroundThreadPoster, uiThreadPoster);
    }

    @Provides
    UserActionsManager userActionsManager(UserActionCacher userActionCacher,
                                          BackgroundThreadPoster backgroundThreadPoster,
                                          UiThreadPoster uiThreadPoster) {
        return new UserActionsManager(userActionCacher, backgroundThreadPoster, uiThreadPoster);
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
        return new ServerSyncControllerImpl(contentResolverProxy, loginStateManager);
    }

}
