package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import il.co.idocare.IdcApplication;
import il.co.idocare.dependencyinjection.controller.ControllerComponent;
import il.co.idocare.dependencyinjection.controller.ControllerModule;


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


        mControllerComponent = ((IdcApplication)getActivity().getApplication())
                .getApplicationComponent()
                .newControllerComponent(
                        new ControllerModule((AppCompatActivity) getActivity(), getChildFragmentManager()));

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCallback.showActionBar(shouldShowActionBar());
        mCallback.setTitle(getTitle());
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

    @Override
    public boolean shouldShowActionBar() {
        return true;
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
