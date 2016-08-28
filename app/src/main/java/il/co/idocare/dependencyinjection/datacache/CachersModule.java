package il.co.idocare.dependencyinjection.datacache;

import android.content.ContentResolver;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.entities.cachers.UserActionCacher;
import il.co.idocare.utils.Logger;

@Module
public class CachersModule {

    @Provides
    UserActionCacher userActionCacher(ContentResolver contentResolver, Logger logger) {
        return new UserActionCacher(contentResolver, logger);
    }
}
