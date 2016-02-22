package il.co.idocare.dependencyinjection.components;

import dagger.Subcomponent;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.AbstractActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.controllers.activities.StartupActivity;
import il.co.idocare.controllers.fragments.AbstractFragment;
import il.co.idocare.controllers.fragments.CloseRequestFragment;
import il.co.idocare.controllers.fragments.HomeFragment;
import il.co.idocare.controllers.fragments.LoginChooserFragment;
import il.co.idocare.controllers.fragments.LoginNativeFragment;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.controllers.fragments.RequestDetailsFragment;
import il.co.idocare.controllers.fragments.SignupNativeFragment;
import il.co.idocare.dependencyinjection.ControllerScope;
import il.co.idocare.dependencyinjection.modules.ControllerModule;
import il.co.idocare.networking.ServerHttpRequest;

@ControllerScope
@Subcomponent(modules = ControllerModule.class)
public interface ControllerComponent {


    void inject(MainActivity activity);
    void inject(StartupActivity activity);

    void inject(LoginChooserFragment fragment);
    void inject(CloseRequestFragment fragment);
    void inject(HomeFragment fragment);
    void inject(LoginNativeFragment fragment);
    void inject(NewRequestFragment fragment);
    void inject(SignupNativeFragment fragment);
    void inject(RequestDetailsFragment fragment);

}
