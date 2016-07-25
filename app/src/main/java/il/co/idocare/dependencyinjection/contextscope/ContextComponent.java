package il.co.idocare.dependencyinjection.contextscope;

import dagger.Subcomponent;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.controllers.activities.StartupActivity;
import il.co.idocare.controllers.fragments.CloseRequestFragment;
import il.co.idocare.controllers.fragments.HomeFragment;
import il.co.idocare.controllers.fragments.LoginChooserFragment;
import il.co.idocare.controllers.fragments.LoginNativeFragment;
import il.co.idocare.controllers.fragments.NewAndCloseRequestBaseFragment;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.controllers.fragments.RequestDetailsFragment;
import il.co.idocare.controllers.fragments.SignupNativeFragment;
import il.co.idocare.dependencyinjection.controllerscope.ControllerComponent;
import il.co.idocare.dependencyinjection.controllerscope.ControllerModule;
import il.co.idocare.dependencyinjection.controllerscope.ControllerScope;

@ContextScope
@Subcomponent(modules = ContextModule.class)
public interface ContextComponent {

    ControllerComponent newControllerComponent(ControllerModule controllerModule);


}
