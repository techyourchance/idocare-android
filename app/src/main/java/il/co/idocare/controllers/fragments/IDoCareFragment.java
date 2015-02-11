package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;


/**
 * This is a wrapper to the standard Fragment class which adds some convenience logic specific
 * to the app.<br>
 * The fragments of this app extend this class.
 */
public abstract class IDoCareFragment extends Fragment {

    IDoCareFragmentCallback mCallback;

    /**
     * Top level fragment =  a fragment which does not have parent in navigation hierarchy
     * @return true if this fragment is a "top level fragment"
     */
    public abstract boolean isTopLevelFragment();

    /**
     * If {@link #isTopLevelFragment() isTopLevelFragment} returns false, then this method should
     * be used to obtain the parent of this fragment in the navigation hierarchy.<br>
     * This information might be used, for example, when navigating to a "non-rtop-level fragment"
     * via Navigation Drawer - in this case the UP button on Action Bar should bring the user to the
     * fragment returned by this method (which might be different from previously shown fragment).
     * @return the class of the navigation hierarchy parent of this fragment, or null (for top
     *         level fragments)
     */
    public abstract Class<? extends IDoCareFragment> getNavHierParentFragment();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (IDoCareFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IDoCareFragmentCallback");
        }

    }

    /**
     * Call to this method replaces the currently shown fragment with a new one
     * @param claz the class of the new fragment.
     * @param addToBackStack whether the old fragment should be added to the back stack.
     * @param args arguments to be set for the new fragment
     */
    public void replaceFragment(Class<? extends IDoCareFragment> claz, boolean addToBackStack,
                                 Bundle args) {
        mCallback.replaceFragment(claz, addToBackStack, args);
    }

    /**
     * The enclosign activity must implament this interface
     */
    public interface IDoCareFragmentCallback {

        /**
         * Call to this method replaces the currently shown fragment with a new one
         * @param claz the class of the new fragment
         * @param addToBackStack whether the old fragment should be added to the back stack
         * @param args arguments to be set for the new fragment
         */
        public void replaceFragment(Class<? extends IDoCareFragment> claz, boolean addToBackStack,
                                    Bundle args);
    }
}
