package il.co.idocare.dependencyinjection.application;

import android.app.Application;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.BuildConfig;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.networking.FilesDownloader;
import il.co.idocarecore.networking.GeneralApi;
import il.co.idocarecore.networking.ServerApi;
import il.co.idocarecore.networking.StdHeadersInterceptor;
import il.co.idocarecore.utils.Logger;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkingModule {

    @Provides
    @ApplicationScope
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
    FilesDownloader filesDownloader(Application application, GeneralApi generalApi, Logger logger) {
        FilesDownloader.CacheDirRetriever cacheDirRetriever =
                new FilesDownloader.CacheDirRetriever(application);
        return new FilesDownloader(cacheDirRetriever, generalApi, logger);
    }
}
