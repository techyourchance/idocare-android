package il.co.idocarecore.screens.common.dialogs;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import static il.co.idocarecore.screens.common.dialogs.DialogsManager.ARGUMENT_KEY_TAG;

/**
 * Base class for all dialogs
 */
public abstract class BaseDialog extends DialogFragment {

    /**
     * Return this dialog's custom tag. Please note that this tag is different
     * bfrom {@link Fragment#getTag()}
     * @return dialog's custom tag, or null if none was set
     */
    protected String getDialogTag() {
        if (getArguments() == null) {
            return null;
        } else {
            return getArguments().getString(ARGUMENT_KEY_TAG);
        }
    }
}
