package il.co.idocare.mvcviews.signupnative;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMvc;
import il.co.idocare.utils.IdcViewUtils;

/**
 * MVC View of the Home screen.
 */
public class SignupNativeViewMvcImpl
        extends AbstractViewMvc<SignupNativeViewMvc.SignupNativeViewMvcListener>
        implements SignupNativeViewMvc {


     private final Button mBtnSignup;
     private final EditText mEdtEmail;
     private final EditText mEdtPassword;
     private final EditText mEdtRepeatPassword;
     private final EditText mEdtNickname;
     private final EditText mEdtFirstName;
     private final EditText mEdtLastName;
     private final ImageView mImgUserPicture;
     private final TextView mTxtAddUserPicture;
     private final TextView mTxtLogin;

    private final View mProgressView;


    public SignupNativeViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_signup_native, container, false));


        mBtnSignup = findViewById(R.id.btn_signup);
        mBtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (SignupNativeViewMvcListener listener : getListeners()) {
                    listener.onSignupClicked();
                }
            }
        });

        mTxtLogin = findViewById(R.id.txt_login);
        mTxtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (SignupNativeViewMvcListener listener : getListeners()) {
                    listener.onLoginClicked();
                }
            }
        });


        mEdtEmail = findViewById(R.id.edt_email);
        mEdtPassword = findViewById(R.id.edt_password);
        mEdtRepeatPassword = findViewById(R.id.edt_confirm_password);
        mEdtNickname = findViewById(R.id.edt_nickname);
        mEdtFirstName = findViewById(R.id.edt_first_name);
        mEdtLastName= findViewById(R.id.edt_last_name);

        mImgUserPicture = findViewById(R.id.img_user_picture);
        mImgUserPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (SignupNativeViewMvcListener listener : getListeners()) {
                    listener.onChangeUserPictureClicked();
                }
            }
        });

        mTxtAddUserPicture = findViewById(R.id.txt_add_user_picture);

        mProgressView = findViewById(R.id.element_progress_overlay);

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


    @Override
    public void onSignupInitiated() {
        IdcViewUtils.showProgressOverlay(mProgressView);
    }

    @Override
    public void onSignupCompleted() {
        IdcViewUtils.hideProgressOverlay(mProgressView);
        mEdtEmail.requestFocus();
    }

    @Override
    public void showUserPicture(String picturePath) {

        ImageLoader.getInstance().displayImage(
                Constants.UIL_LOCAL_FILE_PREFIX + picturePath,
                mImgUserPicture,
                Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);

        mTxtAddUserPicture.setText(getString(R.string.txt_change_user_photo));

    }

}
