package il.co.idocare.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.dialogs.events.PromptDialogDismissedEvent;

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

    private TextView mTxtTitle;
    private TextView mTxtMessage;
    private Button mBtnPositive;
    private Button mBtnNegative;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getControllerComponent().inject(this);
    }

    // THIS CODE MAKES THE SCREEN DIM, BUT THE ACTUAL LAYOUT IS NOT SHOWN

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//        View rootView = inflater.inflate(R.layout.dialog_info_prompt, container, false);
//
//        initSubViews(rootView);
//
//        populateSubViews();
//
//        return rootView;
//
//    }

    // This is a workaround for the strange behavior of onCreateView (which doesn't show dialog's layout)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_info_prompt, null);
        dialogBuilder.setView(dialogView);

        initSubViews(dialogView);

        populateSubViews();

        setCancelable(false);

        return dialogBuilder.create();
    }

    private void initSubViews(View rootView) {
        mTxtTitle = (TextView) rootView.findViewById(R.id.txt_dialog_title);
        mTxtMessage = (TextView) rootView.findViewById(R.id.txt_dialog_message);
        mBtnPositive = (Button) rootView.findViewById(R.id.btn_dialog_positive);
        mBtnNegative = (Button) rootView.findViewById(R.id.btn_dialog_negative);

        mBtnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mEventBus.post(new PromptDialogDismissedEvent(getDialogTag(), PromptDialogDismissedEvent.BUTTON_POSITIVE));
            }
        });

        mBtnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mEventBus.post(new PromptDialogDismissedEvent(getDialogTag(), PromptDialogDismissedEvent.BUTTON_NEGATIVE));
            }
        });
    }

    private void populateSubViews() {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);
        String positiveButtonCaption = getArguments().getString(ARG_POSITIVE_BUTTON_CAPTION);
        String negativeButtonCaption = getArguments().getString(ARG_NEGATIVE_BUTTON_CAPTION);

        mTxtTitle.setText(TextUtils.isEmpty(title) ? "" : title);
        mTxtMessage.setText(TextUtils.isEmpty(message) ? "" : message);
        mBtnPositive.setText(positiveButtonCaption);
        mBtnNegative.setText(negativeButtonCaption);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        dismiss();
        mEventBus.post(new PromptDialogDismissedEvent(getDialogTag(), PromptDialogDismissedEvent.BUTTON_NONE));
    }
}
