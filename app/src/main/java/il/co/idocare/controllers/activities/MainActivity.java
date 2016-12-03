package il.co.idocare.controllers.activities;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.controllers.fragments.HomeFragment;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;
import il.co.idocare.helpers.FrameHelper;
import il.co.idocare.location.LocationTrackerService;
import il.co.idocare.mvcviews.mainnavdrawer.MainViewMVC;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.screens.common.FrameContainer;
import il.co.idocare.utils.Logger;


public class MainActivity extends AbstractActivity implements
        MainViewMVC.MainNavDrawerViewMVCListener, FrameContainer {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_GPS = 1;

    public static final String EXTRA_GPS_PERMISSION_REQUEST_RETRY = "EXTRA_GPS_PERMISSION_REQUEST_RETRY";

    private FragmentManager.OnBackStackChangedListener onBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    syncHomeButtonViewAndFunctionality();
                }
            };

    @Inject ServerSyncController mServerSyncController;
    @Inject FrameHelper mFrameHelper;
    @Inject Logger mLogger;


    private MainViewMVC mMainViewMVC;


    // ---------------------------------------------------------------------------------------------
    //
    // Activity lifecycle management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getControllerComponent().inject(this);

        mMainViewMVC = new MainViewMVC(LayoutInflater.from(this), null, this);
        mMainViewMVC.setListener(this);
        setContentView(mMainViewMVC.getRootView());

        mFrameHelper.setFrameLayoutId(R.id.frame_contents); // init frame helper

        // Show Home fragment if the app is not restored
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment.class, false, true, null);
        }

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
        EventBus.getDefault().register(this);
        getFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);
        mServerSyncController.enableAutomaticSync();
        mServerSyncController.requestImmediateSync();
        checkPermissions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        getFragmentManager().removeOnBackStackChangedListener(onBackStackChangedListener);
        mServerSyncController.disableAutomaticSync();
    }


    @Override
    protected void onResume() {
        super.onResume();
        syncHomeButtonViewAndFunctionality();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mMainViewMVC.syncDrawerToggleState();
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(EXTRA_GPS_PERMISSION_REQUEST_RETRY)) {
            checkGpsPermission();
        }
    }

    // End of activity lifecycle management
    //
    // ---------------------------------------------------------------------------------------------




    @Override
    public void onDrawerVisibilityStateChanged(boolean isVisible) {
        if (isVisible) {
            mMainViewMVC.setTitle("");
        } else {
            Fragment currFragment =
                    MainActivity.this.getFragmentManager().findFragmentById(R.id.frame_contents);
            if (currFragment != null &&
                    IDoCareFragmentInterface.class.isAssignableFrom(currFragment.getClass())) {
                mMainViewMVC.setTitle(((IDoCareFragmentInterface) currFragment).getTitle());
            }
        }

        MainActivity.this.invalidateOptionsMenu();
    }


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
    // Navigation drawer management



    public void setTitle(String title) {
        mMainViewMVC.setTitle(title);
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


            mMainViewMVC.setDrawerIndicatorEnabled(!showHomeAsUp);
        }
    }


    // End of up navigation button management
    //
    // ---------------------------------------------------------------------------------------------





    // ---------------------------------------------------------------------------------------------
    //
    // Permissions management



    private void checkPermissions() {
        checkGpsPermission();
    }

    private void checkGpsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_GPS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissionsArray,
                                           @NonNull int[] grantResultsArray) {
        List<String> permissions = Arrays.asList(permissionsArray);
        if (requestCode == PERMISSION_REQUEST_GPS) {
            int gpsPermissionIndex = permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION);
            if (gpsPermissionIndex != -1
                    && grantResultsArray[gpsPermissionIndex] == PackageManager.PERMISSION_GRANTED) {
                // no-op: LocationTrackerService will account for GPS permission being granted
            }
        }
    }


    // End of permissions management
    //
    // ---------------------------------------------------------------------------------------------


    @NonNull
    @Override
    public FrameHelper getFrameHelper() {
        return mFrameHelper;
    }

}
