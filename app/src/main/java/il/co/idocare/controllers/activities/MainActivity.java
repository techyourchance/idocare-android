package il.co.idocare.controllers.activities;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;
import il.co.idocare.controllers.listadapters.NavigationDrawerListAdapter;
import il.co.idocare.controllers.fragments.HomeFragment;
import il.co.idocare.R;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.datamodels.functional.NavigationDrawerEntry;


public class MainActivity extends AbstractActivity {

    private static final String LOG_TAG = "MainActivity";



    private FragmentManager.OnBackStackChangedListener onBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    syncHomeButtonViewAndFunctionality();
                }
            };

    public GoogleApiClient mGoogleApiClient;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private Toolbar mToolbar;

    private NavigationDrawerListAdapter mNavDrawerAdapter;



    // ---------------------------------------------------------------------------------------------
    //
    // Activity lifecycle management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setBackgroundResource(R.drawable.actionbar_background);

        setSupportActionBar(mToolbar);

        initUniversalImageLoader();

        buildGoogleApiClient();

        setupDrawer();

        // Show Home fragment if the app is not restored
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment.class, false, true, null);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) mGoogleApiClient.connect();

        enableAutomaticSync();
        requestImmediateSync();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();

        disableAutomaticSync();
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshDrawer();
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
        mActionBarDrawerToggle.syncState();
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
    // EventBus events handling

    public void onEventMainThread(UserStateManager.UserLoggedInEvent event) {
        refreshDrawer();
    }

    public void onEventMainThread(UserStateManager.UserLoggedOutEvent event) {
        refreshDrawer();
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // Navigation drawer management


    @Override
    public void onBackPressed() {

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        super.onBackPressed();
    }

    /**
     * Initiate the navigation drawer
     */
    private void setupDrawer() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close) {

            private boolean mIsDrawerVisibleLast = false;

            @Override
            public void onDrawerSlide(View view, float v) {
                boolean isDrawerVisible = mDrawerLayout.isDrawerVisible(GravityCompat.START);

                // For performance update the action bar only when the state of drawer changes
                // (otherwise this code will be executed repeatedly while the drawer is being slided)
                if (isDrawerVisible != mIsDrawerVisibleLast) {

                    if (isDrawerVisible) {
                        mToolbar.setTitle("");
                    } else {
                        Fragment currFragment =
                                MainActivity.this.getFragmentManager().findFragmentById(R.id.frame_contents);
                        if (currFragment != null &&
                                IDoCareFragmentInterface.class.isAssignableFrom(currFragment.getClass())) {
                            mToolbar.setTitle(((IDoCareFragmentInterface) currFragment).getTitle());
                        }
                    }

                    MainActivity.this.invalidateOptionsMenu();

                    mIsDrawerVisibleLast = isDrawerVisible;
                }
            }
        };

        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        mActionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavigateUp();
            }
        });

        // This line is required because of a bug. More info:
        //http://stackoverflow.com/questions/26549008/missing-up-navigation-icon-after-switching-from-ics-actionbar-to-lollipop-toolba
        mActionBarDrawerToggle.setHomeAsUpIndicator(getDrawerToggleDelegate().getThemeUpIndicator());

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        setupDrawerListView();
    }

    @SuppressLint("NewApi")
    private void setupDrawerListView() {

        final ListView drawerList = (ListView) findViewById(R.id.drawer_contents);

        // Set the adapter for the list view
        mNavDrawerAdapter = new NavigationDrawerListAdapter(this, 0);
        drawerList.setAdapter(mNavDrawerAdapter);

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Highlight the selected item and close the drawer
                drawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(drawerList);

                String chosenEntry = mNavDrawerAdapter.getItem(position).getTitle();

                onDrawerEntryChosen(chosenEntry);

            }
        });

        refreshDrawer();
    }

    /**
     * Refresh drawer's entries
     */
    private void refreshDrawer() {

        mNavDrawerAdapter.clear();

        // Get the entries
        List<String> entries = new ArrayList<>(
                Arrays.asList(getResources().getStringArray(R.array.nav_drawer_entries)));
        TypedArray icons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        // Remove one of login/logout options (based on connectivity state)
        if (getUserStateManager().isLoggedIn())
            entries.remove(getString(R.string.nav_drawer_entry_login));
        else
            entries.remove(getString(R.string.nav_drawer_entry_logout));

        // Populate the adapter
        for (int i=0; i<entries.size(); i++) {
            mNavDrawerAdapter.add(new NavigationDrawerEntry(entries.get(i), icons.getResourceId(i, 0)));
        }

        mNavDrawerAdapter.notifyDataSetChanged();

        icons.recycle();
    }

    /**
     * This method provides the required functionality to drawer's entries
     */
    private void onDrawerEntryChosen(String chosenEntry) {
        if (chosenEntry.equals(getResources().getString(R.string.nav_drawer_entry_home))) {
            replaceFragment(HomeFragment.class, false, true, null);
        }
        else if (chosenEntry.equals(getResources().getString(R.string.nav_drawer_entry_new_request))) {
            if (getUserStateManager().getActiveAccount() != null) // user logged in - go to new request fragment
                replaceFragment(NewRequestFragment.class, true, false, null);
            else // user isn't logged in - ask him to log in and go to new request fragment if successful
                askUserToLogIn(
                        getResources().getString(R.string.msg_ask_to_log_in_before_new_request),
                        new Runnable() {
                            @Override
                            public void run() {
                                replaceFragment(NewRequestFragment.class, true, false, null);
                            }
                        });
        } else if (chosenEntry.equals(getResources().getString(R.string.nav_drawer_entry_login))) {
            initiateLoginFlow(null);
        }
        else if (chosenEntry.equals(getResources().getString(R.string.nav_drawer_entry_logout))) {
            initiateLogoutFlow(null);
        }
        else {
            Log.e(LOG_TAG, "drawer entry \"" + chosenEntry + "\" has no functionality");
        }
    }


    public void setTitle(String title) {
        mToolbar.setTitle(title);
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


            mActionBarDrawerToggle.setDrawerIndicatorEnabled(!showHomeAsUp);
//            if (showHomeAsUp)
//                getSupportActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);

        }
    }

    // End of up navigation button management
    //
    // ---------------------------------------------------------------------------------------------


    /**
     * Handle the initiation of UIL (third party package under Apache 2.0 license)
     */
    private void initUniversalImageLoader() {
        // TODO: alter the configuration of UIL according to our needs
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS)
                .build();
        ImageLoader.getInstance().init(config);
    }

    /**
     * Initialize the client which will be used to connect to Google Play Services
     */
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
    }


}
