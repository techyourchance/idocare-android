package il.co.idocare.dependencyinjection.components;

import dagger.Component;
import il.co.idocare.dependencyinjection.ApplicationScope;
import il.co.idocare.dependencyinjection.modules.ApplicationModule;
import il.co.idocare.dependencyinjection.modules.ControllerModule;

@ApplicationScope
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    ControllerComponent newControllerComponent(ControllerModule controllerModule);
    
//    Application application();
//    AccountManager accountManager();
//    SharedPreferences sharedPreferences();
//    Logger logger();
//    ContentResolverProxy contentResolverProxy();


}