package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import de.greenrobot.event.EventBus;
import il.co.idocare.MyApplication;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.dependencyinjection.components.ControllerComponent;
import il.co.idocare.dependencyinjection.modules.ControllerModule;


/**
 * This is a wrapper to the standard Fragment class which adds some convenience logic specific
 * to the app.<br>
 * Fragments of this app should extend this class.
 */
public abstract class AbstractFragment extends Fragment implements
        IDoCareFragmentInterface {


    private ControllerComponent mControllerComponent;

    private IDoCareFragmentCallback mCallback;

    private ProgressDialog mProgressDialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (IDoCareFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IDoCareFragmentCallback");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mControllerComponent = ((MyApplication)getActivity().getApplication())
                .getApplicationComponent().newControllerComponent(new ControllerModule(getActivity()));
    }

    // ---------------------------------------------------------------------------------------------
    //
    // Dependency injection

    protected ControllerComponent getControllerComponent() {
        return mControllerComponent;
    }

    // End of dependency injection
    //
    // ---------------------------------------------------------------------------------------------


    /**
     * See {@link IDoCareFragmentCallback#replaceFragment(Class, boolean, boolean, Bundle)}
     */
    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                 boolean clearBackStack, Bundle args) {
        mCallback.replaceFragment(claz, addToBackStack, clearBackStack, args);
    }


    /**
     * See {@link IDoCareFragmentCallback#setTitle(String)}
     */
    public void setActionBarTitle(String title) {
        mCallback.setTitle(title);
    }


    /**
     * See {@link IDoCareFragmentCallback#askUserToLogIn(String, Runnable)}
     */
    public void askUserToLogIn(String message, final Runnable runnable) {
        mCallback.askUserToLogIn(message, runnable);
    }


    /**
     * Show standard (for the app) progress dialog
     */
    public void showProgressDialog(String title, String message) {
        mProgressDialog = ProgressDialog.
                show(getActivity(), title, message, true);
    }

    /**
     * Dismiss the standard progress dialog
     */
    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus configuration

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    // End of EventBus configuration
    //
    // ---------------------------------------------------------------------------------------------


}
