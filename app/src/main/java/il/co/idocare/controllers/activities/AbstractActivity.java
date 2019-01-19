package il.co.idocare.controllers.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

import il.co.idocare.IdcApplication;
import il.co.idocare.R;
import il.co.idocare.controllers.fragments.IDoCareFragmentCallback;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;
import il.co.idocare.dependencyinjection.controller.ControllerComponent;
import il.co.idocare.dependencyinjection.controller.ControllerModule;
import il.co.idocare.dialogs.DialogsFactory;
import il.co.idocare.dialogs.DialogsManager;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * This is a wrapper around a standard Activity class which provides few convenience methods
 */
public abstract class AbstractActivity extends AppCompatActivity implements
        IDoCareFragmentCallback {


    @Inject DialogsManager mDialogsManager;
    @Inject DialogsFactory mDialogsFactory;

    private ControllerComponent mControllerComponent;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void showActionBar(boolean show) {
        if (getSupportActionBar() != null) {
            if (show)
                getSupportActionBar().show();
            else
                getSupportActionBar().hide();
        }
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Dependency injection

    protected ControllerComponent getControllerComponent() {
        if (mControllerComponent == null) {
            mControllerComponent = ((IdcApplication)getApplication()).getApplicationComponent()
                    .newControllerComponent(
                            new ControllerModule(this, getSupportFragmentManager()));
        }
        return mControllerComponent;
    }

    // End of dependency injection
    //
    // ---------------------------------------------------------------------------------------------

    // TODO: maybe we need to preserve the state of the replaced fragments?
    @Override
    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                boolean clearBackStack, Bundle args) {

        if (clearBackStack) {
            // Remove all entries from back stack
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }


        if (isFragmentShown(claz)) {
            // The requested fragment is already shown - nothing to do
            // Log.v(TAG, "the fragment " + claz.getSimpleName() + " is already shown");
            return;
        }

        // Create new fragment
        Fragment newFragment;

        try {
            newFragment = claz.newInstance();
            if (args != null) newFragment.setArguments(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        // Change to a new fragment
        ft.replace(R.id.frame_contents, newFragment, claz.getClass().getSimpleName());
        ft.commit();
    }

    /**
     * Check whether a fragment of a specific class is currently shown
     * @param claz class of fragment to test. Null considered as "test no fragment shown"
     * @return true if fragment of the same class (or a superclass) is currently shown
     */
    private boolean isFragmentShown(Class<? extends Fragment> claz) {
        Fragment currFragment = getSupportFragmentManager().findFragmentById(R.id.frame_contents);


        return (currFragment == null && claz == null) || (
                currFragment != null && claz.isInstance(currFragment));
    }


    @Override
    public boolean onNavigateUp() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        } else {
            Fragment currFragment = getSupportFragmentManager().findFragmentById(R.id.frame_contents);
            // Check if currently shown fragment is of type IDoCareFragmentInterface
            if (currFragment != null &&
                    IDoCareFragmentInterface.class.isAssignableFrom(currFragment.getClass())) {
                // Get the hierarchical parent of the currently shown fragment
                Class<? extends Fragment> hierParent =
                        ((IDoCareFragmentInterface)currFragment).getNavHierParentFragment();

                if (hierParent != null) {
                    replaceFragment(hierParent, false, true, null);
                    return true;
                }
            }
        }

        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /*
        This code is required in order to support Facebook's LoginButton functionality -
        since we do not use support Fragments, we can't use LoginButton.setFragment() call,
        thus not allowing the Fragments to receive onActivityResult() notifications after FB
        login events. This code redirects the call to the visible fragment.
        NOTE: this code is based on the assumption that all subclasses will have FrameLayout
              named "frame_contents"
         */
        Fragment currFragment = getSupportFragmentManager().findFragmentById(R.id.frame_contents);
        if (currFragment != null)
            currFragment.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }



}
