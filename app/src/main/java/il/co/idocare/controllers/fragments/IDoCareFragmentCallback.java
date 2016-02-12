package il.co.idocare.controllers.fragments;

import android.app.Fragment;
import android.os.Bundle;

import il.co.idocare.authentication.LoginStateManager;

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
    public void setTitle(String title);


    /**
     * This method will pop up a yes/no dialog asking the user to log in. If the user agrees
     * LoginActivity will be started.
     * @param message the message to show in a dialog
     * @param runnable this runnable will be executed if there will be a logged in user when
     *                 LoginActivity finishes
     */
    public void askUserToLogIn(String message, final Runnable runnable);

}

