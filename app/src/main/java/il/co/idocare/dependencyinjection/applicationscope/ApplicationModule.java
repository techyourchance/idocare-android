package il.co.idocare.dependencyinjection.applicationscope;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.BuildConfig;
import il.co.idocare.Constants;
import il.co.idocare.authentication.AuthManager;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.common.settings.PreferenceSettingsEntryFactoryImpl;
import il.co.idocare.common.settings.SettingsManager;
import il.co.idocare.contentproviders.IdcSQLiteOpenHelper;
import il.co.idocare.contentproviders.TransactionsController;
import il.co.idocare.networking.FilesDownloader;
import il.co.idocare.networking.GeneralApi;
import il.co.idocare.networking.ServerApi;
import il.co.idocare.networking.StdHeadersInterceptor;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;
import il.co.idocare.nonstaticproxies.TextUtilsProxy;
import il.co.idocare.pictures.ImageViewPictureLoader;
import il.co.idocare.useractions.UserActionEntityFactory;
import il.co.idocare.utils.Logger;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import il.co.idocare.utils.multithreading.MainThreadPoster;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApplicationModule {

    private final Application mApplication;
    private final IdcSQLiteOpenHelper mSqLiteOpenHelper;

    public ApplicationModule(Application application, IdcSQLiteOpenHelper sqLiteOpenHelper) {
        mApplication = application;
        mSqLiteOpenHelper = sqLiteOpenHelper;
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

    @Provides
    StdHeadersInterceptor stdHeadersInterceptor(LoginStateManager loginStateManager, Logger logger) {
        return new StdHeadersInterceptor(loginStateManager, logger);
    }

    @Provides
    @ApplicationScope
    Retrofit retrofit(StdHeadersInterceptor stdHeadersInterceptor) {
        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(stdHeadersInterceptor);
        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }

    @Provides
    @ApplicationScope
    ServerApi serverApi(Retrofit retrofit) {
        return retrofit.create(ServerApi.class);
    }

    @Provides
    @ApplicationScope
    GeneralApi generalApi(Retrofit retrofit) {
        return retrofit.create(GeneralApi.class);
    }

    @Provides
    FilesDownloader filesDownloader( Application application, GeneralApi generalApi, Logger logger) {
        return new FilesDownloader(application, generalApi, logger);
    }

    @Provides
    TransactionsController transactionsController() {
        return new TransactionsController(mSqLiteOpenHelper);
    }
}
