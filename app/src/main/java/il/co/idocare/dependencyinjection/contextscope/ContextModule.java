package il.co.idocare.dependencyinjection.contextscope;

import android.accounts.AccountManager;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.common.settings.SettingsManager;
import il.co.idocare.helpers.LocationHelper;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;
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
    ServerSyncController provideServerSyncController(ContentResolverProxy contentResolverProxy,
                                                     LoginStateManager loginStateManager) {
        return new ServerSyncController(contentResolverProxy, loginStateManager);
    }

    @Provides
    LocationHelper locationHelper() {
        return new LocationHelper();
    }

}
