package il.co.idocare.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import static il.co.idocare.eventbusevents.DialogEvents.*;

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
    private DialogsFactory mDialogsFactory;

    private DialogFragment currentlyShownDialog;

    public DialogsManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        mDialogsFactory = newDialogsFactory();

        // there might be some dialog already shown
        Fragment fragmentWithDialogTag = fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (fragmentWithDialogTag != null
                && DialogFragment.class.isAssignableFrom(fragmentWithDialogTag.getClass())) {
            currentlyShownDialog = (DialogFragment) fragmentWithDialogTag;
        }
    }

    /**
     * Factory method (in case we will need to unit test DialogsManager)
     */
    protected DialogsFactory newDialogsFactory() {
        return new DialogsFactory();
    }


    /**
     * @return a reference to currently shown dialog, or null if no dialog is shown.
     */
    public DialogFragment getCurrentlyShownDialog() {
        return currentlyShownDialog;
    }


    /**
     * Obtain the tag of the currently shown dialog. Please note that this tag is not the same
     * as tag referenced by {@link Fragment#getTag()}.
     * @return the tag of the currently shown dialog; null if no dialog is shown, or the currently
     *         shown dialog has no tag
     */
    public String getCurrentlyShownDialogTag() {
        if (currentlyShownDialog == null || currentlyShownDialog.getArguments() == null ||
                !currentlyShownDialog.getArguments().containsKey(ARGUMENT_KEY_TAG)) {
            return null;
        } else {
            return currentlyShownDialog.getArguments().getString(ARGUMENT_KEY_TAG);
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
        if (currentlyShownDialog != null) {
            currentlyShownDialog.dismissAllowingStateLoss();
            currentlyShownDialog = null;
        }
    }


    /**
     * Show InfoDialog. Notifications from the dialog will be posted as
     * {@link InfoDialogDismissedEvent} events on EventBus.<br>
     * The shown dialog will be retained across parent activity re-creation,
     * unless you explicitly invoke {@link DialogFragment#setRetainInstance(boolean)} and set it to
     * false.
     * @param title dialog's title
     * @param message dialog's message
     * @param buttonCaption dialog's button caption
     * @param tag string that designates the dialog (note that this is NOT the tag referenced by
     *            {@link DialogFragment#getTag()}); can be null
     * @return reference to the shown dialog
     */
    public DialogFragment showInfoDialog(String title, String message, String buttonCaption, String tag) {

        InfoDialog infoDialog = mDialogsFactory.newInfoDialog(title, message, buttonCaption);

        addTag(infoDialog, tag);

        showDialog(infoDialog);

        return infoDialog;
    }


    /**
     * Show PromptDialog. Notifications from the dialog will be posted as
     * {@link PromptDialogDismissedEvent} events on EventBus.<br>
     * The shown dialog will be retained across parent activity re-creation,
     * unless you explicitly invoke {@link DialogFragment#setRetainInstance(boolean)} and set it to
     * false.
     * @param title dialog's title
     * @param message dialog's message
     * @param positiveButtonCaption dialog's positive button caption
     * @param negativeButtonCaption dialog's negative button caption
     * @param tag string that designates the dialog (note that this is NOT the tag referenced by
     *            {@link DialogFragment#getTag()}); can be null
     * @return reference to the shown dialog
     */
    public DialogFragment showPromptDialog(String title, String message, String positiveButtonCaption,
                                           String negativeButtonCaption, String tag) {

        PromptDialog promptDialog =
                mDialogsFactory.newPromptDialog(title, message, positiveButtonCaption, negativeButtonCaption);

        addTag(promptDialog, tag);

        showDialog(promptDialog);

        return promptDialog;
    }

    private void addTag(DialogFragment dialog, String tag) {
        if (!TextUtils.isEmpty(tag)) {
            Bundle args = dialog.getArguments() != null ? dialog.getArguments() : new Bundle(1);
            args.putString(ARGUMENT_KEY_TAG, tag);
            dialog.setArguments(args);
        }
    }

    private void showDialog(DialogFragment dialog) {
        dismissCurrentlyShownDialog();
        dialog.setRetainInstance(true);
        dialog.show(mFragmentManager, DIALOG_FRAGMENT_TAG);
        currentlyShownDialog = dialog;
    }


}
