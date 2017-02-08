package il.co.idocare.screens.common.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import javax.inject.Inject;

import il.co.idocare.screens.common.Screen;
import il.co.idocare.screens.common.toolbar.ToolbarManager;
import il.co.idocare.screens.requests.fragments.RequestsAllFragment;

/**
 * This is a base class for fragments that represent a full user visible screen
 */
public abstract class BaseScreenFragment extends BaseFragment implements Screen {

    @Inject ToolbarManager mToolbarManager;




    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getControllerComponent().inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbarManager.onScreenShown(this);
    }

    @Override
    public int getToolbarButtonState() {

        boolean hasBackStackEntries = getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0;

        boolean hasHierarchicalParent = getHierarchicalParentFragment() != null;

        boolean showNavigateUpButton = hasBackStackEntries || hasHierarchicalParent;

        if (showNavigateUpButton) {
            return Screen.TOOLBAR_BUTTON_STATE_UP;
        } else {
            return Screen.TOOLBAR_BUTTON_STATE_NAV_DRAWER;
        }
    }

    @Override
    public boolean shouldShowToolbar() {
        return true;
    }

    /**
     * This method returns the hierarchical "parent" of the current fragment. This might be useful
     * to support "up" navigation when back-stack is empty. Default implementation points to
     * {@link RequestsAllFragment}.
     * @return the class of the navigation hierarchy parent of this fragment; null value means
     *         that this fragment is a top level fragment
     */
    public  @Nullable Class<? extends Fragment> getHierarchicalParentFragment() {
        return RequestsAllFragment.class;
    }


}
