package il.co.idocare.mvcviews.loginnative;

import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;

/**
 * MVC View of the Home screen.
 */
public class LoginNativeViewMvcImpl
        extends AbstractViewMVC<LoginNativeViewMvc.LoginNativeViewMvcListener>
        implements LoginNativeViewMvc {


    View mRootView;

    Button mBtnLogin;
    EditText mEdtUsername;
    EditText mEdtPassword;


    public LoginNativeViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.layout_login_native, container, false);


        mBtnLogin = (Button) mRootView.findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (LoginNativeViewMvcListener listener : getListeners()) {
                    listener.onLoginClicked();
                }
            }
        });

        mEdtUsername = (EditText) mRootView.findViewById(R.id.edt_login_username);
        mEdtPassword = (EditText) mRootView.findViewById(R.id.edt_login_password);
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        Bundle bundle = new Bundle();
        bundle.putString(VIEW_STATE_USERNAME, mEdtUsername.getText().toString());
        bundle.putString(VIEW_STATE_PASSWORD, mEdtPassword.getText().toString());
        return bundle;
    }

    @Override
    public void disableUserInput() {
        // Disable UI components
        mEdtUsername.setTag(mEdtUsername.getKeyListener());
        mEdtUsername.setKeyListener(null);

        mEdtPassword.setTag(mEdtPassword.getKeyListener());
        mEdtPassword.setKeyListener(null);

        mBtnLogin.setEnabled(false);
    }

    @Override
    public void enableUserInput() {

        mEdtUsername.setKeyListener((KeyListener) mEdtUsername.getTag());
        mEdtUsername.setText("");
        mEdtPassword.setKeyListener((KeyListener) mEdtPassword.getTag());
        mEdtPassword.setText("");

        mEdtUsername.requestFocus();

        mBtnLogin.setEnabled(true);
    }

}
