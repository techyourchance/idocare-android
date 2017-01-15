package il.co.idocare.dialogs;


import android.os.Bundle;

public class DialogsFactory {

    /**
     * Get a new instance of {@link InfoDialog}.
     * @param title dialog's title
     * @param message dialog's message
     * @param buttonCaption dialog's button caption
     */
    public InfoDialog newInfoDialog(String title, String message, String buttonCaption) {
        Bundle args = new Bundle(3);
        args.putString(InfoDialog.ARG_TITLE, title);
        args.putString(InfoDialog.ARG_MESSAGE, message);
        args.putString(InfoDialog.ARG_BUTTON_CAPTION, buttonCaption);

        InfoDialog infoDialog = new InfoDialog();
        infoDialog.setArguments(args);

        return infoDialog;
    }



    /**
     * Get a new instance of {@link PromptDialog}.
     * @param title dialog's title
     * @param message dialog's message
     * @param positiveButtonCaption dialog's positive button caption
     * @param negativeButtonCaption dialog's negative button caption
     */
    public PromptDialog newPromptDialog(String title, String message, String positiveButtonCaption,
                                        String negativeButtonCaption) {
        Bundle args = new Bundle(4);
        args.putString(PromptDialog.ARG_TITLE, title);
        args.putString(PromptDialog.ARG_MESSAGE, message);
        args.putString(PromptDialog.ARG_POSITIVE_BUTTON_CAPTION, positiveButtonCaption);
        args.putString(PromptDialog.ARG_NEGATIVE_BUTTON_CAPTION, negativeButtonCaption);

        PromptDialog promptDialog = new PromptDialog();
        promptDialog.setArguments(args);

        return promptDialog;
    }

}
