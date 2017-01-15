package il.co.idocare.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import il.co.idocare.dialogs.events.InfoDialogDismissedEvent;

/**
 * A dialog that can show title and message and has a single button. User's actions performed
 * in this dialog will be posted to {@link EventBus} as {@link InfoDialogDismissedEvent}.
 */
public class InfoDialog extends BaseDialog {


    /* package */ static final String ARG_TITLE = "ARG_TITLE";
    /* package */ static final String ARG_MESSAGE = "ARG_MESSAGE";
    /* package */ static final String ARG_BUTTON_CAPTION = "ARG_POSITIVE_BUTTON_CAPTION";

    @Inject EventBus mEventBus;

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);
        String buttonCaption = getArguments().getString(ARG_BUTTON_CAPTION);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(TextUtils.isEmpty(title) ? "" : title)
                .setMessage(TextUtils.isEmpty(message) ? "" : message)
                .setPositiveButton(TextUtils.isEmpty(buttonCaption) ? "" : buttonCaption, null)
                .create();

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mEventBus.post(new InfoDialogDismissedEvent(getDialogTag()));
    }
}
