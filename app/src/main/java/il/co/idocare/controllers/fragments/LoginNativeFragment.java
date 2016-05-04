package il.co.idocare.controllers.fragments;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.mvcviews.loginnative.LoginNativeViewMvc;
import il.co.idocare.mvcviews.loginnative.LoginNativeViewMvcImpl;

/**
 * This fragment handles native login flow
 */
public class LoginNativeFragment extends AbstractFragment implements LoginNativeViewMvc.LoginNativeViewMvcListener {


    @Inject LoginStateManager mLoginStateManager;

    private LoginNativeViewMvc mLoginNativeViewMvc;
    private AlertDialog mAlertDialog;

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
        return "Log in";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLoginStateManager.isLoggedIn()) {
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

    // ---------------------------------------------------------------------------------------------
    //
    // Callbacks from MVC view(s)

    @Override
    public void onLoginClicked() {
        logInNative();
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
