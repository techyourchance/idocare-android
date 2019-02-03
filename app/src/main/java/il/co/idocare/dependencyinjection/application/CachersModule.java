package il.co.idocare.dependencyinjection.application;

import android.content.ContentResolver;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import il.co.idocarecore.requests.cachers.RequestsCacher;
import il.co.idocarecore.requests.cachers.TempIdCacher;
import il.co.idocarecore.useractions.cachers.UserActionCacher;
import il.co.idocarecore.users.UsersCacher;
import il.co.idocarecore.utils.Logger;

@Module
public class CachersModule {

    @Provides
    UserActionCacher userActionCacher(ContentResolver contentResolver, Logger logger) {
        return new UserActionCacher(contentResolver, logger);
    }

    @Provides
    RequestsCacher requestsCacher(ContentResolver contentResolver, EventBus eventBus, Logger logger) {
        return new RequestsCacher(contentResolver, eventBus, logger);
    }

    @Provides
    UsersCacher usersCacher(ContentResolver contentResolver, EventBus eventBus, Logger logger) {
        return new UsersCacher(contentResolver, eventBus, logger);
    }

    @Provides
    TempIdCacher tempIdCacher(ContentResolver contentResolver) {
        return new TempIdCacher(contentResolver);
    }
}
