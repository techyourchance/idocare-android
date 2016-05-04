package il.co.idocare.mvcviews.signupnative;

import il.co.idocare.mvcviews.ViewMVC;

/**
 * This MVC view represents the signup screen which allows the user to create a new "native" account
 */
public interface SignupNativeViewMvc extends ViewMVC {

    String VIEW_STATE_EMAIL = "email";
    String VIEW_STATE_PASSWORD = "password";
    String VIEW_STATE_REPEAT_PASSWORD = "repeat_password";
    String VIEW_STATE_NICKNAME= "nickname";
    String VIEW_STATE_FIRST_NAME= "first_name";
    String VIEW_STATE_LAST_NAME= "last_name";


    /**
     * This interface should be implemented by classes which instantiate SignupNativeViewMvc in
     * order to get notifications about input events
     */
    public interface SignupNativeViewMvcListener {
        void onSignupClicked();
        void onChangeUserPictureClicked();
    }

    /**
     * Call to this method will make the UI unresponsive to user's input ("freeze UI")
     */
    void disableUserInput();

    /**
     * Call to this method will make the UI responsive to user's input ("unfreeze UI")
     */
    void enableUserInput();

    /**
     * Show a picture on signup screen
     * @param picturePath path to a picture in format recognized by UIL
     */
    void showUserPicture(String picturePath);

}
