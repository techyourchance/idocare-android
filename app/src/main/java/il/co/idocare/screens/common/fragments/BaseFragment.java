package il.co.idocare.screens.common.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import il.co.idocare.MyApplication;
import il.co.idocare.controllers.fragments.IDoCareFragmentCallback;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;
import il.co.idocare.dependencyinjection.contextscope.ContextModule;
import il.co.idocare.dependencyinjection.controllerscope.ControllerComponent;
import il.co.idocare.dependencyinjection.controllerscope.ControllerModule;
import il.co.idocare.dependencyinjection.datacache.CachersModule;
import il.co.idocare.dependencyinjection.datacache.RetrieversModule;


/**
 * This class encapsulates logic which is common to all fragments in the application
 */
public abstract class BaseFragment extends Fragment {

    private ControllerComponent mControllerComponent;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mControllerComponent = ((MyApplication)getActivity().getApplication())
                .getApplicationComponent()
                .newContextComponent(new ContextModule(getActivity()))
                .newControllerComponent(
                        new ControllerModule(getActivity(), getFragmentManager()),
                        new CachersModule(),
                        new RetrieversModule());

    }

    protected ControllerComponent getControllerComponent() {
        return mControllerComponent;
    }

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

}
