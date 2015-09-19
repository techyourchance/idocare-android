package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import il.co.idocare.R;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.views.LoginNativeViewMVC;
import il.co.idocare.views.SignupNativeViewMVC;

/**
 * This fragment handles native signup flow
 */
public class SignupNativeFragment extends AbstractFragment {


    private static final String LOG_TAG = SignupNativeFragment.class.getSimpleName();

    private static final Pattern passwordValidationNoSpaces =
            Pattern.compile("^\\S+$");
    private static final Pattern passwordValidationMinimumLength =
            Pattern.compile("^.*.{8,}$");
    private static final Pattern passwordValidationHasDigit =
            Pattern.compile("^.*[0-9].*$");

    private SignupNativeViewMVC mSignupNativeViewMVC;

    private AlertDialog mAlertDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSignupNativeViewMVC = new SignupNativeViewMVC(inflater, container);

        return mSignupNativeViewMVC.getRootView();
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
        return "Sign up";
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(mSignupNativeViewMVC);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(mSignupNativeViewMVC);
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

    public void onEvent(SignupNativeViewMVC.SignupButtonClickEvent event) {
        signUpNative();
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    /**
     * Initiate signup server request
     */
    private void signUpNative() {

        final Bundle userDataBundle = mSignupNativeViewMVC.getViewState();

        final String email = userDataBundle.getString(SignupNativeViewMVC.VIEW_STATE_EMAIL);
        final String password = userDataBundle.getString(SignupNativeViewMVC.VIEW_STATE_PASSWORD);
        final String repeatPassword = userDataBundle.getString(SignupNativeViewMVC.VIEW_STATE_REPEAT_PASSWORD);
        final String nickname = userDataBundle.getString(SignupNativeViewMVC.VIEW_STATE_NICKNAME);
        final String firstName = userDataBundle.getString(SignupNativeViewMVC.VIEW_STATE_FIRST_NAME);
        final String lastName = userDataBundle.getString(SignupNativeViewMVC.VIEW_STATE_LAST_NAME);

        // Basic validation of user's input
        if (!validateFields(email, password, repeatPassword, nickname, firstName, lastName)) {
            return;
        }

        // Notify of signup init
        EventBus.getDefault().post(new SignupNativeViewMVC.SignupRequestSentEvent());

        final UserStateManager userStateManager = new UserStateManager(getActivity());

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bundle signupResult = userStateManager.signUpNative(email, password, nickname,
                        firstName, lastName, null);

                if (signupResult.containsKey(UserStateManager.KEY_ERROR_MSG)) {
                    Log.e(LOG_TAG, "Signup failed. Error message: " +
                            signupResult.getString(UserStateManager.KEY_ERROR_MSG));

                    // Signup failed - send notification
                    EventBus.getDefault().post(new SignupNativeViewMVC.SignupFailedEvent());
                    // And notify the user
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.msg_signup_failed),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    // Signed up and successfully - the user is logged in
                    Intent intent = new Intent();
                    intent.putExtras(signupResult);
                    finishActivity(Activity.RESULT_OK, intent, signupResult);
                }

            }
        }).start();
    }

    private boolean validateFields(String email, String password, String repeatPassword,
                                   String nickname, String firstName, String lastName) {
        StringBuffer errorMessageBuff = new StringBuffer();

        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(email))
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_illegal_email));

        if (!passwordValidationMinimumLength.matcher(password).find())
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_password_must_not_be_short));

        if (!passwordValidationNoSpaces.matcher(password).find())
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_password_must_not_contain_spaces));

        if (!passwordValidationHasDigit.matcher(password).find())
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_password_must_contain_digit));

        if (!password.equals(repeatPassword))
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_repeat_password_must_match));

        if (TextUtils.isEmpty(firstName))
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_first_name_is_required));
        
        if (TextUtils.isEmpty(lastName))
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_last_name_is_required));

        String errorMessage = errorMessageBuff.toString();

        if (!TextUtils.isEmpty(errorMessage)) {
            // Show alert dialog with error message
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.msg_resolve_errors) + errorMessage)
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.btn_dialog_close),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mAlertDialog = null;
                                }
                            });

            mAlertDialog = builder.create();
            mAlertDialog.show();
            return false;
        } else {
            return true;
        }

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
