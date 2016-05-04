package il.co.idocare.mvcviews.loginnative;

import il.co.idocare.mvcviews.ObservableViewMVC;

/**
 * This MVC view represents the login screen which allows the user to log in using its "native"
 * credentials
 */
public interface LoginNativeViewMvc
        extends ObservableViewMVC<LoginNativeViewMvc.LoginNativeViewMvcListener> {

    public static final String VIEW_STATE_USERNAME = "username";
    public static final String VIEW_STATE_PASSWORD = "password";

    /**
     * This interface should be implemented by classes which instantiate LoginNativeViewMvc in
     * order to get notifications about input events
     */
    interface LoginNativeViewMvcListener {
        void onLoginClicked();
    }

    /**
     * Call to this method will make the UI unresponsive to user's input ("freeze UI")
     */
    void disableUserInput();

    /**
     * Call to this method will make the UI responsive to user's input ("unfreeze UI")
     */
    void enableUserInput();



}
