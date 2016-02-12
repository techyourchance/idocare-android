package il.co.idocare.dependencyinjection.components;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import dagger.Component;
import il.co.idocare.dependencyinjection.ApplicationScope;
import il.co.idocare.dependencyinjection.modules.ControllerModule;
import il.co.idocare.dependencyinjection.modules.ApplicationModule;
import il.co.idocare.nonstaticproxies.ContentResolverProxy;
import il.co.idocare.utils.Logger;

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