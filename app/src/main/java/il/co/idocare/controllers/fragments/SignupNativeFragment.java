package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.validator.routines.EmailValidator;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Pattern;

import javax.inject.Inject;

import il.co.idocarecore.Constants;
import il.co.idocare.R;
import il.co.idocarecore.authentication.AuthManager;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocarecore.datamodels.pojos.UserSignupData;
import il.co.idocare.dialogs.DialogsFactory;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.dialogs.events.PromptDialogDismissedEvent;
import il.co.idocarecore.eventbusevents.LoginStateEvents;
import il.co.idocare.mvcviews.signupnative.SignupNativeViewMvc;
import il.co.idocare.mvcviews.signupnative.SignupNativeViewMvcImpl;
import il.co.idocarecore.pictures.CameraAdapter;
import il.co.idocarecore.utils.UtilMethods;
import il.co.idocarecore.utils.eventbusregistrator.EventBusRegistrable;

/**
 * This fragment handles native signup flow
 */
@EventBusRegistrable
public class SignupNativeFragment extends AbstractFragment implements SignupNativeViewMvcImpl.SignupNativeViewMvcListener {


    private static final String TAG = "SignupNativeFragment";

    private static final Pattern passwordValidationNoSpaces =
            Pattern.compile("^\\S+$");
    private static final Pattern passwordValidationMinimumLength =
            Pattern.compile("^.*.{6,}$");

    private static final String PICTURE_CAPTURE_DIALOG_TAG = "PICTURE_CAPTURE_DIALOG_TAG";

    private SignupNativeViewMvcImpl mSignupNativeViewMvc;

    @Inject LoginStateManager mLoginStateManager;
    @Inject AuthManager mAuthManager;
    @Inject DialogsManager mDialogsManager;
    @Inject DialogsFactory mDialogsFactory;

    private String mCameraPicturePath;
    private String mUserPicturePath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getControllerComponent().inject(this);

        mSignupNativeViewMvc = new SignupNativeViewMvcImpl(inflater, container);
        mSignupNativeViewMvc.registerListener(this);

        restoreSavedStateIfNeeded(savedInstanceState);

        return mSignupNativeViewMvc.getRootView();
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
            mSignupNativeViewMvc.showUserPicture(mUserPicturePath);
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
        return getString(R.string.title_signup_native);
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    // ---------------------------------------------------------------------------------------------
    //
    // Callbacks from MVC view(s)

    @Override
    public void onSignupClicked() {
        signUpNative();
    }

    @Override
    public void onChangeUserPictureClicked() {
        showAddPictureDialog();
    }

    @Override
    public void onLoginClicked() {
        replaceFragment(LoginNativeFragment.class, true, false, null);
    }

    // End of callbacks from MVC view(s)
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginSucceededEvent event) {
        finishActivity(Activity.RESULT_OK);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginFailedEvent event) {
        mSignupNativeViewMvc.onSignupCompleted();

    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------


    /**
     * Initiate signup server request
     */
    private void signUpNative() {

        final Bundle userDataBundle = mSignupNativeViewMvc.getViewState();

        final String email = userDataBundle.getString(SignupNativeViewMvc.VIEW_STATE_EMAIL);
        final String password = userDataBundle.getString(SignupNativeViewMvc.VIEW_STATE_PASSWORD);
        final String repeatPassword = userDataBundle.getString(SignupNativeViewMvc.VIEW_STATE_REPEAT_PASSWORD);
        final String nickname = userDataBundle.getString(SignupNativeViewMvc.VIEW_STATE_NICKNAME);
        final String firstName = userDataBundle.getString(SignupNativeViewMvc.VIEW_STATE_FIRST_NAME);
        final String lastName = userDataBundle.getString(SignupNativeViewMvc.VIEW_STATE_LAST_NAME);

        // Basic validation of user's input
        if (!validateFields(email, password, repeatPassword, nickname, firstName, lastName)) {
            return;
        }

        UserSignupData userData = new UserSignupData(email, password, nickname,
                firstName, lastName, null, mUserPicturePath);

        // disable user input during signup
        mSignupNativeViewMvc.onSignupInitiated();

        mAuthManager.signUp(userData);

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

        if (!password.equals(repeatPassword))
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_repeat_password_must_match));

        if (TextUtils.isEmpty(firstName))
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_first_name_is_required));
        
        if (TextUtils.isEmpty(lastName))
            errorMessageBuff.append("\n* ").append(getString(R.string.msg_last_name_is_required));

        String errorMessage = errorMessageBuff.toString();

        if (!TextUtils.isEmpty(errorMessage)) {
            DialogFragment dialog = mDialogsFactory.newInfoDialog(
                    "",
                    getString(R.string.msg_resolve_errors) + errorMessage, // TODO: make use of string resource with params
                    getString(R.string.btn_dialog_close)
            );

            mDialogsManager.showRetainedDialogWithTag(dialog, null);

            return false;
        } else {
            return true;
        }

    }

    private void showAddPictureDialog() {
        DialogFragment dialog = mDialogsFactory.newPromptDialog(
                "",
                getString(R.string.user_picture_chooser_dialog_message),
                getString(R.string.user_picture_chooser_dialog_button_caption_new),
                getString(R.string.user_picture_chooser_dialog_button_caption_from_gallery)
        );

        mDialogsManager.showRetainedDialogWithTag(dialog, PICTURE_CAPTURE_DIALOG_TAG);
    }

    @Subscribe
    public void onEvent(PromptDialogDismissedEvent event) {
        if (PICTURE_CAPTURE_DIALOG_TAG.equals(event.getDialogTag())) {
            if (event.getClickedButtonIndex() == PromptDialogDismissedEvent.BUTTON_POSITIVE) {
                takePictureWithCamera();
            } else if (event.getClickedButtonIndex() == PromptDialogDismissedEvent.BUTTON_NEGATIVE) {
                uploadExistingPicture();
            }
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                mUserPicturePath = mCameraPicturePath;
                UtilMethods.adjustCameraPicture(mUserPicturePath);
                mSignupNativeViewMvc.showUserPicture(mUserPicturePath);
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
                mSignupNativeViewMvc.showUserPicture(mUserPicturePath);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void finishActivity(int resultCode) {
        // This code is crap, but no time to think of a better approach
        // TODO: find a solution without casting (or, at least, not at this stage)
        ((LoginActivity)getActivity()).setResult(resultCode, null);
        ((LoginActivity)getActivity()).finish();

        if (getActivity().getIntent().hasExtra(LoginActivity.ARG_LAUNCHED_FROM_STARTUP_ACTIVITY)) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }


}
