package il.co.idocare.views;

import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import il.co.idocare.R;

/**
 * Created by Vasiliy on 9/5/2015.
 */
public class LoginChooserViewMVC implements ViewMVC {

    View mRootView;



    public LoginChooserViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.layout_login_chooser, container, false);

        AppCompatButton btnSignUpNative = (AppCompatButton) getRootView().findViewById(R.id.btn_choose_sign_up_native);
        btnSignUpNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new SignUpNativeClickEvent());
            }
        });

        AppCompatButton btnLogInNative = (AppCompatButton) getRootView().findViewById(R.id.btn_choose_log_in_native);
        btnLogInNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new LogInNativeClickEvent());
            }
        });

        TextView txtSkipLogin = (TextView) getRootView().findViewById(R.id.txt_choose_skip_login);
        txtSkipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new SkipLoginClickEvent());
            }
        });
    }


    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        return null;
    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    public static class SignUpNativeClickEvent {}

    public static class LogInNativeClickEvent {}

    public static class SkipLoginClickEvent {}

    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------


}
