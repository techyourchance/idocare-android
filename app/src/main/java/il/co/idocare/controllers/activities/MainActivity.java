package il.co.idocare.controllers.activities;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import il.co.idocare.Constants;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.adapters.NavigationDrawerListAdapter;
import il.co.idocare.controllers.fragments.HomeFragment;
import il.co.idocare.controllers.fragments.AbstractFragment;
import il.co.idocare.R;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.pojos.NavigationDrawerEntry;


public class MainActivity extends AbstractActivity implements
        FragmentManager.OnBackStackChangedListener {

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
            replaceFragment(HomeFragment.class, false, null);
        }

        initUniversalImageLoader();

        buildGoogleApiClient();

        setupDrawer();

        // This callback will be used to show/hide up (back) button in actionbar
        getFragmentManager().addOnBackStackChangedListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) mGoogleApiClient.connect();

        enableAutomaticSync();

        // TODO: verify that this call resolves the missing UP button when the activity is restarted
        onBackStackChanged();

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
    // Back stack management


    @Override
    public void onBackStackChanged() {
        if (getActionBar() != null) {
            // Enable Up button only  if there are entries in the back stack
            boolean hasBackstackEntries = getFragmentManager().getBackStackEntryCount() > 0;
            getActionBar().setDisplayHomeAsUpEnabled(hasBackstackEntries);
        }
    }

    @Override
    public boolean onNavigateUp() {
        getFragmentManager().popBackStack();
        return true;
    }

    // End of back stack management
    //
    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
    //
    // Navigation drawer management

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

                // Can't do switch/case on Strings :(
                if (chosenEntry.equals(getResources().getString(R.string.nav_drawer_entry_home))) {
                    replaceFragment(HomeFragment.class, false, null);
                }
                else if (chosenEntry.equals(getResources().getString(R.string.nav_drawer_entry_new_request))) {
                    replaceFragment(NewRequestFragment.class, false, null);
                }
                else if (chosenEntry.equals(getResources().getString(R.string.nav_drawer_entry_logout))) {
                    MainActivity.this.logOutCurrentUser();
                }
                else {
                    Log.e(LOG_TAG, "drawer entry \"" + chosenEntry + "\" has no functionality");
                }

                // Clear back-stack
                // TODO: this is correct only if all entries in the drawer are "top level"
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
        });
    }

    private void setupDrawerAndActionBarDependencies() {

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {

            private boolean mIsDrawerVisibleLast = false;

            @Override
            public void onDrawerSlide(View view, float v) {
                boolean isDrawerVivible = drawerLayout.isDrawerVisible(Gravity.START);

                // For performance update the action bar only when the state of drawer changes
                if (isDrawerVivible != mIsDrawerVisibleLast) {

                    if (isDrawerVivible) {
                        setActionBarTitle("");
                    } else {
                        Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);
                        if (currFragment != null) {
                            setActionBarTitle(((AbstractFragment)currFragment).getTitle());
                        }
                    }

                    invalidateOptionsMenu();

                    mIsDrawerVisibleLast = isDrawerVivible;
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
        SharedPreferences prefs =
                getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

        prefs.edit().remove(Constants.FIELD_NAME_USER_ID).apply();
        prefs.edit().remove(Constants.FIELD_NAME_USER_AUTH_TOKEN).apply();

    }


    private void enableAutomaticSync() {
        Account acc = getActiveAccount();
        ContentResolver.setIsSyncable(acc, IDoCareContract.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(acc, IDoCareContract.AUTHORITY, true);
    }

    private void disableAutomaticSync() {
        Account acc = getActiveAccount();
        ContentResolver.setIsSyncable(acc, IDoCareContract.AUTHORITY, 0);
    }

    private void requestImmediateSync() {
        Account acc = getActiveAccount();
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        ContentResolver.requestSync(acc, IDoCareContract.AUTHORITY, settingsBundle);
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
