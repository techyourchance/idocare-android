package il.co.idocare.controllers.fragments;

import android.app.Fragment;

/**
 *
 */
public interface IDoCareFragmentInterface {


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


    /**
     * This method is used to control ActionBar visibility from Fragment (ActionBar should be
     * shown by default)
     * @return true if the fragment should be shown with action bar; false otherwise
     */
    boolean shouldShowActionBar();

}
