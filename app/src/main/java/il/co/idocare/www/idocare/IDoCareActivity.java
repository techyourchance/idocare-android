package il.co.idocare.www.idocare;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class IDoCareActivity extends Activity implements
        FragmentManager.OnBackStackChangedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ServerRequest.OnServerResponseCallback {

    private static final String LOG_TAG = "IDoCareActivity";

    protected GoogleApiClient mGoogleApiClient;

    ScheduledExecutorService mRequestsUpdateScheduler;
    ScheduledFuture mScheduledFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUniversalImageLoader();

        buildGoogleApiClient();

        initNavigationDrawer();

        // This callback will be used to show/hide up (back) button in actionbar
        getFragmentManager().addOnBackStackChangedListener(this);

        // Decide which fragment to show if the app is not restored
        if (savedInstanceState == null) {
            SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_FILE, MODE_PRIVATE);

            if (prefs.contains("username") && prefs.contains("password")) {
                // Go straight to home page if username and password exist
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
        scheduleRequestsCacheUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();

        // Stop the updates of requests' cache
        stopRequestsCacheUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

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

    /**
     * Show a particular fragment
     * TODO: maybe we need to preserve the state of the replaced fragments?
     * @param claz the class of the fragment to be shown
     */
    protected void showFragment(Class<? extends Fragment> claz, boolean addToBackStack) {

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
        Fragment newFragment = null;

        try {
            newFragment = claz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (addToBackStack) ft.addToBackStack(null);
        // Change to a new fragment
        ft.replace(R.id.frame_contents, newFragment, claz.getClass().getSimpleName());
        ft.commit();

    }

    /**
     * Initiate the navigation drawer
     */
    private void initNavigationDrawer() {
        String[] entries = getResources().getStringArray(R.array.drawer_entries);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerList = (ListView) findViewById(R.id.drawer_contents);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1
                , entries));


        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Highlight the selected item and close the drawer
                drawerList.setItemChecked(position, true);
                drawerLayout.closeDrawer(drawerList);

                switch (position) {

                    // TODO: populate other options too
                    case 0:
                        showFragment(FragmentHome.class, true);
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }

                // Clear back-stack
                // TODO: this is correct only if all entries in the drawer are "top level"
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
        });

    }


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

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    /**
     * Start periodical update of the list of the requests stored in application context. The
     * application context was required in order to preserve the data when the activity is killed.
     * TODO: this workaround should be replaced with DB+SyncAdapter scheme
     */
    private void scheduleRequestsCacheUpdates() {
        if (mRequestsUpdateScheduler == null)
            mRequestsUpdateScheduler = Executors.newSingleThreadScheduledExecutor();

        final Runnable update = new Runnable() {
            public void run() {
                ServerRequest serverRequest = new ServerRequest(Constants.GET_ALL_REQUESTS_URL,
                        Constants.ServerRequestTag.GET_ALL_REQUESTS, IDoCareActivity.this);

                SharedPreferences prefs =
                        getSharedPreferences(Constants.PREFERENCES_FILE, MODE_PRIVATE);
                serverRequest.addTextField("username", prefs.getString("username", "no_username"));
                serverRequest.addTextField("password", prefs.getString("password", "no_password"));

                serverRequest.execute();
            }
        };

        mScheduledFuture =
                mRequestsUpdateScheduler.scheduleAtFixedRate (update, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Stop updates of requests' cache
     */
    private void stopRequestsCacheUpdates() {
        if (mScheduledFuture != null) mScheduledFuture.cancel(false);
    }


    @Override
    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.GET_ALL_REQUESTS) {
            List<RequestItem> requests = UtilMethods.extractRequestsFromJSON(responseData);
            ((IDoCareApplication)getApplication()).setRequests(requests);
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }





}
