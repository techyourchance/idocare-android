package il.co.idocare.mvcviews.loginchooser;

import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;
import il.co.idocare.utils.IdcViewUtils;

/**
 * Implementation of LoginChooserViewMvc
 */
public class LoginChooserViewMvcImpl
        extends AbstractViewMVC<LoginChooserViewMvc.LoginChooserViewMvcListener>
        implements LoginChooserViewMvc {

    private final View mProgressView;
    private final Button mBtnSignUpNative;
    private final Button mBtnSignInNative;
    private final TextView mTxtSkip;

    public LoginChooserViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_login_chooser, container, false));

        mProgressView = findViewById(R.id.element_progress_overlay);

        mBtnSignUpNative = findViewById(R.id.btn_choose_sign_up_native);
        mBtnSignUpNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (LoginChooserViewMvcListener listener : getListeners()) {
                    listener.onSignupNativeClicked();
                }
            }
        });

        mBtnSignInNative = findViewById(R.id.btn_choose_log_in_native);
        mBtnSignInNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (LoginChooserViewMvcListener listener : getListeners()) {
                    listener.onLoginNativeClicked();
                }
            }
        });

        mTxtSkip = findViewById(R.id.txt_choose_skip_login);
        mTxtSkip.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void onLoginInitiated() {
        IdcViewUtils.showProgressOverlay(mProgressView);
    }

    @Override
    public void onLoginCompleted() {
        IdcViewUtils.hideProgressOverlay(mProgressView);
    }


}
