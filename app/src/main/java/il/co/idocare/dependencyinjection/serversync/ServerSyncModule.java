package il.co.idocare.dependencyinjection.serversync;

import android.content.ContentResolver;

import com.techyourchance.threadposter.BackgroundThreadPoster;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.contentproviders.TransactionsController;
import il.co.idocare.location.ReverseGeocoder;
import il.co.idocarecore.networking.ServerApi;
import il.co.idocarecore.requests.cachers.RequestsCacher;
import il.co.idocarecore.requests.cachers.TempIdCacher;
import il.co.idocarecore.requests.retrievers.RawRequestRetriever;
import il.co.idocarecore.requests.retrievers.TempIdRetriever;
import il.co.idocarecore.serversync.syncers.RequestsSyncer;
import il.co.idocarecore.serversync.syncers.UserActionsSyncer;
import il.co.idocarecore.serversync.syncers.UsersSyncer;
import il.co.idocarecore.useractions.cachers.UserActionCacher;
import il.co.idocarecore.useractions.retrievers.UserActionsRetriever;
import il.co.idocarecore.users.UsersCacher;
import il.co.idocarecore.users.UsersRetriever;
import il.co.idocarecore.utils.Logger;

@Module
public class ServerSyncModule {

    @Provides
    @ServerSyncScope
    RequestsSyncer requestsSyncer(RequestsCacher requestsCacher,
                                  RawRequestRetriever rawRequestsRetriever,
                                  TransactionsController transactionsController,
                                  TempIdCacher tempIdCacher,
                                  ServerApi serverApi,
                                  ReverseGeocoder reverseGeocoder,
                                  EventBus eventBus,
                                  Logger logger) {
        return new RequestsSyncer(requestsCacher, rawRequestsRetriever, transactionsController,
                tempIdCacher, serverApi, reverseGeocoder, eventBus, logger);
    }

    @Provides
    @ServerSyncScope
    UserActionsSyncer userActionsSyncer(RequestsSyncer requestsSyncer,
                                        BackgroundThreadPoster backgroundThreadPoster,
                                        UserActionsRetriever userActionsRetriever,
                                        UserActionCacher userActionCacher,
                                        TempIdRetriever tempIdRetriever,
                                        ContentResolver contentResolver,
                                        ServerApi serverApi,
                                        Logger logger) {
        return new UserActionsSyncer(requestsSyncer, backgroundThreadPoster, userActionsRetriever,
                userActionCacher, tempIdRetriever, contentResolver, serverApi, logger);
    }
    @Provides
    @ServerSyncScope
    UsersSyncer usersSyncer(UsersRetriever usersRetriever,
                            UsersCacher usersCacher,
                            LoginStateManager loginStateManager,
                            ServerApi serverApi,
                            Logger logger) {
        return new UsersSyncer(usersRetriever, usersCacher, loginStateManager, serverApi, logger);
    }

}
