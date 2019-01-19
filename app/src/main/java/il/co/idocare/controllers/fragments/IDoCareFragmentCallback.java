package il.co.idocare.controllers.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;

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
     * Calls to this method will alter the visibility of action bar
     * @param show set to true in order for action bar to be shown; false otherwise
     */
    public void showActionBar(boolean show);

}

