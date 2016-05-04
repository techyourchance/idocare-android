package il.co.idocare.mvcviews.loginchooser;

import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;

/**
 * Implementation of LoginChooserViewMvc
 */
public class LoginChooserViewMvcImpl
        extends AbstractViewMVC<LoginChooserViewMvc.LoginChooserViewMvcListener>
        implements LoginChooserViewMvc {

    public LoginChooserViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_login_chooser, container, false));

        AppCompatButton btnSignUpNative = (AppCompatButton) getRootView().findViewById(R.id.btn_choose_sign_up_native);
        btnSignUpNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (LoginChooserViewMvcListener listener : getListeners()) {
                    listener.onSignupNativeClicked();
                }
            }
        });

        AppCompatButton btnLogInNative = (AppCompatButton) getRootView().findViewById(R.id.btn_choose_log_in_native);
        btnLogInNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (LoginChooserViewMvcListener listener : getListeners()) {
                    listener.onLoginNativeClicked();
                }
            }
        });

        TextView txtSkipLogin = (TextView) getRootView().findViewById(R.id.txt_choose_skip_login);
        txtSkipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (LoginChooserViewMvcListener listener : getListeners()) {
                    listener.onSkipClicked();
                }
            }
        });
    }

    @Override
    public Bundle getViewState() {
        return null;
    }


}
