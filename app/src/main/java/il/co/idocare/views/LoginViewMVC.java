package il.co.idocare.views;

import android.os.Bundle;
import android.os.Message;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import il.co.idocare.Constants;
import il.co.idocare.R;

/**
 * MVC View of the Home screen.
 */
public class LoginViewMVC extends AbstractViewMVC {

    private final static String LOG_TAG = "LoginViewMVC";

    View mRootView;

    Button mBtnLogin;
    EditText mEdtUsername;
    EditText mEdtPassword;


    public LoginViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.fragment_login, container, false);


        mBtnLogin = (Button) mRootView.findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyOutboxHandlers(Constants.MessageType.V_LOGIN_BUTTON_CLICK.ordinal(),
                        0, 0, null);
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
        bundle.putString("username", mEdtUsername.getText().toString());
        bundle.putString("password", mEdtPassword.getText().toString());
        return bundle;
    }

    @Override
    protected void handleMessage(Message msg) {

        switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {
            case C_AUTHENTICATION_INITIATED:
                authenticationInitiated();
                break;
            case C_AUTHENTICATION_COMPLETED:
                authenticationCompleted();
                break;
            default:
                Log.w(LOG_TAG, "Message of type "
                        + Constants.MESSAGE_TYPE_VALUES[msg.what].toString() + " wasn't consumed");
        }
    }

    private void authenticationInitiated() {
        // Disable UI components
        mEdtUsername.setTag(mEdtUsername.getKeyListener());
        mEdtUsername.setKeyListener(null);

        mEdtPassword.setTag(mEdtPassword.getKeyListener());
        mEdtPassword.setKeyListener(null);

        mBtnLogin.setEnabled(false);
    }

    private void authenticationCompleted() {
        mEdtUsername.setKeyListener((KeyListener) mEdtUsername.getTag());
        mEdtUsername.clearComposingText();
        mEdtPassword.setKeyListener((KeyListener) mEdtPassword.getTag());
        mEdtPassword.clearComposingText();
        mBtnLogin.setEnabled(true);
    }
}
