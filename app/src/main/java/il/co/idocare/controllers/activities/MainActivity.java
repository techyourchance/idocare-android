package il.co.idocare.controllers.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.fragments.HomeFragment;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.loaders.UserInfoLoader;
import il.co.idocare.location.LocationTrackerService;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.utils.Logger;
import il.co.idocare.views.MainNavDrawerViewMVC;


public class MainActivity extends AbstractActivity implements
        MainNavDrawerViewMVC.MainNavDrawerViewMVCListener, LoaderManager.LoaderCallbacks<UserItem> {

    private static final String TAG = "MainActivity";

    private static final int USER_LOADER = 0;

    private FragmentManager.OnBackStackChangedListener onBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    syncHomeButtonViewAndFunctionality();
                }
            };


    @Inject LoginStateManager mLoginStateManager;
    @Inject ServerSyncController mServerSyncController;
    @Inject Logger mLogger;


    private MainNavDrawerViewMVC mMainNavDrawerViewMVC;



    // ---------------------------------------------------------------------------------------------
    //
    // Activity lifecycle management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getControllerComponent().inject(this);

        mMainNavDrawerViewMVC = new MainNavDrawerViewMVC(LayoutInflater.from(this), null, this);
        mMainNavDrawerViewMVC.setListener(this);
        setContentView(mMainNavDrawerViewMVC.getRootView());

        // Show Home fragment if the app is not restored
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment.class, false, true, null);
        }

        getLoaderManager().initLoader(USER_LOADER, null, this);

        startServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mServerSyncController.enableAutomaticSync();
        mServerSyncController.requestImmediateSync();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mServerSyncController.disableAutomaticSync();
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshNavDrawer();
        syncHomeButtonViewAndFunctionality();
        getFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getFragmentManager().removeOnBackStackChangedListener(onBackStackChangedListener);
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mMainNavDrawerViewMVC.syncDrawerToggleState();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        boolean actionsVisibility = !drawerLayout.isDrawerVisible(GravityCompat.START);

        for(int i=0;i<menu.size();i++){
            menu.getItem(i).setVisible(actionsVisibility);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    // End of activity lifecycle management
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // Services management

    private void startServices() {
        this.startService(new Intent(this, LocationTrackerService.class));
    }


    private void stopServices() {
        this.stopService(new Intent(this, LocationTrackerService.class));
    }

    // End of services
    //
    // ---------------------------------------------------------------------------------------------





    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginSucceededEvent event) {
        refreshNavDrawer();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateManager.UserLoggedOutEvent event) {
        refreshNavDrawer();
    }


    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // Navigation drawer management


    private void refreshNavDrawer() {
        mMainNavDrawerViewMVC.refreshDrawer(mLoginStateManager.isLoggedIn());
    }

    @Override
    public void onBackPressed() {
        if (mMainNavDrawerViewMVC.isDrawerVisible()) {
            mMainNavDrawerViewMVC.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onDrawerEntryChosen(String chosenEntry) {
        if (chosenEntry.equals(getString(R.string.nav_drawer_entry_home))) {
            replaceFragment(HomeFragment.class, false, true, null);
        }
        else if (chosenEntry.equals(getString(R.string.nav_drawer_entry_new_request))) {
            if (mLoginStateManager.isLoggedIn()) // user logged in - go to new request fragment
                replaceFragment(NewRequestFragment.class, true, false, null);
            else // user isn't logged in - ask him to log in and go to new request fragment if successful
                askUserToLogIn(
                        getString(R.string.msg_ask_to_log_in_before_new_request),
                        new Runnable() {
                            @Override
                            public void run() {
                                replaceFragment(NewRequestFragment.class, true, false, null);
                            }
                        });
        } else if (chosenEntry.equals(getString(R.string.nav_drawer_entry_login))) {
            initiateLoginFlow(null);
        }
        else if (chosenEntry.equals(getString(R.string.nav_drawer_entry_logout))) {
            initiateLogoutFlow(null);
        }
        else {
            Log.e(TAG, "drawer entry \"" + chosenEntry + "\" has no functionality");
        }
    }

    public void setTitle(String title) {
        mMainNavDrawerViewMVC.setTitle(title);
    }


    @Override
    public void onDrawerVisibilityStateChanged(boolean isVisible) {
        if (isVisible) {
            mMainNavDrawerViewMVC.setTitle("");
        } else {
            Fragment currFragment =
                    MainActivity.this.getFragmentManager().findFragmentById(R.id.frame_contents);
            if (currFragment != null &&
                    IDoCareFragmentInterface.class.isAssignableFrom(currFragment.getClass())) {
                mMainNavDrawerViewMVC.setTitle(((IDoCareFragmentInterface) currFragment).getTitle());
            }
        }

        MainActivity.this.invalidateOptionsMenu();
    }

    @Override
    public void onNavigationClick() {
        onNavigateUp();
    }


    // End of navigation drawer management
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Up navigation button management

    private void syncHomeButtonViewAndFunctionality() {

        /*
         The "navigate up" button should be enabled if either there are entries in the
         back stack, or the currently shown fragment has a hierarchical parent.
         Only top level fragments will have the UP button switched for nav drawer's "hamburger"
          */

        if (getSupportActionBar() != null) {

            boolean hasBackstackEntries = getFragmentManager().getBackStackEntryCount() > 0;

            Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);

            boolean hasHierParent = currFragment != null
                    && IDoCareFragmentInterface.class.isAssignableFrom(currFragment.getClass())
                    && ((IDoCareFragmentInterface)currFragment).getNavHierParentFragment() != null;

            boolean showHomeAsUp = hasBackstackEntries || hasHierParent;


            mMainNavDrawerViewMVC.setDrawerIndicatorEnabled(!showHomeAsUp);
        }
    }


    // End of up navigation button management
    //
    // ---------------------------------------------------------------------------------------------



    /**
     * Initiate a flow that will take the user through logout process
     */
    private void initiateLogoutFlow(@Nullable Runnable runnable) {
        mLoginStateManager.logOut();
        if (runnable != null)
            runOnUiThread(runnable);
    }


    // ---------------------------------------------------------------------------------------------
    //
    // LoaderCallback methods

    @Override
    public Loader<UserItem> onCreateLoader(int id, Bundle args) {
        if (id == USER_LOADER) {
            if (mLoginStateManager.isLoggedIn()) {
                mLogger.d(TAG, "instantiating UserInfoLoader; user ID: " + mLoginStateManager.getActiveAccountUserId());
                return new UserInfoLoader(
                        this,
                        getContentResolver(),
                        mServerSyncController,
                        Long.valueOf(mLoginStateManager.getActiveAccountUserId()));
            } else {
                return null;
            }
        } else {
            mLogger.e(TAG, "onCreateLoader() called with unrecognized id: " + id);
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<UserItem> loader, UserItem data) {
        if (loader.getId() == USER_LOADER) {
            if (data != null)
                mMainNavDrawerViewMVC.bindUserData(data);
            else
                mLogger.e(TAG, "USER_LOADER returned null data");
        }
    }

    @Override
    public void onLoaderReset(Loader<UserItem> loader) {

    }



    // End of LoaderCallback methods
    //
    // ---------------------------------------------------------------------------------------------

}
