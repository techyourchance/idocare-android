package il.co.idocare.screens.common;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import il.co.idocare.R;

/**
 * This helper can be used in order to manager the contents of a single FrameLayout
 */

public class MainFrameHelper {


    private FragmentManager mFragmentManager;

    public MainFrameHelper(FragmentManager fragmentManager) {
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
        ft.replace(R.id.frame_contents, newFragment, null);
        ft.commit();
    }

    /**
     * Check whether a fragment of a specific class is currently shown
     * @param claz class of fragment to test. Null considered as "test no fragment shown"
     * @return true if fragment of the same class (or a superclass) is currently shown
     */
    private boolean isFragmentShown(Class<? extends Fragment> claz) {
        Fragment currFragment = mFragmentManager.findFragmentById(R.id.frame_contents);


        return (currFragment == null && claz == null) || (
                currFragment != null && claz.isInstance(currFragment));
    }



}
