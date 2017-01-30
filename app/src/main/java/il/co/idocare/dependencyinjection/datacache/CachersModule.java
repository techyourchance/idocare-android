package il.co.idocare.dependencyinjection.datacache;

import android.content.ContentResolver;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.requests.cachers.RequestsCacher;
import il.co.idocare.useractions.cachers.UserActionCacher;
import il.co.idocare.utils.Logger;

@Module
public class CachersModule {

    @Provides
    UserActionCacher userActionCacher(ContentResolver contentResolver, Logger logger) {
        return new UserActionCacher(contentResolver, logger);
    }

    @Provides
    RequestsCacher requestsCacher(ContentResolver contentResolver, EventBus eventBus) {
        return new RequestsCacher(contentResolver, eventBus);
    }
}
