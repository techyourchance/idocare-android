package il.co.idocare.views;

import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import il.co.idocare.R;

/**
 * MVC View of the Home screen.
 */
public class LoginNativeViewMVC implements ViewMVC {

    public static final String VIEW_STATE_USERNAME = "username";
    public static final String VIEW_STATE_PASSWORD = "password";

    private final static String LOG_TAG = LoginNativeViewMVC.class.getSimpleName();

    View mRootView;

    Button mBtnLogin;
    EditText mEdtUsername;
    EditText mEdtPassword;


    public LoginNativeViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.layout_login_native, container, false);


        mBtnLogin = (Button) mRootView.findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new LoginButtonClickEvent());
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


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    public void onEventMainThread(LoginRequestSentEvent event) {
        authenticationInitiated();
    }

    public void onEventMainThread(LoginFailedEvent event) {
        authenticationFailed();
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    private void authenticationInitiated() {
        // Disable UI components
        mEdtUsername.setTag(mEdtUsername.getKeyListener());
        mEdtUsername.setKeyListener(null);

        mEdtPassword.setTag(mEdtPassword.getKeyListener());
        mEdtPassword.setKeyListener(null);

        mBtnLogin.setEnabled(false);
    }

    private void authenticationFailed() {

        Toast.makeText(
                getRootView().getContext(),
                getRootView().getContext().getResources().getString(R.string.msg_login_failed),
                Toast.LENGTH_LONG).show();

        mEdtUsername.setKeyListener((KeyListener) mEdtUsername.getTag());
        mEdtUsername.setText("");
        mEdtPassword.setKeyListener((KeyListener) mEdtPassword.getTag());
        mEdtPassword.setText("");

        mEdtUsername.requestFocus();

        mBtnLogin.setEnabled(true);
    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    public static class LoginRequestSentEvent {}

    public static class LoginFailedEvent {}

    public static class LoginButtonClickEvent {}

    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------

}
