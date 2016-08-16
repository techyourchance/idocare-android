package il.co.idocare.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;


public class InfoDialog extends DialogFragment {


    /**
     * This event will be posted to EventBus when InfoDialog is dismissed
     */
    public static class InfoDialogDismissedEvent {}

    public static final String ARG_TITLE = "ARG_TITLE";
    public static final String ARG_MESSAGE = "ARG_MESSAGE";
    public static final String ARG_BUTTON_CAPTION = "ARG_BUTTON_CAPTION";



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
        EventBus.getDefault().post(new InfoDialogDismissedEvent());
    }
}
