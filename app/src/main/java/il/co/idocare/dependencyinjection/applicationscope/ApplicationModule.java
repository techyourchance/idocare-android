package il.co.idocare.dependencyinjection.applicationscope;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.Constants;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.common.settings.PreferenceSettingsEntryFactoryImpl;
import il.co.idocare.common.settings.SettingsManager;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;
import il.co.idocare.nonstaticproxies.TextUtilsProxy;
import il.co.idocare.pictures.ImageViewPictureLoader;
import il.co.idocare.useractions.UserActionEntityFactory;
import il.co.idocare.utils.Logger;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import il.co.idocare.utils.multithreading.MainThreadPoster;

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
    LoginStateManager provideLoginStateManager(AccountManager accountManager,
                                               SettingsManager settingsManager,
                                               Logger logger) {
        return new LoginStateManager(accountManager, settingsManager, logger);
    }

    @Provides
    @ApplicationScope
    SharedPreferences provideSharedPreferences() {
        return mApplication.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Provides
    @ApplicationScope
    SettingsManager settingsManager(SharedPreferences sharedPreferences) {
        return new SettingsManager(new PreferenceSettingsEntryFactoryImpl(sharedPreferences));
    }

    @Provides
    @ApplicationScope
    Logger provideLogger() {
        return new Logger();
    }

    @Provides
    ContentResolver contentResolver() {
        return mApplication.getContentResolver();
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

    @Provides
    EventBus eventBus() {
        return EventBus.getDefault();
    }

    @Provides
    @ApplicationScope
    MainThreadPoster mainThreadPoster() {
        return new MainThreadPoster();
    }

    @Provides
    @ApplicationScope
    BackgroundThreadPoster backgroundThreadPoster() {
        return new BackgroundThreadPoster();
    }

    @Provides
    @ApplicationScope
    ImageViewPictureLoader provideImageViewPictureLoader() {
        return new ImageViewPictureLoader();
    }

    @Provides
    UserActionEntityFactory userActionEntityFactory() {
        return new UserActionEntityFactory();
    }
}
