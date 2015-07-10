package il.co.idocare.controllers.interfaces;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Fragment;
import android.os.Bundle;

import java.io.IOException;

/**
 *
 */
public interface MyFragmentInterface {


    /**
     * Top level fragment =  a fragment which does not have parent in navigation hierarchy
     * @return true if this fragment is a "top level fragment"
     */
    public boolean isTopLevelFragment();


    /**
     * If {@link #isTopLevelFragment()} returns false, then this method should
     * be used to obtain the parent of this fragment in the navigation hierarchy.<br>
     * This information might be used, for example, when navigating to a "non-top-level fragment"
     * via Navigation Drawer - in this case the UP button on Action Bar should bring the user to the
     * fragment returned by this method (which might be different from previously shown fragment).
     * @return the class of the navigation hierarchy parent of this fragment, or null (for top
     *         level fragments)
     */
    public Class<? extends Fragment> getNavHierParentFragment();


    /**
     * Get fragment's title
     * @return fragment's title, or null if the fragment does not have a title
     */
    public String getTitle();



    // ---------------------------------------------------------------------------------------------
    //
    // Inner classes and interfaces

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


    // End of inner classes and interfaces
    //
    // ---------------------------------------------------------------------------------------------

}
