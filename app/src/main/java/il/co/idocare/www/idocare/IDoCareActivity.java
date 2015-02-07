package il.co.idocare.www.idocare;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class IDoCareActivity extends Activity implements
        FragmentManager.OnBackStackChangedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "IDoCareActivity";

    protected GoogleApiClient mGoogleApiClient;

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
}
