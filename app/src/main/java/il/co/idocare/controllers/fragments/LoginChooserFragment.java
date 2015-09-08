package il.co.idocare.controllers.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.views.LoginChooserViewMVC;

/**
 *
 */
public class LoginChooserFragment extends AbstractFragment {

    private static final String LOG_TAG = LoginChooserFragment.class.getSimpleName();

    private LoginChooserViewMVC mLoginChooserViewMVC;

    private CallbackManager mFacebookCallbackManager;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLoginChooserViewMVC = new LoginChooserViewMVC(inflater, container);

        initializeFacebookLogin();

        return mLoginChooserViewMVC.getRootView();
    }

    @Override
    public boolean isTopLevelFragment() {
        return true;
    }

    @Override
    public Class<? extends Fragment> getNavHierParentFragment() {
        return null;
    }

    @Override
    public String getTitle() {
        return "";
    }


    private void initializeFacebookLogin() {

        mFacebookCallbackManager = CallbackManager.Factory.create();

        LoginButton btnLoginFB = (LoginButton) mLoginChooserViewMVC.getRootView()
                .findViewById(R.id.btn_choose_facebook_login);
        btnLoginFB.setReadPermissions("public_profile");
        btnLoginFB.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_LONG).show();
                // TODO: complete
            }

            @Override
            public void onCancel() {
                Toast.makeText(getActivity(), "Login canceled", Toast.LENGTH_LONG).show();
                // TODO: complete
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getActivity(), "Login error: " + exception.toString(),
                        Toast.LENGTH_LONG).show();
                // TODO: complete
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }



    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    public void onEvent(LoginChooserViewMVC.SkipLoginClickEvent event) {

        // Write to SharedPreferences an indicator of user willing to skip login
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        prefs.edit().putInt(Constants.LOGIN_SKIPPED_KEY, 1).apply();

        if (getArguments() != null &&
                getArguments().containsKey(LoginActivity.ARG_LAUNCHED_FROM_STARTUP_ACTIVITY)) {
            // If the enclosing activity was launched from StartupActivity then switch to
            // MainActivity (otherwise rely on backstack)
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
        getActivity().finish();
    }


    public void onEvent(LoginChooserViewMVC.LogInNativeClickEvent event) {
        replaceFragment(LoginNativeFragment.class, true, false, getArguments());
    }

    public void onEvent(LoginChooserViewMVC.SignUpNativeClickEvent event) {
        // TODO: add logic to pop up SignupNativeFragment
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------


}
