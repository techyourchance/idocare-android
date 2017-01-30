package il.co.idocare.dependencyinjection.serversync;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.networking.newimplementation.ServerApi;
import il.co.idocare.requests.cachers.RequestsCacher;
import il.co.idocare.requests.retrievers.RequestsRetriever;
import il.co.idocare.serversync.syncers.RequestsSyncer;
import il.co.idocare.utils.Logger;

@Module
public class ServerSyncModule {

    @Provides
    @ServerSyncScope
    RequestsSyncer requestsSyncer(RequestsCacher requestsCacher,
                                  RequestsRetriever requestsRetriever,
                                  ServerApi serverApi,
                                  EventBus eventBus,
                                  Logger logger) {
        return new RequestsSyncer(requestsCacher, requestsRetriever, serverApi, eventBus, logger);
    }

}
