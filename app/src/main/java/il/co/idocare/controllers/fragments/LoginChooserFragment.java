package il.co.idocare.controllers.fragments;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import il.co.idocare.authentication.AuthManager;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.dialogs.DialogsFactory;
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

    private LoginChooserViewMvcImpl mLoginChooserViewMVC;

    private CallbackManager mFacebookCallbackManager;

    @Inject LoginStateManager mLoginStateManager;
    @Inject AuthManager mAuthManager;
    @Inject DialogsManager mDialogsManager;
    @Inject DialogsFactory mDialogsFactory;
    @Inject Logger mLogger;


    private boolean mShowFacebookEmailRequiredDialog;

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

        if (mShowFacebookEmailRequiredDialog) {
            mShowFacebookEmailRequiredDialog = false;

            DialogFragment dialogFragment = mDialogsFactory.newInfoDialog(
                    getString(R.string.fb_login_email_required_dialog_title),
                    getString(R.string.fb_login_email_required_dialog_message),
                    getString(R.string.btn_dialog_close)
            );

            mDialogsManager.showRetainedDialogWithTag(dialogFragment, null);
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginSucceededEvent event) {
        mLoginChooserViewMVC.onLoginCompleted();
        finishActivity();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginFailedEvent event) {
        mLoginChooserViewMVC.onLoginCompleted();
    }

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

    private void loginFacebook(AccessToken accessToken) {
        mLoginChooserViewMVC.onLoginInitiated();
        mAuthManager.logInFacebook(accessToken);
    }

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

            loginFacebook(loginResult.getAccessToken());
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

            mShowFacebookEmailRequiredDialog = true;

        }

    }

}
