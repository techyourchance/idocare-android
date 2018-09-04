package il.co.idocare.mvcviews.mainnavdrawer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMvc;
import il.co.idocare.mvcviews.ViewMvc;

/**
 * This MVC view represents application's main screen which contains NavigationDrawer and a single
 * FrameLayout in which app's screens will be presented
 */
public class MainViewMvc
        extends AbstractViewMvc<MainViewMvc.MainNavDrawerViewMvcListener>
        implements ViewMvc {


    public interface MainNavDrawerViewMvcListener {
        /**
         * Will be called when Navigation Drawer's visibility state changes
         * @param isVisible whether the drawer is visible now
         */
        void onDrawerVisibilityStateChanged(boolean isVisible);

        /**
         * Will be called when "navigate up" button is clicked
         */
        void onNavigateUpClicked();
    }


    @NonNull
    private final AppCompatActivity mActivity;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;


    public MainViewMvc(@NonNull LayoutInflater inflater,
                       @Nullable ViewGroup container,
                       @NonNull AppCompatActivity activity) {
        mActivity = activity;
        setRootView(inflater.inflate(R.layout.layout_main, container));


        init();
    }


    private void init() {
        initToolbar();
        initNavDrawer();
    }


    private void initToolbar() {
        mToolbar = (Toolbar) getRootView().findViewById(R.id.toolbar);
        mActivity.setSupportActionBar(mToolbar);
    }

    /**
     * Initiate the navigation drawer
     */
    private void initNavDrawer() {

        mDrawerLayout = (DrawerLayout) getRootView().findViewById(R.id.drawer_layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(
                mActivity,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close) {

            private boolean mIsDrawerVisibleLast = false;

            @Override
            public void onDrawerSlide(View view, float v) {

                // Only update when drawer's visibility actually changed
                if (mIsDrawerVisibleLast != isDrawerVisible()) {
                    mIsDrawerVisibleLast = !mIsDrawerVisibleLast;
                    for (MainNavDrawerViewMvcListener listener : getListeners()) {
                        listener.onDrawerVisibilityStateChanged(mIsDrawerVisibleLast);
                    }
                }
            }
        };

        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);

        mActionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (MainNavDrawerViewMvcListener listener : getListeners()) {
                    listener.onNavigateUpClicked();
                }
            }
        });

        // This is required because of a bug. More info:
        //http://stackoverflow.com/questions/26549008/missing-up-navigation-icon-after-switching-from-ics-actionbar-to-lollipop-toolbar
        if (mActivity.getDrawerToggleDelegate() != null) mActionBarDrawerToggle
                .setHomeAsUpIndicator(mActivity.getDrawerToggleDelegate().getThemeUpIndicator());

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

    }


    @Override
    public Bundle getViewState() {
        return null;
    }

    public void setTitle(String title) {
        mToolbar.setTitle(title);
    }

    /**
     * See {@link ActionBarDrawerToggle#syncState()}
     */
    public void syncDrawerToggleState() {
        mActionBarDrawerToggle.syncState();
    }

    /**
     * See {@link ActionBarDrawerToggle#setDrawerIndicatorEnabled(boolean)}
     * @param enabled true un order to show drawer's indicator ("hamburger"); false in order to
     *                show "up" navigation icon
     */
    public void setDrawerIndicatorEnabled(boolean enabled) {
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(enabled);
    }

    public boolean isDrawerVisible() {
        return mDrawerLayout.isDrawerVisible(GravityCompat.START);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void hideToolbar() {
        mToolbar.setVisibility(View.GONE);
    }

    public void showToolbar() {
        mToolbar.setVisibility(View.VISIBLE);
    }

    public ViewGroup getFrameContent() {
        return findViewById(R.id.frame_contents);
    }
}
