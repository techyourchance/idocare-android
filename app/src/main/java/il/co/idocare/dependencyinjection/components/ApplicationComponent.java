package il.co.idocare.dependencyinjection.components;

import dagger.Component;
import il.co.idocare.dependencyinjection.ApplicationScope;
import il.co.idocare.dependencyinjection.modules.ControllerModule;
import il.co.idocare.dependencyinjection.modules.ApplicationModule;

@ApplicationScope
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    ControllerComponent newControllerComponent(ControllerModule controllerModule);

}