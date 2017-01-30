package il.co.idocare.dependencyinjection.applicationscope;

import dagger.Component;
import il.co.idocare.dependencyinjection.contextscope.ContextComponent;
import il.co.idocare.dependencyinjection.contextscope.ContextModule;
import il.co.idocare.dependencyinjection.controllerscope.ControllerComponent;
import il.co.idocare.dependencyinjection.controllerscope.ControllerModule;
import il.co.idocare.dependencyinjection.datacache.CachersModule;
import il.co.idocare.dependencyinjection.datacache.RetrieversModule;

@ApplicationScope
@Component(modules = {ApplicationModule.class, CachersModule.class, RetrieversModule.class})
public interface ApplicationComponent {

    ContextComponent newContextComponent(ContextModule contextModule);

}