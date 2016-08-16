package il.co.idocare.dialogs;


import android.os.Bundle;

public class DialogsFactory {

    /**
     * Instantiate new InfoDialog
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


}
