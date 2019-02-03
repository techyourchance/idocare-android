package il.co.idocare.screens.common.fragments;

import android.app.Activity;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import il.co.idocare.IdcApplication;
import il.co.idocare.dependencyinjection.controller.ControllerComponent;
import il.co.idocare.dependencyinjection.controller.ControllerModule;
import il.co.idocarecore.utils.eventbusregistrator.EventBusRegistrator;


/**
 * This class encapsulates logic which is common to all fragments in the application
 */
public abstract class BaseFragment extends Fragment {

    private ControllerComponent mControllerComponent;

    @Inject EventBusRegistrator mEventBusRegistrator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mControllerComponent = ((IdcApplication)getActivity().getApplication())
                .getApplicationComponent()
                .newControllerComponent(
                        new ControllerModule(getActivity(), getActivity().getSupportFragmentManager()));

        mControllerComponent.inject(this);

    }

    protected ControllerComponent getControllerComponent() {
        return mControllerComponent;
    }

    @Override
    public void onStart() {
        super.onStart();
        mEventBusRegistrator.registerMembersOfAnnotatedType(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mEventBusRegistrator.unregisterMembersOfAnnotatedType(this);
    }

}
