package il.co.idocare.controllers.fragments;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.dialogs.DialogsFactory;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.dialogs.events.InfoDialogDismissedEvent;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.mvcviews.loginnative.LoginNativeViewMvc;
import il.co.idocare.mvcviews.loginnative.LoginNativeViewMvcImpl;
import il.co.idocare.utils.Logger;

/**
 * This fragment handles native login flow
 */
public class LoginNativeFragment extends AbstractFragment implements LoginNativeViewMvc.LoginNativeViewMvcListener {

    private static final String TAG = "LoginNativeFragment";

    private static final String MULTIPLE_ACCOUNTS_NOT_SUPPORTED_DIALOG_TAG = "MULTIPLE_ACCOUNTS_NOT_SUPPORTED_DIALOG_TAG";


    @Inject LoginStateManager mLoginStateManager;
    @Inject DialogsManager mDialogsManager;
    @Inject DialogsFactory mDialogsFactory;
    @Inject Logger mLogger;


    private LoginNativeViewMvc mLoginNativeViewMvc;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLoginNativeViewMvc = new LoginNativeViewMvcImpl(inflater, container);
        mLoginNativeViewMvc.registerListener(this);

        getControllerComponent().inject(this);

        return mLoginNativeViewMvc.getRootView();
    }

    @Override
    public boolean isTopLevelFragment() {
        return false;
    }

    @Override
    public Class<? extends Fragment> getNavHierParentFragment() {
        return LoginChooserFragment.class;
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_login_native);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mLoginStateManager.isLoggedIn()) {
            mDialogsManager.showRetainedDialogWithTag(
                    mDialogsFactory.newInfoDialog(
                            null,
                            getResources().getString(R.string.no_support_for_multiple_accounts_message),
                            getResources().getString(R.string.btn_dialog_close)),
                    MULTIPLE_ACCOUNTS_NOT_SUPPORTED_DIALOG_TAG);
        }
    }

    // ---------------------------------------------------------------------------------------------
    //
    // Callbacks from MVC view(s)

    @Override
    public void onLoginClicked() {
        logInNative();
    }

    @Override
    public void onSignupClicked() {
        replaceFragment(SignupNativeFragment.class, true, false, null);
    }

    // End of callbacks from MVC view(s)
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginSucceededEvent event) {
        Bundle loginResult = new Bundle();
        loginResult.putString(AccountManager.KEY_ACCOUNT_NAME, event.getUsername());
        loginResult.putString(AccountManager.KEY_AUTHTOKEN, event.getAuthToken());
        Intent intent = new Intent();
        intent.putExtras(loginResult);
        finishActivity(Activity.RESULT_OK, intent, loginResult);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginFailedEvent event) {
        // "unfreeze" UI
        mLoginNativeViewMvc.enableUserInput();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInfoDialogDismissed(InfoDialogDismissedEvent event) {
        if (mDialogsManager.getCurrentlyShownDialogTag().equals(MULTIPLE_ACCOUNTS_NOT_SUPPORTED_DIALOG_TAG)) {
            getActivity().finish();
        } else {
            mLogger.e(TAG, "received unrecognized InfoDialogDismissedEvent");
        }
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    /**
     * Initiate login server request
     */
    private void logInNative() {

        final Bundle userDataBundle = mLoginNativeViewMvc.getViewState();

        final String username = userDataBundle.getString(LoginNativeViewMvcImpl.VIEW_STATE_USERNAME);
        final String password = userDataBundle.getString(LoginNativeViewMvcImpl.VIEW_STATE_PASSWORD);

        // "freeze" UI during login
        mLoginNativeViewMvc.disableUserInput();

        mLoginStateManager.logInNative(username, password);

    }


    private void finishActivity(int resultCode, Intent data, Bundle result) {
        // This code is crap, but no time to think of a better approach
        // TODO: find a solution without casting (or, at least, not at this stage)
        ((LoginActivity)getActivity()).setAccountAuthenticatorResult(result);
        ((LoginActivity)getActivity()).setResult(resultCode, data);
        ((LoginActivity)getActivity()).finish();

        if (getActivity().getIntent().hasExtra(LoginActivity.ARG_LAUNCHED_FROM_STARTUP_ACTIVITY)) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }


}
