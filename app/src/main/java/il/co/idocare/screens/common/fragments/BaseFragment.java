package il.co.idocare.screens.common.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import il.co.idocare.MyApplication;
import il.co.idocare.controllers.fragments.IDoCareFragmentCallback;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;
import il.co.idocare.dependencyinjection.contextscope.ContextModule;
import il.co.idocare.dependencyinjection.controllerscope.ControllerComponent;
import il.co.idocare.dependencyinjection.controllerscope.ControllerModule;
import il.co.idocare.dependencyinjection.datacache.CachersModule;
import il.co.idocare.dependencyinjection.datacache.RetrieversModule;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrator;


/**
 * This class encapsulates logic which is common to all fragments in the application
 */
public abstract class BaseFragment extends Fragment {

    private ControllerComponent mControllerComponent;

    @Inject EventBusRegistrator mEventBusRegistrator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mControllerComponent = ((MyApplication)getActivity().getApplication())
                .getApplicationComponent()
                .newContextComponent(new ContextModule(getActivity()))
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
