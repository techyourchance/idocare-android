package il.co.idocare.mvcviews.signupnative;

import il.co.idocare.mvcviews.ViewMvc;

/**
 * This MVC view represents the signup screen which allows the user to create a new "native" account
 */
public interface SignupNativeViewMvc extends ViewMvc {

    String VIEW_STATE_EMAIL = "email";
    String VIEW_STATE_PASSWORD = "password";
    String VIEW_STATE_REPEAT_PASSWORD = "repeat_password";
    String VIEW_STATE_NICKNAME= "nickname";
    String VIEW_STATE_FIRST_NAME= "first_name";
    String VIEW_STATE_LAST_NAME= "last_name";

    public interface SignupNativeViewMvcListener {
        void onSignupClicked();
        void onChangeUserPictureClicked();
        void onLoginClicked();
    }

    void onSignupInitiated();

    void onSignupCompleted();

    /**
     * Show a picture on signup screen
     * @param picturePath path to a picture in format recognized by UIL
     */
    void showUserPicture(String picturePath);

}
