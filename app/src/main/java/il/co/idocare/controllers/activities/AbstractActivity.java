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
import android.os.Bundle;

import java.io.IOException;

import il.co.idocare.R;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.controllers.fragments.AbstractFragment;

/**
 * This is a wrapper around a standard Activity class which provides few convenience methods
 */
public abstract class AbstractActivity extends Activity implements
        AbstractFragment.IDoCareFragmentCallback {

    private static final String LOG_TAG = AbstractActivity.class.getSimpleName();
    private static final String ACTIVE_ACCOUNT_NAME_KEY = "idocare_active_account_name";



    // ---------------------------------------------------------------------------------------------
    //
    // Fragments management

    // TODO: maybe we need to preserve the state of the replaced fragments?
    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                Bundle args) {

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


        // If the new fragment is a subclass of AbstractFragment
        if (AbstractFragment.class.isAssignableFrom(claz)) {

            if (((AbstractFragment) newFragment).isTopLevelFragment()) {
                // Top level fragments don't have UP button
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else if (addToBackStack) {
                ft.addToBackStack(null);
            }

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
        Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);


        return (currFragment == null && claz == null) || (
                currFragment != null && claz.isInstance(currFragment));
    }

    // End of fragments management
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Action bar management

    public void setActionBarTitle(int resourceId) {

        if (getActionBar() != null ) {
            getActionBar().show();
            if (resourceId != 0) {
                getActionBar().setTitle(resourceId);
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
    // Accounts management

    @Override
    public String getAuthTokenForActiveAccount() throws AuthenticatorException, OperationCanceledException, IOException {

        AccountManagerFuture<Bundle> future = AccountManager.get(this).getAuthTokenByFeatures(
                AccountAuthenticator.ACCOUNT_TYPE,
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
        Account[] accounts = AccountManager.get(this).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
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
