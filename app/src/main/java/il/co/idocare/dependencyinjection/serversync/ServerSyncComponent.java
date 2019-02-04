package il.co.idocare.dependencyinjection.serversync;

import dagger.Subcomponent;
import il.co.idocare.serversync.SyncAdapter;

@ServerSyncScope
@Subcomponent(modules = {ServerSyncModule.class})
public interface ServerSyncComponent {

    void inject(SyncAdapter syncAdapter);

}
