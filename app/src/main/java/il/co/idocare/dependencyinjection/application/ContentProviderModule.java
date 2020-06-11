package il.co.idocare.dependencyinjection.application;

import android.app.Application;
import android.content.ContentResolver;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import il.co.idocarecore.contentproviders.IdcSQLiteOpenHelper;
import il.co.idocarecore.contentproviders.TransactionsController;
import il.co.idocarecore.nonstaticproxies.ContentResolverProxy;

@Module
@InstallIn(ApplicationComponent.class)
public class ContentProviderModule {

    @Provides
    IdcSQLiteOpenHelper idcSQLiteOpenHelper(Application application) {
        return IdcSQLiteOpenHelper.getInstance(application);
    }

    @Provides
    ContentResolver contentResolver(Application application) {
        return application.getContentResolver();
    }

    @Provides
    ContentResolverProxy provideContentResolverProxy() {
        return new ContentResolverProxy();
    }

    @Provides
    TransactionsController transactionsController(IdcSQLiteOpenHelper idcSQLiteOpenHelper) {
        return new TransactionsController(idcSQLiteOpenHelper);
    }

}
