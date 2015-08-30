package il.co.idocare.controllers.fragments;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Fragment;
import android.os.Bundle;

import java.io.IOException;

/**
 * The enclosing activity must implement this interface
 */
public interface IDoCareFragmentCallback {

    /**
     * Call to this method replaces the currently shown fragment with a new one
     * @param claz the class of the new fragment
     * @param addToBackStack whether this fragment transaction should be added to the back stack
     * @param clearBackStack if set to true then the back stack will be cleared (note that if
     *                       addToBackStack is true, then the back stack might still contain
     *                       one entry after execution of this method)
     * @param args arguments to be set for the new fragment
     */
    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                boolean clearBackStack, Bundle args);

    /**
     * Change ActionBar title
     */
    public void setActionBarTitle(String title);

    /**
     * This method obtains the auth token for the active account (as specified in SharedPreferences).
     * If no IDoCare accounts registered on the device, or active account is not set, or the auth
     * token is not valid - the user will be prompted for credentials using AccountManager's APIs.
     *
     * This method might block and should not be called on the main thread.
     * @return AccountManagerFuture object as returned by GetAuthToken() of AccountManager.
     */
    public String getAuthTokenForActiveAccount() throws AuthenticatorException,
            OperationCanceledException, IOException;

    /**
     * @return the active account as specified in SharedPreferences, null if no account specified
     */
    public Account getActiveAccount();

}

