package il.co.idocare.dialogs;

import android.app.Activity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import il.co.idocare.IdcApplication;
import il.co.idocare.dependencyinjection.controller.ControllerComponent;
import il.co.idocare.dependencyinjection.controller.ControllerModule;

/**
 * Base class for all dialogs
 */
public abstract class BaseDialog extends DialogFragment {

    /**
     * Whenever a dialog is shown with non-empty "tag", the provided tag will be stored in
     * arguments Bundle under this key. Please note that this tag is different from a tag returned
     * by {@link Fragment#getTag()}
     */
    public static final String ARGUMENT_KEY_TAG = "ARGUMENT_KEY_TAG";


    private ControllerComponent mControllerComponent;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mControllerComponent = ((IdcApplication)getActivity().getApplication())
                .getApplicationComponent()
                .newControllerComponent(
                        new ControllerModule((AppCompatActivity) getActivity(), getChildFragmentManager()));
    }

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


    protected ControllerComponent getControllerComponent() {
        return mControllerComponent;
    }

}
