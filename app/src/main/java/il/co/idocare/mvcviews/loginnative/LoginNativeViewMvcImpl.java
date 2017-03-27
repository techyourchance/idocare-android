package il.co.idocare.mvcviews.loginnative;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMvc;
import il.co.idocare.utils.IdcViewUtils;

/**
 * MVC View of the Home screen.
 */
public class LoginNativeViewMvcImpl
        extends AbstractViewMvc<LoginNativeViewMvc.LoginNativeViewMvcListener>
        implements LoginNativeViewMvc {

     private final Button mBtnLogin;
     private final TextView mTxtSignup;
     private final EditText mEdtUsername;
     private final EditText mEdtPassword;
    
    private final View mProgressView;


    public LoginNativeViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_login_native, container, false));

        mBtnLogin = findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (LoginNativeViewMvcListener listener : getListeners()) {
                    listener.onLoginClicked();
                }
            }
        });

        mTxtSignup = findViewById(R.id.txt_signup);
        mTxtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (LoginNativeViewMvcListener listener : getListeners()) {
                    listener.onSignupClicked();
                }
            }
        });

        mEdtUsername = findViewById(R.id.edt_login_email);
        mEdtPassword = findViewById(R.id.edt_login_password);

        mProgressView = findViewById(R.id.element_progress_overlay);
    }

    @Override
    public Bundle getViewState() {
        Bundle bundle = new Bundle();
        bundle.putString(VIEW_STATE_USERNAME, mEdtUsername.getText().toString());
        bundle.putString(VIEW_STATE_PASSWORD, mEdtPassword.getText().toString());
        return bundle;
    }

    @Override
    public void onLoginInitiated() {
        IdcViewUtils.showProgressOverlay(mProgressView);
    }

    @Override
    public void onLoginCompleted() {
        IdcViewUtils.hideProgressOverlay(mProgressView);
        mEdtUsername.requestFocus();
    }

}
