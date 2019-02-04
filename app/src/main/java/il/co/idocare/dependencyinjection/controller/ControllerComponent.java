package il.co.idocare.dependencyinjection.controller;

import dagger.Subcomponent;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.controllers.activities.StartupActivity;
import il.co.idocarecore.screens.common.dialogs.InfoDialog;
import il.co.idocarerequests.screens.requestdetails.fragments.CloseRequestFragment;
import il.co.idocare.screens.common.fragments.BaseFragment;
import il.co.idocare.screens.common.fragments.BaseScreenFragment;
import il.co.idocarerequests.screens.requests.fragments.RequestsAllFragment;
import il.co.idocare.controllers.fragments.LoginChooserFragment;
import il.co.idocare.controllers.fragments.LoginNativeFragment;
import il.co.idocarerequests.screens.requestdetails.fragments.NewAndCloseRequestBaseFragment;
import il.co.idocarerequests.screens.requestdetails.fragments.NewRequestFragment;
import il.co.idocarerequests.screens.requestdetails.fragments.RequestDetailsFragment;
import il.co.idocare.controllers.fragments.SignupNativeFragment;
import il.co.idocarecore.screens.common.dialogs.PromptDialog;
import il.co.idocare.screens.navigationdrawer.fragments.NavigationDrawerFragment;
import il.co.idocarerequests.screens.requests.fragments.RequestsListBaseFragment;
import il.co.idocarerequests.screens.requests.fragments.RequestsMyFragment;

@ControllerScope
@Subcomponent(modules = {ControllerModule.class, ScreensNavigationModule.class})
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
