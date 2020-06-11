package il.co.idocare.screens.common.dialogs;


import android.app.Activity;
import android.os.Bundle;

import javax.inject.Inject;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import il.co.idocarecore.screens.common.dialogs.DialogsFactory;
import il.co.idocarecore.screens.common.dialogs.InfoDialog;
import il.co.idocarecore.screens.common.dialogs.PromptDialog;


public class DialogsFactoryImpl implements DialogsFactory {

    private final FragmentFactory mFragmentFactory;
    private final Activity mActivity;

    @Inject
    public DialogsFactoryImpl(FragmentFactory fragmentFactory, Activity activity) {
        mFragmentFactory = fragmentFactory;
        mActivity = activity;
    }

    /**
     * Get a new instance of {@link InfoDialog}.
     * @param title dialog's title
     * @param message dialog's message
     * @param buttonCaption dialog's button caption
     */
    @Override
    public DialogFragment newInfoDialog(String title, String message, String buttonCaption) {
        Bundle args = new Bundle(3);
        args.putString(InfoDialog.ARG_TITLE, title);
        args.putString(InfoDialog.ARG_MESSAGE, message);
        args.putString(InfoDialog.ARG_BUTTON_CAPTION, buttonCaption);

        DialogFragment fragment = (DialogFragment) mFragmentFactory.instantiate(mActivity.getClassLoader(), InfoDialog.class.getName());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Get a new instance of {@link PromptDialog}.
     * @param title dialog's title
     * @param message dialog's message
     * @param positiveButtonCaption dialog's positive button caption
     * @param negativeButtonCaption dialog's negative button caption
     */
    @Override
    public DialogFragment newPromptDialog(String title, String message, String positiveButtonCaption,
                                        String negativeButtonCaption) {
        Bundle args = new Bundle(4);
        args.putString(PromptDialog.ARG_TITLE, title);
        args.putString(PromptDialog.ARG_MESSAGE, message);
        args.putString(PromptDialog.ARG_POSITIVE_BUTTON_CAPTION, positiveButtonCaption);
        args.putString(PromptDialog.ARG_NEGATIVE_BUTTON_CAPTION, negativeButtonCaption);

        DialogFragment fragment = (DialogFragment) mFragmentFactory.instantiate(mActivity.getClassLoader(), PromptDialog.class.getName());
        fragment.setArguments(args);
        return fragment;
    }

}
