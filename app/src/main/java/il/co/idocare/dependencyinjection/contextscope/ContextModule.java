package il.co.idocare.dependencyinjection.contextscope;

import android.accounts.AccountManager;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.authentication.MyAccountManager;
import il.co.idocare.helpers.LocationHelper;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;
import il.co.idocare.nonstaticproxies.TextUtilsProxy;
import il.co.idocare.pictures.ImageViewPictureLoader;
import il.co.idocare.utils.Logger;

@Module
public class ContextModule {

    private Context mContext;

    public ContextModule(Context context) {
        mContext = context;
    }

    @Provides
    @ContextScope
    Context provideContext() {
        return mContext;
    }

    @Provides
    @ContextScope
    LoginStateManager provideLoginStateManager(Context context, AccountManager accountManager,
                                               MyAccountManager myAccountManager, Logger logger) {
        return new LoginStateManager(context, accountManager, myAccountManager, logger);
    }


    @Provides
    @ContextScope
    MyAccountManager provideMyAccountManager(AccountManager accountManager, Logger logger,
                                             TextUtilsProxy textUtilsProxy) {
        return new MyAccountManager(accountManager, logger, textUtilsProxy);
    }

    @Provides
    @ContextScope
    ServerSyncController provideServerSyncController(MyAccountManager myAccountManager,
                                                     ContentResolverProxy contentResolverProxy) {
        return new ServerSyncController(myAccountManager, contentResolverProxy);
    }

    @Provides
    @ContextScope
    ImageViewPictureLoader provideImageViewPictureLoader() {
        return new ImageViewPictureLoader();
    }

    @Provides
    LocationHelper locationHelper() {
        return new LocationHelper();
    }

}
