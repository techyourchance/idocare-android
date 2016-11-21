package il.co.idocare.dependencyinjection.serversync;

import dagger.Subcomponent;
import il.co.idocare.dependencyinjection.datacache.CachersModule;
import il.co.idocare.networking.SyncAdapter;

@ServerSyncScope
@Subcomponent(modules = {ServerSyncModule.class})
public interface ServerSyncComponent {

    void inject(SyncAdapter syncAdapter);

}
