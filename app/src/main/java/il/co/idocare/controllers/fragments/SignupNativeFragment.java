package il.co.idocare.controllers.fragments;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.commons.validator.routines.EmailValidator;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Pattern;

import javax.inject.Inject;


import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.datamodels.pojos.UserSignupNativeData;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.pictures.CameraAdapter;
import il.co.idocare.utils.UtilMethods;
import il.co.idocare.views.SignupNativeViewMVC;

/**
 * This fragment handles native signup flow
 */
public class SignupNativeFragment extends AbstractFragment {


    private static final String LOG_TAG = SignupNativeFragment.class.getSimpleName();

    private static final Pattern passwordValidationNoSpaces =
            Pattern.compile("^\\S+$");
    private static final Pattern passwordValidationMinimumLength =
            Pattern.compile("^.*.{6,}$");
    private static final Pattern passwordValidationHasDigit =
            Pattern.compile("^.*[0-9].*$");

    private SignupNativeViewMVC mSignupNativeViewMVC;

    @Inject
    LoginStateManager mLoginStateManager;

    private AlertDialog mAlertDialog;

    private String mCameraPicturePath;
    private String mUserPicturePath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSignupNativeViewMVC = new SignupNativeViewMVC(inflater, container);

        getControllerComponent().inject(this);

        restoreSavedStateIfNeeded(savedInstanceState);

        return mSignupNativeViewMVC.getRootView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mUserPicturePath))
            outState.putString("user_picture_path", mUserPicturePath);
        if (!TextUtils.isEmpty(mCameraPicturePath))
            outState.putString("camera_picture_path", mCameraPicturePath);
    }

    private void restoreSavedStateIfNeeded(Bundle savedInstanceState) {
        if (savedInstanceState == null) return; // not restoring

        if (savedInstanceState.containsKey("camera_picture_path"))
            mCameraPicturePath = savedInstanceState.getString("camera_picture_path");

        if (savedInstanceState.containsKey("user_picture_path")) {
            mUserPicturePath = savedInstanceState.getString("user_picture_path");
            mSignupNativeViewMVC.showUserPicture(mUserPicturePath);
        }
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
        if (mLoginStateManager.isLoggedIn()) {
            // Disallow multiple accounts by showing a dialog which finishes the activity
            showMultipleAccountsNotAllowedDialog();
        }
    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling


    @Subscribe
    public void onEvent(SignupNativeViewMVC.SignupButtonClickEvent event) {
        signUpNative();
    }

    @Subscribe
    public void onEvent(SignupNativeViewMVC.AddUserPictureClickEvent event) {
        showAddPictureDialog();
    }


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
        // TODO: refactor communication with MVC views to use standard listeners instead of EventBus
        EventBus.getDefault().post(new SignupNativeViewMVC.SignupFailedEvent());
        // TODO: change this toast to an informative dialog
        Toast.makeText(getActivity(),
                getResources().getString(R.string.msg_signup_failed),
                Toast.LENGTH_LONG).show();
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                mUserPicturePath = mCameraPicturePath;
                UtilMethods.adjustCameraPicture(mUserPicturePath);
                mSignupNativeViewMVC.showUserPicture(mUserPicturePath);
            } else {
                // TODO: do we need anything here?
            }
        } else if (requestCode == Constants.REQUEST_CODE_SELECT_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getActivity().getContentResolver()
                        .query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mUserPicturePath = cursor.getString(columnIndex);
                cursor.close();
                mSignupNativeViewMVC.showUserPicture(mUserPicturePath);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

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

        UserSignupNativeData userData = new UserSignupNativeData(email, password, nickname,
                firstName, lastName, null, mUserPicturePath);

        // Notify of signup init
        EventBus.getDefault().post(new SignupNativeViewMVC.SignupRequestSentEvent());

        mLoginStateManager.signUpNative(userData);

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

    private void showMultipleAccountsNotAllowedDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
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
    }

    private void showAddPictureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.msg_choose_add_user_picture_method))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.btn_upload_existing),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mAlertDialog.dismiss();
                                uploadExistingPicture();
                            }
                        })
                .setNegativeButton(getString(R.string.btn_take_new_with_camera),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mAlertDialog.dismiss();
                                takePictureWithCamera();
                            }
                        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void uploadExistingPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, null),
                Constants.REQUEST_CODE_SELECT_PICTURE);
    }

    private void takePictureWithCamera() {
        CameraAdapter cameraAdapter = new CameraAdapter(getActivity());
        mCameraPicturePath = cameraAdapter.takePicture(
                Constants.REQUEST_CODE_TAKE_PICTURE, "new_request");

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
