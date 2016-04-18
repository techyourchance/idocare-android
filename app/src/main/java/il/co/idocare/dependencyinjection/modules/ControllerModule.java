package il.co.idocare.dependencyinjection.modules;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.authentication.MyAccountManager;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.dependencyinjection.ControllerScope;
import il.co.idocare.deviceinfo.GooglePlayServicesChecker;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;
import il.co.idocare.nonstaticproxies.TextUtilsProxy;
import il.co.idocare.utils.Logger;

@Module
public class ControllerModule {

    private Activity mActivity;

    public ControllerModule(@NonNull Activity activity) {
        mActivity = activity;
    }

    @Provides
    @ControllerScope
    Context provideContext() {
        return mActivity;
    }

    @Provides
    @ControllerScope
    Activity provideActivity() {
        return mActivity;
    }

    @Provides
    @ControllerScope
    LoginStateManager provideLoginStateManager(Context context, AccountManager accountManager,
                                               MyAccountManager myAccountManager, Logger logger) {
        return new LoginStateManager(context, accountManager, myAccountManager, logger);
    }


    @Provides
    @ControllerScope
    MyAccountManager provideMyAccountManager(AccountManager accountManager, Logger logger,
                                             TextUtilsProxy textUtilsProxy) {
        return new MyAccountManager(accountManager, logger, textUtilsProxy);
    }

    @Provides
    @ControllerScope
    ServerSyncController provideServerSyncController(LoginStateManager loginStateManager,
                                                     MyAccountManager myAccountManager,
                                                     ContentResolverProxy contentResolverProxy) {
        return new ServerSyncController(loginStateManager, myAccountManager, contentResolverProxy);
    }

    @Provides
    @ControllerScope
    GooglePlayServicesChecker provideGooglePlayServicesChecker(Activity activity) {
        return new GooglePlayServicesChecker(activity);
    }

}
