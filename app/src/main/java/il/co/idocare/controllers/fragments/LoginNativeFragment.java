package il.co.idocare.controllers.fragments;

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
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import il.co.idocare.R;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.networking.LegacyServerHttpRequest;
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
        if (isLoggedIn()) {
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
        logInNative();
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

        final UserStateManager userStateManager = new UserStateManager(getActivity());

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bundle loginResult = userStateManager.logInNative(username, password);

                if (loginResult.containsKey(UserStateManager.KEY_ERROR_MSG)) {
                    // Login failed - send notification
                    EventBus.getDefault().post(new LoginNativeViewMVC.LoginFailedEvent());
                } else {
                    // Logged in successfully
                    Intent intent = new Intent();
                    intent.putExtras(loginResult);
                    finishActivity(Activity.RESULT_OK, intent, loginResult);
                }

            }
        }).start();
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
