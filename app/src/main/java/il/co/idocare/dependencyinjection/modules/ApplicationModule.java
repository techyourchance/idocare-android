package il.co.idocare.dependencyinjection.modules;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.Constants;
import il.co.idocare.dependencyinjection.ApplicationScope;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;
import il.co.idocare.nonstaticproxies.TextUtilsProxy;
import il.co.idocare.utils.Logger;

@Module
public class ApplicationModule {

    private final Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @ApplicationScope
    Application provideApplicationContext() {
        return mApplication;
    }

    @Provides
    @ApplicationScope
    AccountManager provideAccountManager(Application application) {
        return AccountManager.get(application);
    }

    @Provides
    @ApplicationScope
    SharedPreferences provideSharedPreferences() {
        return mApplication.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @ApplicationScope
    Logger provideLogger() {
        return new Logger();
    }


    @Provides
    @ApplicationScope
    ContentResolverProxy provideContentResolverProxy() {
        return new ContentResolverProxy();
    }


    @Provides
    @ApplicationScope
    TextUtilsProxy  provideTextUtilsProxy() {
        return new TextUtilsProxy();
    }
}
