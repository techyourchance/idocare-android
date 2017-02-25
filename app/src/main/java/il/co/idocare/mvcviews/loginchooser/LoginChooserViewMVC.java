package il.co.idocare.mvcviews.loginchooser;

import il.co.idocare.mvcviews.ViewMVC;

/**
 * This MVC view represents "pre-login" screen which allows the user to choose between login
 * mechanisms supported by the app.
 */
public interface LoginChooserViewMvc extends ViewMVC {

    /**
     * This interface should be implemented by classes which instantiate LoginChooserViewMvc in
     * order to get notifications about input events
     */
    interface LoginChooserViewMvcListener {
        void onSkipClicked();
        void onSignupNativeClicked();
        void onLoginNativeClicked();
    }

    public void onLoginInitiated();

    public void onLoginCompleted();

}
