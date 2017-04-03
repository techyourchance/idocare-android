package il.co.idocare.dependencyinjection.applicationscope;

import dagger.Component;
import il.co.idocare.dependencyinjection.controllerscope.ControllerComponent;
import il.co.idocare.dependencyinjection.controllerscope.ControllerModule;
import il.co.idocare.dependencyinjection.serversync.ServerSyncComponent;
import il.co.idocare.dependencyinjection.serversync.ServerSyncModule;

@ApplicationScope
@Component(
        modules = {
                ApplicationModule.class,
                ContentProviderModule.class,
                CachersModule.class,
                RetrieversModule.class
        }
)
public interface ApplicationComponent {

    ControllerComponent newControllerComponent(ControllerModule controllerModule);

    ServerSyncComponent newServerSyncComponent(ServerSyncModule serverSyncModule);

}