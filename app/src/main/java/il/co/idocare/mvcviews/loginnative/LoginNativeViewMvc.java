package il.co.idocare.mvcviews.loginnative;

import il.co.idocarecore.screens.common.mvcviews.ObservableViewMvc;

/**
 * This MVC view represents the login screen which allows the user to log in using its "native"
 * credentials
 */
public interface LoginNativeViewMvc
        extends ObservableViewMvc<LoginNativeViewMvc.LoginNativeViewMvcListener> {

    public static final String VIEW_STATE_USERNAME = "username";
    public static final String VIEW_STATE_PASSWORD = "password";

    /**
     * This interface should be implemented by classes which instantiate LoginNativeViewMvc in
     * order to get notifications about input events
     */
    interface LoginNativeViewMvcListener {
        void onLoginClicked();
        void onSignupClicked();
    }

    void onLoginInitiated();

    void onLoginCompleted();



}
