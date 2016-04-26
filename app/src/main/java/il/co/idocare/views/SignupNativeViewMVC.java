package il.co.idocare.views;

import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import il.co.idocare.Constants;
import il.co.idocare.R;

/**
 * MVC View of the Home screen.
 */
public class SignupNativeViewMVC implements ViewMVC {

    public static final String VIEW_STATE_EMAIL = "email";
    public static final String VIEW_STATE_PASSWORD = "password";
    public static final String VIEW_STATE_REPEAT_PASSWORD = "repeat_password";
    public static final String VIEW_STATE_NICKNAME= "nickname";
    public static final String VIEW_STATE_FIRST_NAME= "first_name";
    public static final String VIEW_STATE_LAST_NAME= "last_name";

    View mRootView;

    Button mBtnSignup;
    EditText mEdtEmail;
    EditText mEdtPassword;
    EditText mEdtRepeatPassword;
    EditText mEdtNickname;
    EditText mEdtFirstName;
    EditText mEdtLastName;
    ImageView mImgUserPicture;
    TextView mTxtAddUserPicture;


    public SignupNativeViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.layout_signup_native, container, false);


        mBtnSignup = (Button) mRootView.findViewById(R.id.btn_signup);
        mBtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new SignupButtonClickEvent());
            }
        });

        mEdtEmail = (EditText) mRootView.findViewById(R.id.edt_email);
        mEdtPassword = (EditText) mRootView.findViewById(R.id.edt_password);
        mEdtRepeatPassword = (EditText) mRootView.findViewById(R.id.edt_repeat_password);
        mEdtNickname = (EditText) mRootView.findViewById(R.id.edt_nickname);
        mEdtFirstName = (EditText) mRootView.findViewById(R.id.edt_first_name);
        mEdtLastName= (EditText) mRootView.findViewById(R.id.edt_last_name);

        mImgUserPicture = (ImageView) mRootView.findViewById(R.id.img_user_picture);
        mImgUserPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new AddUserPictureClickEvent());
            }
        });

        mTxtAddUserPicture = (TextView) mRootView.findViewById(R.id.txt_add_user_picture);

    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        Bundle bundle = new Bundle();
        bundle.putString(VIEW_STATE_EMAIL, mEdtEmail.getText().toString());
        bundle.putString(VIEW_STATE_PASSWORD, mEdtPassword.getText().toString());
        bundle.putString(VIEW_STATE_REPEAT_PASSWORD, mEdtRepeatPassword.getText().toString());
        bundle.putString(VIEW_STATE_NICKNAME, mEdtNickname.getText().toString());
        bundle.putString(VIEW_STATE_FIRST_NAME, mEdtFirstName.getText().toString());
        bundle.putString(VIEW_STATE_LAST_NAME, mEdtLastName.getText().toString());
        return bundle;
    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SignupRequestSentEvent event) {
        authenticationInitiated();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SignupSuccessfulEvent event) {
        // TODO: do we need anything here?
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SignupFailedEvent event) {
        authenticationFailed();
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    private void authenticationInitiated() {
        // Disable UI components
        mEdtEmail.setTag(mEdtEmail.getKeyListener());
        mEdtEmail.setKeyListener(null);

        mEdtPassword.setTag(mEdtPassword.getKeyListener());
        mEdtPassword.setKeyListener(null);

        mEdtRepeatPassword.setTag(mEdtRepeatPassword.getKeyListener());
        mEdtRepeatPassword.setKeyListener(null);

        mEdtNickname.setTag(mEdtNickname.getKeyListener());
        mEdtNickname.setKeyListener(null);

        mEdtFirstName.setTag(mEdtFirstName.getKeyListener());
        mEdtFirstName.setKeyListener(null);

        mEdtLastName.setTag(mEdtLastName.getKeyListener());
        mEdtLastName.setKeyListener(null);

        mBtnSignup.setEnabled(false);
    }

    private void authenticationFailed() {

        mEdtEmail.setKeyListener((KeyListener) mEdtEmail.getTag());
        mEdtEmail.setText("");
        
        mEdtPassword.setKeyListener((KeyListener) mEdtPassword.getTag());
        mEdtPassword.setText("");

        mEdtRepeatPassword.setKeyListener((KeyListener) mEdtRepeatPassword.getTag());
        mEdtRepeatPassword.setText("");

        mEdtNickname.setKeyListener((KeyListener) mEdtNickname.getTag());
        mEdtNickname.setText("");

        mEdtFirstName.setKeyListener((KeyListener) mEdtFirstName.getTag());
        mEdtFirstName.setText("");

        mEdtLastName.setKeyListener((KeyListener) mEdtLastName.getTag());
        mEdtLastName.setText("");
        
        mEdtEmail.requestFocus();

        mBtnSignup.setEnabled(true);
    }

    public void showUserPicture(String picturePath) {

        ImageLoader.getInstance().displayImage(
                Constants.UIL_LOCAL_FILE_PREFIX + picturePath,
                mImgUserPicture,
                Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);

        mTxtAddUserPicture.setText(mRootView.getContext().getString(R.string.txt_change_user_photo));

    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    public static class SignupRequestSentEvent {}

    public static class SignupSuccessfulEvent {}

    public static class SignupFailedEvent {}

    public static class SignupButtonClickEvent {}

    public static class AddUserPictureClickEvent {}

    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------

}
