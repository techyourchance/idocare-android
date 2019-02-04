package il.co.idocarecore.screens.common.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import il.co.idocarecore.R;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

/**
 * A dialog that can show title and message and has a single button. User's actions performed
 * in this dialog will be posted to {@link EventBus} as {@link InfoDialogDismissedEvent}.
 */
public class InfoDialog extends BaseDialog {

    public static final String ARG_TITLE = "ARG_TITLE";
    public static final String ARG_MESSAGE = "ARG_MESSAGE";
    public static final String ARG_BUTTON_CAPTION = "ARG_POSITIVE_BUTTON_CAPTION";

    private final EventBus mEventBus;

    private TextView mTxtTitle;
    private TextView mTxtMessage;
    private Button mBtnPositive;

    public InfoDialog(EventBus eventBus) {
        mEventBus = eventBus;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_info_prompt, null);
        dialogBuilder.setView(dialogView);

        initSubViews(dialogView);

        populateSubViews();

        setCancelable(true);

        return dialogBuilder.create();
    }

    private void initSubViews(View rootView) {
        mTxtTitle = (TextView) rootView.findViewById(R.id.txt_dialog_title);
        mTxtMessage = (TextView) rootView.findViewById(R.id.txt_dialog_message);
        mBtnPositive = (Button) rootView.findViewById(R.id.btn_dialog_positive);

        // Hide "negative" button - it is used only in PromptDialog
        rootView.findViewById(R.id.btn_dialog_negative).setVisibility(View.GONE);

        mBtnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    private void populateSubViews() {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);
        String positiveButtonCaption = getArguments().getString(ARG_BUTTON_CAPTION);

        mTxtTitle.setText(TextUtils.isEmpty(title) ? "" : title);
        mTxtMessage.setText(TextUtils.isEmpty(message) ? "" : message);
        mBtnPositive.setText(positiveButtonCaption);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mEventBus.post(new InfoDialogDismissedEvent(getDialogTag()));
    }
}
