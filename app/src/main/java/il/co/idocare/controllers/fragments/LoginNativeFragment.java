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

import de.greenrobot.event.EventBus;
import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.views.LoginNativeViewMVC;

/**
 * This fragment handles native login flow
 */
public class LoginNativeFragment extends AbstractFragment {


    private static final String LOG_TAG = LoginNativeFragment.class.getSimpleName();

    private LoginNativeViewMVC mLoginNativeViewMVC;

    private AlertDialog mAlertDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLoginNativeViewMVC = new LoginNativeViewMVC(inflater, container);

        return mLoginNativeViewMVC.getRootView();
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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(mLoginNativeViewMVC);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(mLoginNativeViewMVC);
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

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    public void onEvent(LoginNativeViewMVC.LoginButtonClickEvent event) {
        // TODO: refactor communication with MVC views to use standard listeners instead of EventBus
        logInNative();
    }

    public void onEventMainThread(LoginStateEvents.LoginSucceededEvent event) {
        Bundle loginResult = new Bundle();
        loginResult.putString(AccountManager.KEY_ACCOUNT_NAME, event.getUsername());
        loginResult.putString(AccountManager.KEY_AUTHTOKEN, event.getAuthToken());
        Intent intent = new Intent();
        intent.putExtras(loginResult);
        finishActivity(Activity.RESULT_OK, intent, loginResult);
    }

    public void onEventMainThread(LoginStateEvents.LoginFailedEvent event) {
        // TODO: refactor communication with MVC views to use standard listeners instead of EventBus
        EventBus.getDefault().post(new LoginNativeViewMVC.LoginFailedEvent());
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    /**
     * Initiate login server request
     */
    private void logInNative() {

        final Bundle userDataBundle = mLoginNativeViewMVC.getViewState();

        final String username = userDataBundle.getString(LoginNativeViewMVC.VIEW_STATE_USERNAME);
        final String password = userDataBundle.getString(LoginNativeViewMVC.VIEW_STATE_PASSWORD);

        // Notify of login init
        EventBus.getDefault().post(new LoginNativeViewMVC.LoginRequestSentEvent());

        LoginStateManager loginStateManager = new LoginStateManager(getActivity(),
                AccountManager.get(getActivity()));
        loginStateManager.logInNative(username, password);

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
