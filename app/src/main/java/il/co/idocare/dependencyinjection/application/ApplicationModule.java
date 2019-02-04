package il.co.idocare.dependencyinjection.application;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;

import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.location.IdcLocationManagerImpl;
import il.co.idocarecore.Constants;
import il.co.idocarecore.authentication.AuthManager;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.common.settings.PreferenceSettingsEntryFactoryImpl;
import il.co.idocarecore.common.settings.SettingsManager;
import il.co.idocare.location.ReverseGeocoder;
import il.co.idocare.location.StandardReverseGeocoder;
import il.co.idocarecore.location.IdcLocationManager;
import il.co.idocarecore.networking.FilesDownloader;
import il.co.idocarecore.networking.ServerApi;
import il.co.idocarecore.pictures.ImageViewPictureLoader;
import il.co.idocarecore.useractions.UserActionEntityFactory;
import il.co.idocarecore.utils.Logger;

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
    AuthManager authManager(LoginStateManager loginStateManager,
                            BackgroundThreadPoster backgroundThreadPoster,
                            ServerApi serverApi,
                            FilesDownloader filesDownloader,
                            EventBus eventBus,
                            Logger logger) {
        return new AuthManager(loginStateManager, backgroundThreadPoster, serverApi, filesDownloader, eventBus, logger);
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
    EventBus eventBus() {
        return EventBus.getDefault();
    }

    @Provides
    @ApplicationScope
    UiThreadPoster uiThreadPoster() {
        return new UiThreadPoster();
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

    @Provides
    ReverseGeocoder reverseGeocoder(Application application) {
        Geocoder geocoder = new Geocoder(application, Locale.getDefault());
        return new StandardReverseGeocoder(geocoder);
    }

    @Provides
    @ApplicationScope
    IdcLocationManager idcLocationManager(Application application, UiThreadPoster uiThreadPoster, Logger logger) {
        return new IdcLocationManagerImpl(application, uiThreadPoster, logger);
    }
}
