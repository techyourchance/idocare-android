package il.co.idocare.dependencyinjection.controllerscope;

import dagger.Subcomponent;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.controllers.activities.StartupActivity;
import il.co.idocare.dialogs.InfoDialog;
import il.co.idocare.screens.requestdetails.fragments.CloseRequestFragment;
import il.co.idocare.screens.common.fragments.BaseFragment;
import il.co.idocare.screens.common.fragments.BaseScreenFragment;
import il.co.idocare.screens.requests.fragments.RequestsAllFragment;
import il.co.idocare.controllers.fragments.LoginChooserFragment;
import il.co.idocare.controllers.fragments.LoginNativeFragment;
import il.co.idocare.screens.requestdetails.fragments.NewAndCloseRequestBaseFragment;
import il.co.idocare.screens.requestdetails.fragments.NewRequestFragment;
import il.co.idocare.screens.requestdetails.fragments.RequestDetailsFragment;
import il.co.idocare.controllers.fragments.SignupNativeFragment;
import il.co.idocare.dialogs.PromptDialog;
import il.co.idocare.screens.navigationdrawer.fragments.NavigationDrawerFragment;
import il.co.idocare.screens.requests.fragments.RequestsListBaseFragment;
import il.co.idocare.screens.requests.fragments.RequestsMyFragment;

@ControllerScope
@Subcomponent(modules = {ControllerModule.class})
public interface ControllerComponent {

    void inject(MainActivity activity);
    void inject(StartupActivity activity);

    void inject(BaseFragment baseFragment);
    void inject(BaseScreenFragment baseScreenFragment);
    void inject(RequestsListBaseFragment requestsListBaseFragment);
    void inject(NewAndCloseRequestBaseFragment fragment);
    void inject(LoginChooserFragment fragment);
    void inject(CloseRequestFragment fragment);
    void inject(RequestsAllFragment fragment);
    void inject(RequestsMyFragment fragment);
    void inject(LoginNativeFragment fragment);
    void inject(NewRequestFragment fragment);
    void inject(SignupNativeFragment fragment);
    void inject(RequestDetailsFragment fragment);
    void inject(NavigationDrawerFragment navigationDrawerFragment);


    void inject(PromptDialog promptDialog);
    void inject(InfoDialog infoDialog);

}
