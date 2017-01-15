package il.co.idocare.controllers.fragments;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.dialogs.events.InfoDialogDismissedEvent;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.mvcviews.loginchooser.LoginChooserViewMvc;
import il.co.idocare.mvcviews.loginchooser.LoginChooserViewMvcImpl;
import il.co.idocare.utils.Logger;

/**
 * This fragment allows the user to choose between multiple signup/login options, or skip login
 */
public class LoginChooserFragment extends AbstractFragment
        implements LoginChooserViewMvc.LoginChooserViewMvcListener {

    private static final String TAG = "LoginChooserFragment";

    /**
     * When LoginChooserFragment is started with this key in arguments, a slide in animation
     * will be played
     */
    public static final String ARG_PLAY_ANIMATION = "arg_play_animation";

    private static final String MULTIPLE_ACCOUNTS_NOT_SUPPORTED_DIALOG_TAG = "MULTIPLE_ACCOUNTS_NOT_SUPPORTED_DIALOG_TAG";

    private LoginChooserViewMvcImpl mLoginChooserViewMVC;

    private CallbackManager mFacebookCallbackManager;

    @Inject LoginStateManager mLoginStateManager;
    @Inject DialogsManager mDialogsManager;
    @Inject Logger mLogger;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getControllerComponent().inject(this);

        mLoginChooserViewMVC = new LoginChooserViewMvcImpl(inflater, container);
        mLoginChooserViewMVC.registerListener(this);

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

    @Override
    public boolean shouldShowActionBar() {
        return false;
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
        if (getArguments() != null && getArguments().containsKey(ARG_PLAY_ANIMATION)) {
            animateButtonsSlideIn();
            getArguments().remove(ARG_PLAY_ANIMATION);
        }

        if (mLoginStateManager.isLoggedIn())
            forbidMultiuserLogin();
    }

    private void animateButtonsSlideIn() {
        final View buttonsLayout =
                mLoginChooserViewMVC.getRootView().findViewById(R.id.login_buttons_layout);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(
                R.dimen.initial_buttons_layout_offset, typedValue, true);
        float initialLoginButtonsLayoutOffset = typedValue.getFloat();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(initialLoginButtonsLayoutOffset, 0);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                buttonsLayout.setTranslationY(value);
                buttonsLayout.requestLayout();
            }
        });
        valueAnimator.start();
    }

    private void forbidMultiuserLogin() {
        // Disallow multiple accounts by showing a dialog which finishes the activity
        mDialogsManager.showInfoDialog(
                null,
                getResources().getString(R.string.no_support_for_multiple_accounts_message),
                getResources().getString(R.string.btn_dialog_close),
                MULTIPLE_ACCOUNTS_NOT_SUPPORTED_DIALOG_TAG);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginSucceededEvent event) {
        LoginChooserFragment.this.dismissProgressDialog();
        finishActivity();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginFailedEvent event) {
        LoginChooserFragment.this.dismissProgressDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInfoDialogDismissed(InfoDialogDismissedEvent event) {
        if (mDialogsManager.getCurrentlyShownDialogTag().equals(MULTIPLE_ACCOUNTS_NOT_SUPPORTED_DIALOG_TAG)) {
            finishActivity();
        } else {
            mLogger.e(TAG, "received unrecognized InfoDialogDismissedEvent");
        }
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Callbacks from MVC view(s)


    @Override
    public void onSkipClicked() {
        mLoginStateManager.setLoginSkipped(true);
        finishActivity();
    }

    @Override
    public void onSignupNativeClicked() {
        replaceFragment(SignupNativeFragment.class, true, false, getArguments());
    }

    @Override
    public void onLoginNativeClicked() {
        replaceFragment(LoginNativeFragment.class, true, false, getArguments());
    }

    // End of callbacks from MVC view(s)
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

            mLoginStateManager.logInFacebook(accessToken);
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
