package il.co.idocare.screens.common.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import il.co.idocare.screens.common.toolbar.ToolbarManager;
import il.co.idocarecore.screens.common.Screen;

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
        return Screen.TOOLBAR_BUTTON_STATE_NAV_DRAWER;
    }

    @Override
    public boolean shouldShowToolbar() {
        return true;
    }

}
