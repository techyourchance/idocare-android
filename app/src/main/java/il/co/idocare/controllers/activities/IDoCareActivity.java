package il.co.idocare.controllers.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import il.co.idocare.Constants;
import il.co.idocare.controllers.fragments.FragmentHome;
import il.co.idocare.controllers.fragments.FragmentLogin;
import il.co.idocare.IDoCareApplication;
import il.co.idocare.controllers.fragments.AbstractFragment;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.UtilMethods;


public class IDoCareActivity extends Activity implements
        AbstractFragment.IDoCareFragmentCallback,
        FragmentManager.OnBackStackChangedListener {

    private static final String LOG_TAG = "IDoCareActivity";

    private static Context sContext;

    public GoogleApiClient mGoogleApiClient;

    ScheduledExecutorService mRequestsUpdateScheduler;
    ScheduledFuture mScheduledFuture;


    // ---------------------------------------------------------------------------------------------
    //
    // Activity lifecycle management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUniversalImageLoader();

        buildGoogleApiClient();

        setupDrawer();

        // This callback will be used to show/hide up (back) button in actionbar
        getFragmentManager().addOnBackStackChangedListener(this);

        // Decide which fragment to show if the app is not restored
        if (savedInstanceState == null) {
            SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_FILE, MODE_PRIVATE);

            if (prefs.contains(Constants.FieldName.USER_ID.getValue()) &&
                    prefs.contains(Constants.FieldName.USER_PUBLIC_KEY.getValue())) {
                // Go straight to home page if user ID and public key exist
                getFragmentManager().beginTransaction()
                        .add(R.id.frame_contents, new FragmentHome())
                        .commit();
            } else {
                // Hide action bar
                if (getActionBar() != null) getActionBar().hide();
                // Bring up login fragment
                getFragmentManager().beginTransaction()
                        .add(R.id.frame_contents, new FragmentLogin())
                        .commit();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) mGoogleApiClient.connect();

        // TODO: verify that this call resolves the missing UP button when the activity is restarted
        onBackStackChanged();

        // Start periodic updates of requests' cache
        //scheduleRequestsCacheUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
    // Fragments management

    // TODO: maybe we need to preserve the state of the replaced fragments?
    @Override
    public void replaceFragment(Class<? extends AbstractFragment> claz, boolean addToBackStack,
                                Bundle args) {

        Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (currFragment != null) {
            if (claz.isInstance(currFragment)) {
                // The currently shown fragment is the same as the new one - nothing to do
                Log.v(LOG_TAG, "the fragment " + claz.getSimpleName() + " is already shown");
                return;
            }
        }

        // Create new fragment
        Fragment newFragment;

        try {
            newFragment = claz.newInstance();
            if (args != null) newFragment.setArguments(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        if (addToBackStack) ft.addToBackStack(null);
        // Change to a new fragment
        ft.replace(R.id.frame_contents, newFragment, claz.getClass().getSimpleName());
        ft.commit();

    }



    // End of fragments management
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


    private void setupDrawerListView() {

        String[] entries = getResources().getStringArray(R.array.drawer_entries);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerList = (ListView) findViewById(R.id.drawer_contents);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                entries));

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Highlight the selected item and close the drawer
                drawerList.setItemChecked(position, true);
                drawerLayout.closeDrawer(drawerList);

                switch (position) {

                    // TODO: populate other options too
                    case 0:
                        IDoCareActivity.this.replaceFragment(FragmentHome.class, true, null);
                        break;
                    default:
                        return;
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

            @Override
            public void onDrawerSlide(View view, float v) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View view) {

            }

            @Override
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerStateChanged(int state) {
            }
        });
    }

    // End of navigation drawer management
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
