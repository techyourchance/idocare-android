package il.co.idocare.screens.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;

import il.co.idocare.R;
import il.co.idocare.screens.common.fragments.BaseScreenFragment;

/**
 * This helper can be used in order to manager the contents of a single FrameLayout
 */

public class MainFrameHelper {


    private final Activity mActivity;
    private final FragmentManager mFragmentManager;

    public MainFrameHelper(Activity activity, FragmentManager fragmentManager) {
        mActivity = activity;
        mFragmentManager = fragmentManager;
    }


    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                boolean clearBackStack, Bundle args) {

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

        replaceFragment(newFragment, addToBackStack, clearBackStack);
    }


    public void replaceFragment(Fragment newFragment, boolean addToBackStack, boolean clearBackStack) {

        if (clearBackStack) {
            // Remove all entries from back stack
            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction ft = mFragmentManager.beginTransaction();

        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        // Change to a new fragment
        ft.replace(getMainFrameId(), newFragment, null);
        ft.commit();
    }

    public void navigateUp() {
        Fragment currentFragment = mFragmentManager.findFragmentById(getMainFrameId());

        if (mFragmentManager.popBackStackImmediate()) {
            return; // navigated "back" in fragments back-stack
        } else if ( currentFragment != null && currentFragment instanceof BaseScreenFragment) {
            Class<? extends Fragment> hierParentClass =
                    ((BaseScreenFragment)currentFragment).getHierarchicalParentFragment();
            // navigate "up" to hierarchical parent fragment
            replaceFragment(hierParentClass, false, true, null);
        } else if (mActivity.onNavigateUp()) {
            return; // navigated "up" to hierarchical parent activity
        } else {
            mActivity.finish(); // finish the activity as last resort
        }
    }

    private @IdRes int getMainFrameId() {
        return R.id.frame_contents;
    }
}
