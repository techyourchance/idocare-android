package il.co.idocare.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import il.co.idocare.dialogs.events.PromptDialogDismissedEvent;
import il.co.idocare.utils.Logger;

/**
 * A dialog that can show title and message and has two buttons. User's actions performed
 * in this dialog will be posted to {@link EventBus} as {@link PromptDialogDismissedEvent}.
 */
public class PromptDialog extends BaseDialog {


    /* package */ static final String ARG_TITLE = "ARG_TITLE";
    /* package */ static final String ARG_MESSAGE = "ARG_MESSAGE";
    /* package */ static final String ARG_POSITIVE_BUTTON_CAPTION = "ARG_POSITIVE_BUTTON_CAPTION";
    /* package */ static final String ARG_NEGATIVE_BUTTON_CAPTION = "ARG_NEGATIVE_BUTTON_CAPTION";

    @Inject EventBus mEventBus;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getControllerComponent().inject(this);
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);
        String positiveButtonCaption = getArguments().getString(ARG_POSITIVE_BUTTON_CAPTION);
        String negativeButtonCaption = getArguments().getString(ARG_NEGATIVE_BUTTON_CAPTION);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(TextUtils.isEmpty(title) ? "" : title)
                .setMessage(TextUtils.isEmpty(message) ? "" : message)
                .setPositiveButton(TextUtils.isEmpty(positiveButtonCaption) ? "" : positiveButtonCaption,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mEventBus.post(new PromptDialogDismissedEvent(getDialogTag(), PromptDialogDismissedEvent.BUTTON_POSITIVE));
                            }
                        })
                .setNegativeButton(TextUtils.isEmpty(negativeButtonCaption) ? "" : negativeButtonCaption,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mEventBus.post(new PromptDialogDismissedEvent(getDialogTag(), PromptDialogDismissedEvent.BUTTON_NEGATIVE));
                            }
                        })
                .create();

        return dialog;
    }

}
