package il.co.idocare.screens.common.toolbar;

import androidx.annotation.UiThread;

import org.greenrobot.eventbus.Subscribe;

import il.co.idocare.screens.common.Screen;
import il.co.idocare.screens.navigationdrawer.events.NavigationDrawerStateChangeEvent;
import il.co.idocare.utils.Logger;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrable;

/**
 * Implementations of this interface can be used in order to control the toolbar
 */
@UiThread
@EventBusRegistrable
public class ToolbarManager {

    private static final String TAG = "ToolbarManager";

    private ToolbarDelegate mToolbarDelegate;
    private Logger mLogger;

    private Screen mCurrentScreen;

    public ToolbarManager(ToolbarDelegate toolbarDelegate,
                          Logger logger) {
        mToolbarDelegate = toolbarDelegate;
        mLogger = logger;
    }

    public void onScreenShown(Screen screen) {
        mLogger.d(TAG, "onScreenShown(); screen: " + screen);

        mCurrentScreen = screen;

        if (screen.shouldShowToolbar()) {

            mToolbarDelegate.showToolbar();

            updateToolbarTitle();

            updateToolbarButton();


        } else {
            mToolbarDelegate.hideToolbar();
        }
    }

    private void updateToolbarTitle() {
        mToolbarDelegate.setTitle(mCurrentScreen.getTitle());
    }

    private void clearToolbarTitle() {
        mToolbarDelegate.setTitle("");
    }

    private void updateToolbarButton() {
        int toolbarButtonState = mCurrentScreen.getToolbarButtonState();
        switch (toolbarButtonState) {
            case Screen.TOOLBAR_BUTTON_STATE_UP:
                mToolbarDelegate.showNavigateUpButton();
                break;
            case Screen.TOOLBAR_BUTTON_STATE_NAV_DRAWER:
                mToolbarDelegate.showNavDrawerButton();
                break;
            default:
                throw new RuntimeException("unsupported state: " + toolbarButtonState);
        }
    }

    @Subscribe
    public void onDrawerVisibilityStateChange(NavigationDrawerStateChangeEvent event) {
        if (event.getState() == NavigationDrawerStateChangeEvent.STATE_OPENED) {
            clearToolbarTitle();
        } else if (event.getState() == NavigationDrawerStateChangeEvent.STATE_CLOSED) {
            updateToolbarTitle();
        }
    }
}
