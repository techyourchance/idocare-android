package il.co.idocare.dependencyinjection.components;

import dagger.Subcomponent;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.dependencyinjection.ControllerScope;
import il.co.idocare.dependencyinjection.modules.ControllerModule;
import il.co.idocare.networking.ServerHttpRequest;

@ControllerScope
@Subcomponent(modules = ControllerModule.class)
public interface ControllerComponent {

    LoginStateManager loginStateManager();

}
