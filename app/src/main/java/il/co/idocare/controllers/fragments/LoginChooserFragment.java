package il.co.idocare.controllers.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.views.LoginChooserViewMVC;

/**
 * This fragment allows the user to choose between multiple signup/login options, or skip login
 */
public class LoginChooserFragment extends AbstractFragment {

    private static final String LOG_TAG = LoginChooserFragment.class.getSimpleName();

    private LoginChooserViewMVC mLoginChooserViewMVC;

    private CallbackManager mFacebookCallbackManager;

    private AlertDialog mAlertDialog;


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
        btnLoginFB.setReadPermissions("public_profile", "email");
        btnLoginFB.registerCallback(mFacebookCallbackManager, new LoginFacebookCallback());

        // The below code resizes Facebook login button
        float fbIconScale = 1.45F;
        Drawable drawable = getActivity().getResources().getDrawable(
                com.facebook.R.drawable.com_facebook_button_icon);
        drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*fbIconScale),
                (int)(drawable.getIntrinsicHeight()*fbIconScale));
        btnLoginFB.setCompoundDrawables(drawable, null, null, null);
        btnLoginFB.setCompoundDrawablePadding(getActivity().getResources().
                getDimensionPixelSize(R.dimen.fb_margin_override_textpadding));
        btnLoginFB.setPadding(
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_lr),
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_top),
                0,
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_bottom));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getUserStateManager().isLoggedIn()) {
            // Disallow multiple accounts by showing a dialog which finishes the activity
            if (mAlertDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getResources().getString(R.string.msg_no_support_for_multiple_accounts))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.btn_dialog_close),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ((LoginActivity) getActivity()).finish();
                                    }
                                });
                mAlertDialog = builder.create();
                mAlertDialog.show();
            } else {
                mAlertDialog.show();
            }
        }
    }

    private void finishActivity() {
        if (getActivity().getIntent().hasExtra(LoginActivity.ARG_LAUNCHED_FROM_STARTUP_ACTIVITY)) {
            // If the enclosing activity has this flag then it was launched from StartupActivity.
            // If this is the case, then activity back stack will be empty and we need explicitly
            // designate the activity that should be started
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            // Just finish the enclosing activity and rely on activities back stack
            getActivity().finish();
        }

    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    public void onEvent(LoginChooserViewMVC.SkipLoginClickEvent event) {

        getUserStateManager().setLoginSkipped(true);

        finishActivity();
    }


    public void onEvent(LoginChooserViewMVC.LogInNativeClickEvent event) {
        replaceFragment(LoginNativeFragment.class, true, false, getArguments());
    }

    public void onEvent(LoginChooserViewMVC.SignUpNativeClickEvent event) {
        replaceFragment(SignupNativeFragment.class, true, false, getArguments());
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------

    private class LoginFacebookCallback implements FacebookCallback<LoginResult> {


        @Override
        public void onSuccess(LoginResult loginResult) {
            /*
            Currently we do not support proper FB login flow, but use a temporary hack.
            When the user successfully logs in with FB, a native account is created. Account's
            username is user's email (therefore a permission to obtain email
            from FB is required); account's password is user's UID obtained from FB.
            TODO: implement standard FB authentication scheme
             */

            if (!loginResult.getRecentlyGrantedPermissions().contains("email")) {
                onUserDeclinedEmailPermission();
                return;
            }

            final AccessToken accessToken = loginResult.getAccessToken();

            LoginChooserFragment.this.showProgressDialog("Synchronizing with Facebook",
                    "Please wait while we are synchronizing with your Facebook profile...");

            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (!getUserStateManager().addFacebookAccount(accessToken)) {

                        LoginManager.getInstance().logOut();

                    }

                    LoginChooserFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoginChooserFragment.this.dismissProgressDialog();

                            finishActivity();
                        }
                    });

                }
            }).start();

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


        private void onUserDeclinedEmailPermission() {

            // Email is required at this stage - remove details of FB account,
            // pop up a notification, and do nothing

            LoginManager.getInstance().logOut();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("IDoCare currently requires your email address in order to work. " +
                    "In order to grant us this permission, Facebook login flow requires that " +
                    "you will remove IDoCare from apps list at your Facebook page " +
                    "(Settings->Apps), and then re-try Facebook login." +
                    "\nWe apologize for inconvenience")
                    .setCancelable(false)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Currently nothing to do here
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

}
