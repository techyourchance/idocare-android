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
import android.view.ViewGroup;

import com.techyourchance.fragmenthelper.FragmentContainerWrapper;
import com.techyourchance.fragmenthelper.FragmentHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.mvcviews.mainnavdrawer.MainViewMvc;
import il.co.idocare.screens.common.MainFrameHelper;
import il.co.idocare.screens.common.toolbar.ToolbarDelegate;
import il.co.idocare.screens.navigationdrawer.NavigationDrawerDelegate;
import il.co.idocare.screens.navigationdrawer.events.NavigationDrawerStateChangeEvent;
import il.co.idocare.screens.requests.fragments.RequestsAllFragment;
import il.co.idocare.serversync.ServerSyncController;
import il.co.idocare.utils.Logger;


public class MainActivity extends AbstractActivity implements
        MainViewMvc.MainNavDrawerViewMvcListener,
        NavigationDrawerDelegate,
        ToolbarDelegate,
        FragmentContainerWrapper {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_GPS = 1;

    public static final String EXTRA_GPS_PERMISSION_REQUEST_RETRY = "EXTRA_GPS_PERMISSION_REQUEST_RETRY";

    @Inject ServerSyncController mServerSyncController;
    @Inject Logger mLogger;
    @Inject EventBus mEventBus;
    @Inject FragmentHelper mFragmentHelper;



    private MainViewMvc mMainViewMvc;



    // ---------------------------------------------------------------------------------------------
    //
    // Activity lifecycle management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getControllerComponent().inject(this);

        mMainViewMvc = new MainViewMvc(LayoutInflater.from(this), null, this);
        mMainViewMvc.registerListener(this);
        setContentView(mMainViewMvc.getRootView());

        // Show Home fragment if the app is not restored
        if (savedInstanceState == null) {
            mFragmentHelper.replaceFragmentAndRemoveCurrentFromHistory(new RequestsAllFragment());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mServerSyncController.enableAutomaticSync();
        mServerSyncController.requestImmediateSync();
        checkPermissions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mServerSyncController.disableAutomaticSync();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mMainViewMvc.syncDrawerToggleState();
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

    @Override
    public void openDrawer() {
        mMainViewMvc.openDrawer();
    }

    @Override
    public void closeDrawer() {
        mMainViewMvc.closeDrawer();
    }

    public void setTitle(String title) {
        mMainViewMvc.setTitle(title);
    }

    @Override
    public void onNavigateUpClicked() {
        mFragmentHelper.navigateUp();
    }

    @Override
    public void onBackPressed() {
        if (mMainViewMvc.isDrawerVisible()) {
            closeDrawer();
        } else {
            mFragmentHelper.navigateBack();
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
        mMainViewMvc.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void showNavDrawerButton() {
        mMainViewMvc.setDrawerIndicatorEnabled(true);
    }

    @Override
    public void hideToolbar() {
        mMainViewMvc.hideToolbar();
    }

    @Override
    public void showToolbar() {
        mMainViewMvc.showToolbar();
    }

    @NonNull
    @Override
    public ViewGroup getFragmentContainer() {
        return mMainViewMvc.getFrameContent();
    }
}
