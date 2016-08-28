package il.co.idocare.dependencyinjection.controllerscope;

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
import il.co.idocare.dependencyinjection.datacache.CachersModule;
import il.co.idocare.dialogs.PromptDialog;

@ControllerScope
@Subcomponent(modules = {ControllerModule.class, CachersModule.class})
public interface ControllerComponent {

    void inject(MainActivity activity);
    void inject(StartupActivity activity);

    void inject(NewAndCloseRequestBaseFragment fragment);
    void inject(LoginChooserFragment fragment);
    void inject(CloseRequestFragment fragment);
    void inject(HomeFragment fragment);
    void inject(LoginNativeFragment fragment);
    void inject(NewRequestFragment fragment);
    void inject(SignupNativeFragment fragment);
    void inject(RequestDetailsFragment fragment);


    void inject(PromptDialog promptDialog);

}
