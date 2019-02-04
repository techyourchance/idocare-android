package il.co.idocarecore.screens.common.dialogs;

import androidx.fragment.app.DialogFragment;

public interface DialogsFactory {
    DialogFragment newInfoDialog(String title,
                           String message,
                           String buttonCaption);

    DialogFragment newPromptDialog(String title,
                                   String message,
                                   String positiveButtonCaption,
                                   String negativeButtonCaption);
}
