package il.co.idocare.controllers.activities;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import il.co.idocare.Constants;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;
import il.co.idocare.controllers.listadapters.NavigationDrawerListAdapter;
import il.co.idocare.controllers.fragments.HomeFragment;
import il.co.idocare.R;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.datamodels.functional.NavigationDrawerEntry;


public class MainActivity extends AbstractActivity {

    private static final String LOG_TAG = "MainActivity";


    public GoogleApiClient mGoogleApiClient;



    // ---------------------------------------------------------------------------------------------
    //
    // Activity lifecycle management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Show the action bar (just in case it was hidden)
        if (getActionBar() != null) getActionBar().show();

        // Show Home fragment if the app is not restored
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment.class, false, true, null);
        }

        initUniversalImageLoader();

        buildGoogleApiClient();

        setupDrawer();

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
    public boolean onPrepareOptionsMenu(Menu menu) {

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        boolean actionsVisibility = !drawerLayout.isDrawerVisible(Gravity.START);

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
    // Navigation drawer management

    @Override
    public void onBackPressed() {

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerVisible(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
            return;
        }

        super.onBackPressed();
    }

    /**
     * Initiate the navigation drawer
     */
    private void setupDrawer() {

        setupDrawerListView();

        setupDrawerAndActionBarDependencies();

    }

    @SuppressLint("NewApi")
    private void setupDrawerListView() {

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerList = (ListView) findViewById(R.id.drawer_contents);

        // Set the adapter for the list view
        final NavigationDrawerListAdapter adapter = new NavigationDrawerListAdapter(this, 0);
        drawerList.setAdapter(adapter);

        // Populate the adapter with entries
        String[] entries = getResources().getStringArray(R.array.nav_drawer_entries);
        TypedArray icons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        for (int i=0; i<entries.length; i++) {
            adapter.add(new NavigationDrawerEntry(entries[i], icons.getResourceId(i, 0)));
        }

        icons.recycle();

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Highlight the selected item and close the drawer
                drawerList.setItemChecked(position, true);
                drawerLayout.closeDrawer(drawerList);

                String chosenEntry = adapter.getItem(position).getTitle();

                onDrawerEntryChosen(chosenEntry);

            }
        });
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
        }
        else if (chosenEntry.equals(getResources().getString(R.string.nav_drawer_entry_logout))) {
            MainActivity.this.logOutCurrentUser();
        }
        else {
            Log.e(LOG_TAG, "drawer entry \"" + chosenEntry + "\" has no functionality");
        }
    }

    private void setupDrawerAndActionBarDependencies() {

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {

            private boolean isDrawerVisibleLast = false;

            @Override
            public void onDrawerSlide(View view, float v) {
                boolean isDrawerVisible = drawerLayout.isDrawerVisible(Gravity.START);

                // For performance update the action bar only when the state of drawer changes
                if (isDrawerVisible != isDrawerVisibleLast) {

                    if (isDrawerVisible) {
                        setActionBarTitle("");
                    } else {
                        Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);
                        if (currFragment != null &&
                                IDoCareFragmentInterface.class.isAssignableFrom(currFragment.getClass())) {
                            setActionBarTitle(((IDoCareFragmentInterface)currFragment).getTitle());
                        }
                    }

                    invalidateOptionsMenu();

                    isDrawerVisibleLast = isDrawerVisible;
                }
            }

            @Override
            public void onDrawerOpened(View view) {

            }

            @Override
            public void onDrawerClosed(View view) {
            }

            @Override
            public void onDrawerStateChanged(int state) {
            }
        });
    }

    // End of navigation drawer management
    //
    // ---------------------------------------------------------------------------------------------




    // ---------------------------------------------------------------------------------------------
    //
    // User session management

    private void logOutCurrentUser() {
        getUserStateManager().logOut();
    }



    // End of user session management
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
