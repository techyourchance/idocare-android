package il.co.idocare.dependencyinjection.applicationscope;

import android.app.Application;
import android.content.ContentResolver;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.contentproviders.IdcSQLiteOpenHelper;
import il.co.idocare.contentproviders.TransactionsController;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;

@Module
public class ContentProviderModule {

    private final IdcSQLiteOpenHelper mSqLiteOpenHelper;

    public ContentProviderModule(IdcSQLiteOpenHelper sqLiteOpenHelper) {
        mSqLiteOpenHelper = sqLiteOpenHelper;
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
    TransactionsController transactionsController() {
        return new TransactionsController(mSqLiteOpenHelper);
    }

}
