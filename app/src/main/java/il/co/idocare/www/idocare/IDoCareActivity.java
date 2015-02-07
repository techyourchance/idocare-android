package il.co.idocare.www.idocare;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

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

    private void stopRequestsCacheUpdates() {
//        if (mRequestsUpdateScheduler != null)
//            mRequestsUpdateScheduler.shutdown();
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
