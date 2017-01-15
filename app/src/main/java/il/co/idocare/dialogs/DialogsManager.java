package il.co.idocare.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

/**
 * This object should be used in activities and fragments in order to manage dialogs. Its
 * functionality includes:<br>
 *     1) show common application's dialogs with a single method call<br>
 *     2) manage dialog lifecycle and dependencies between consecutively shown dialogs<br>
 *
 */
public class DialogsManager {


    /**
     * Whenever a dialog is shown with non-empty "tag", the provided tag will be stored in
     * arguments Bundle under this key.
     */
    private static final String ARGUMENT_KEY_TAG = BaseDialog.ARGUMENT_KEY_TAG;

    /**
     * In case Activity or Fragment that instantiated this DialogsManager are re-created (e.g.
     * in case of memory reclaim by OS, orientation change, etc.), we need to be able
     * to get a reference to dialog that might have been shown. This tag will be supplied with
     * all DialogFragment's shown by this DialogsManager and can be used to query
     * {@link FragmentManager} for last shown dialog.
     */
    private static final String DIALOG_FRAGMENT_TAG = "DIALOG_FRAGMENT_TAG";

    private FragmentManager mFragmentManager;

    private DialogFragment mCurrentlyShownDialog;

    public DialogsManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;

        // there might be some dialog already shown
        Fragment fragmentWithDialogTag = fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (fragmentWithDialogTag != null
                && DialogFragment.class.isAssignableFrom(fragmentWithDialogTag.getClass())) {
            mCurrentlyShownDialog = (DialogFragment) fragmentWithDialogTag;
        }
    }

    /**
     * @return a reference to currently shown dialog, or null if no dialog is shown.
     */
    public @Nullable DialogFragment getCurrentlyShownDialog() {
        return mCurrentlyShownDialog;
    }


    /**
     * Obtain the tag of the currently shown dialog. Please note that this tag is not the same
     * as tag referenced by {@link Fragment#getTag()}.
     * @return the tag of the currently shown dialog; null if no dialog is shown, or the currently
     *         shown dialog has no tag
     */
    public @Nullable String getCurrentlyShownDialogTag() {
        if (mCurrentlyShownDialog == null || mCurrentlyShownDialog.getArguments() == null ||
                !mCurrentlyShownDialog.getArguments().containsKey(ARGUMENT_KEY_TAG)) {
            return null;
        } else {
            return mCurrentlyShownDialog.getArguments().getString(ARGUMENT_KEY_TAG);
        }
    }

    /**
     * Check whether a dialog with a specified tag is currently shown
     * @param tag dialog tag to query
     * @return true if a dialog with the given tag is currently shown; false otherwise
     */
    public boolean isDialogCurrentlyShown(String tag) {
        String shownDialogTag = getCurrentlyShownDialogTag();
        return !TextUtils.isEmpty(shownDialogTag) && shownDialogTag.equals(tag);
    }

    /**
     * Dismiss the currently shown dialog. Has no effect if no dialog is shown. Please note that
     * we always allow state loss upon dismissal.
     */
    public void dismissCurrentlyShownDialog() {
        if (mCurrentlyShownDialog != null) {
            mCurrentlyShownDialog.dismissAllowingStateLoss();
            mCurrentlyShownDialog = null;
        }
    }

    /**
     * Show dialog and assign it a given tag. Replaces any other currently shown dialog.<br>
     * The shown dialog will be retained across parent activity re-creation.
     * @param dialog dialog to show
     * @param tag string that designates the dialog (note that this is NOT the tag referenced by
     *            {@link DialogFragment#getTag()}); can be null
     */
    public void showRetainedDialogWithTag(DialogFragment dialog, @Nullable String tag) {
        dismissCurrentlyShownDialog();
        dialog.setRetainInstance(true);
        addTag(dialog, tag);
        showDialog(dialog);
    }

    private void addTag(DialogFragment dialog, String tag) {
        if (!TextUtils.isEmpty(tag)) {
            Bundle args = dialog.getArguments() != null ? dialog.getArguments() : new Bundle(1);
            args.putString(ARGUMENT_KEY_TAG, tag);
            dialog.setArguments(args);
        }
    }

    private void showDialog(DialogFragment dialog) {
        dialog.show(mFragmentManager, DIALOG_FRAGMENT_TAG);
        mCurrentlyShownDialog = dialog;
    }


}
