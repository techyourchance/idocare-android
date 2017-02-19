package il.co.idocare.controllers.activities;

import android.Manifest;
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
import il.co.idocare.screens.common.MainFrameHelper;
import il.co.idocare.screens.common.toolbar.ToolbarDelegate;
import il.co.idocare.screens.navigationdrawer.NavigationDrawerDelegate;
import il.co.idocare.screens.navigationdrawer.events.NavigationDrawerStateChangeEvent;
import il.co.idocare.screens.requests.fragments.RequestsAllFragment;
import il.co.idocare.location.LocationTrackerService;
import il.co.idocare.mvcviews.mainnavdrawer.MainViewMVC;
import il.co.idocare.serversync.ServerSyncController;
import il.co.idocare.utils.Logger;


public class MainActivity extends AbstractActivity implements
        MainViewMVC.MainNavDrawerViewMVCListener,
        NavigationDrawerDelegate,
        ToolbarDelegate {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_GPS = 1;

    public static final String EXTRA_GPS_PERMISSION_REQUEST_RETRY = "EXTRA_GPS_PERMISSION_REQUEST_RETRY";

    @Inject ServerSyncController mServerSyncController;
    @Inject Logger mLogger;
    @Inject EventBus mEventBus;
    @Inject MainFrameHelper mMainFrameHelper;


    private MainViewMVC mMainViewMVC;



    // ---------------------------------------------------------------------------------------------
    //
    // Activity lifecycle management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getControllerComponent().inject(this);

        mMainViewMVC = new MainViewMVC(LayoutInflater.from(this), null, this);
        mMainViewMVC.registerListener(this);
        setContentView(mMainViewMVC.getRootView());

        // Show Home fragment if the app is not restored
        if (savedInstanceState == null) {
            replaceFragment(RequestsAllFragment.class, false, true, null);
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
        mServerSyncController.enableAutomaticSync();
        mServerSyncController.requestImmediateSync();
        checkPermissions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mServerSyncController.disableAutomaticSync();
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
        mEventBus.post(new NavigationDrawerStateChangeEvent(isVisible ?
                NavigationDrawerStateChangeEvent.STATE_OPENED :
                NavigationDrawerStateChangeEvent.STATE_CLOSED));
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


    @Override
    public void openDrawer() {
        mMainViewMVC.openDrawer();
    }

    @Override
    public void closeDrawer() {
        mMainViewMVC.closeDrawer();
    }

    public void setTitle(String title) {
        mMainViewMVC.setTitle(title);
    }


    @Override
    public void onNavigationClick() {
        mMainFrameHelper.navigateUp();
    }

    @Override
    public void onBackPressed() {
        if (mMainViewMVC.isDrawerVisible()) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

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

    @Override
    public void showNavigateUpButton() {
        mMainViewMVC.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void showNavDrawerButton() {
        mMainViewMVC.setDrawerIndicatorEnabled(true);
    }

    @Override
    public void hideToolbar() {
        mMainViewMVC.hideToolbar();
    }

    @Override
    public void showToolbar() {
        mMainViewMVC.showToolbar();
    }
}
