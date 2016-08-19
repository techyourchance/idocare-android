package il.co.idocare.controllers.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import il.co.idocare.Constants;
import il.co.idocare.MyApplication;
import il.co.idocare.R;
import il.co.idocare.controllers.fragments.IDoCareFragmentCallback;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;
import il.co.idocare.dependencyinjection.contextscope.ContextModule;
import il.co.idocare.dependencyinjection.controllerscope.ControllerComponent;
import il.co.idocare.dependencyinjection.controllerscope.ControllerModule;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.eventbusevents.DialogEvents;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * This is a wrapper around a standard Activity class which provides few convenience methods
 */
public abstract class AbstractActivity extends AppCompatActivity implements
        IDoCareFragmentCallback {

    private static final String TAG = "AbstractActivity";

    private static final String USER_LOGIN_DIALOG_TAG = "USER_LOGIN_DIALOG_TAG";

    @Inject DialogsManager mDialogsManager;

    private ControllerComponent mControllerComponent;

    private Runnable mPostLoginRunnable;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mControllerComponent = ((MyApplication)getApplication()).getApplicationComponent()
                .newContextComponent(new ContextModule(this))
                .newControllerComponent(new ControllerModule(this, getSupportFragmentManager()));

        mPostLoginRunnable = null;

        FacebookSdk.sdkInitialize(getApplicationContext());

        // TODO: alter the configuration of UIL according to our needs
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS)
                .build();
        ImageLoader.getInstance().init(config);

    }

    @Override
    public void onBackPressed() {
        if (onNavigateUp()) {
            return;
        }

        super.onBackPressed();
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
        return mControllerComponent;
    }

    // End of dependency injection
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Fragments management

    // TODO: maybe we need to preserve the state of the replaced fragments?
    @Override
    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                boolean clearBackStack, Bundle args) {

        if (clearBackStack) {
            // Remove all entries from back stack
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
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


    // End of fragments management
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // User state management



    @Override
    public void askUserToLogIn(String message, final Runnable runnable) {

        mDialogsManager.showPromptDialog(
                null,
                message,
                getResources().getString(R.string.btn_dialog_positive),
                getResources().getString(R.string.btn_dialog_negative),
                USER_LOGIN_DIALOG_TAG);

        mPostLoginRunnable = runnable;

    }

    @Subscribe
    public void onPromptDialogDismissed(DialogEvents.PromptDialogDismissedEvent event) {
        if (event.getTag().equals(USER_LOGIN_DIALOG_TAG)) {
            if (event.getClickedButtonIndex() == DialogEvents.PromptDialogDismissedEvent.BUTTON_POSITIVE) {
                initiateLoginFlow();
            } else {
                mPostLoginRunnable = null;
            }
        }
    }

    /**
     * Initiate a flow that will take the user through login process
     */
    public void initiateLoginFlow() {
        Intent intent = new Intent(AbstractActivity.this, LoginActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Constants.REQUEST_CODE_LOGIN:
                // If login succeeded and there is a runnable - run it
                // TODO: ensure tha RESULT_OK indeed means login succeeded
                if (resultCode == Activity.RESULT_OK && mPostLoginRunnable != null) {
                    runOnUiThread(mPostLoginRunnable);
                }
                mPostLoginRunnable = null; // In any case - clear the runnable
                return;

            default:
                break;
        }

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

    // End of user state management
    //
    // ---------------------------------------------------------------------------------------------




}
