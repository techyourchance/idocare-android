package il.co.idocare.controllers.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.FacebookSdk;

import java.io.IOException;

import il.co.idocare.R;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.controllers.fragments.IDoCareFragmentCallback;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;

/**
 * This is a wrapper around a standard Activity class which provides few convenience methods
 */
public abstract class AbstractActivity extends Activity implements
        IDoCareFragmentCallback {

    private static final String LOG_TAG = AbstractActivity.class.getSimpleName();


    private FragmentManager.OnBackStackChangedListener onBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    manageNavigateUpButton();
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);

        manageNavigateUpButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getFragmentManager().removeOnBackStackChangedListener(onBackStackChangedListener);
    }

    @Override
    public void onBackPressed() {
        if (onNavigateUp()) {
            return;
        }

        super.onBackPressed();
    }




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
            // Log.v(LOG_TAG, "the fragment " + claz.getSimpleName() + " is already shown");
            return;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();

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

        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        // Change to a new fragment
        ft.replace(R.id.frame_contents, newFragment, claz.getClass().getSimpleName());
        ft.commit();


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getFragmentManager().executePendingTransactions();
                manageNavigateUpButton();
            }
        });

    }

    /**
     * Check whether a fragment of a specific class is currently shown
     * @param claz class of fragment to test. Null considered as "test no fragment shown"
     * @return true if fragment of the same class (or a superclass) is currently shown
     */
    private boolean isFragmentShown(Class<? extends Fragment> claz) {
        Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);


        return (currFragment == null && claz == null) || (
                currFragment != null && claz.isInstance(currFragment));
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
        Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);
        if (currFragment != null)
            currFragment.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    // End of fragments management
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Action bar management

    public void setActionBarTitle(String title) {

        if (getActionBar() != null ) {
            getActionBar().show();
            if (!TextUtils.isEmpty(title)) {
                getActionBar().setTitle(title);
            } else {
                getActionBar().setTitle("");
            }
        }
    }

    // End of action bar management
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Up navigation button management

    private void manageNavigateUpButton() {
        if (getActionBar() != null) {
            // The "navigate up" button should be enabled if either there are entries in the
            // back stack, or the currently shown fragment has a hierarchical parent

            boolean hasBackstackEntries = getFragmentManager().getBackStackEntryCount() > 0;

            Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);

            boolean hasHierParent = currFragment != null &&
                    IDoCareFragmentInterface.class.isAssignableFrom(currFragment.getClass()) &&
                    ((IDoCareFragmentInterface)currFragment).getNavHierParentFragment() != null;

            getActionBar().setDisplayHomeAsUpEnabled(hasBackstackEntries || hasHierParent);
        }
    }

    @Override
    public boolean onNavigateUp() {

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            return true;
        } else {
            Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);
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

    // End of up navigation button management
    //
    // ---------------------------------------------------------------------------------------------




    // ---------------------------------------------------------------------------------------------
    //
    // Accounts management

    @Override
    public String getAuthTokenForActiveAccount() throws AuthenticatorException, OperationCanceledException, IOException {

        AccountManagerFuture<Bundle> future = AccountManager.get(this).getAuthTokenByFeatures(
                AccountAuthenticator.ACCOUNT_TYPE_DEFAULT,
                AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT,
                null,
                this,
                null,
                null,
                null,
                null
        );

        return future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
    }

    @Override
    public Account getActiveAccount() {
        Account[] accounts = AccountManager.get(this).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);
        if (accounts.length > 0) {
            return accounts[0];
        } else {
            return null;
        }
    }

    // End of accounts management
    //
    // ---------------------------------------------------------------------------------------------



}
