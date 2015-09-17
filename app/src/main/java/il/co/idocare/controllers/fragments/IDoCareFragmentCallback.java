package il.co.idocare.controllers.fragments;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Fragment;
import android.os.Bundle;

import java.io.IOException;

import il.co.idocare.authentication.UserStateManager;

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
     *
     * @return get a reference to UserStateManager object
     */
    public UserStateManager getUserStateManager();
}

