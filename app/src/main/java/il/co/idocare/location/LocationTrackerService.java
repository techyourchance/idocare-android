package il.co.idocare.location;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import il.co.idocare.R;
import il.co.idocare.controllers.activities.MainActivity;

/**
 * This service tracks the location of the user and provides this information to other app's
 * components
 */
public class LocationTrackerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationTrackerService";


    private static final int LOCATION_UPDATE_INTERVAL_SHORT_MS = 1000;
    private static final int LOCATION_UPDATE_INTERVAL_LONG_MS = 5000;

    private static final int GPS_PERMISSION_RECHECK_INTERVAL = 2000; // in ms

    private static final int NOTIFICATION_ID_GPS_PERMISSION_REQUEST = 0;

    private IBinder mBinder = new LocationTrackerServiceBinder();

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderApi mFusedLocationProviderApi;
    private LocationRequest mLocationRequest;

    private Set<LocationListener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<LocationListener, Boolean>(1));

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationRequest = createLocationRequest();
        mFusedLocationProviderApi = LocationServices.FusedLocationApi;
        mGoogleApiClient = createGoogleApiClient();


        if (isAccessFineLocationPermissionGranted()) {
            init();
        } else {
            showPermissionRequestNotification();
            scheduleGpsPermissionRecheck();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    private GoogleApiClient createGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL_LONG_MS);
        locationRequest.setFastestInterval(LOCATION_UPDATE_INTERVAL_SHORT_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void init() {
        mGoogleApiClient.connect();
    }

    private void scheduleGpsPermissionRecheck() {
        Log.d(TAG, "scheduling ACCESS_FINE_LOCATION permission recheck in " +
                GPS_PERMISSION_RECHECK_INTERVAL + " ms");
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAccessFineLocationPermissionGranted()) {
                    hidePermissionRequestNotification();
                    init();
                } else {
                    scheduleGpsPermissionRecheck();
                }
            }
        }, GPS_PERMISSION_RECHECK_INTERVAL);
    }

    @Override
    public void onLocationChanged(Location location) {
        processNewLocation(location);
    }

    private void processNewLocation(Location location) {
        for (LocationListener listener : mListeners) {
            listener.onLocationChanged(location);
        }
    }

    private boolean isAccessFineLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }


    private void showPermissionRequestNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder
                .setSmallIcon(R.drawable.ic_logo_grayscale)
                .setContentTitle(getString(R.string.notification_gps_permission_request_title))
                .setContentText(getString(R.string.notification_gps_permission_request_body));

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.EXTRA_GPS_PERMISSION_REQUEST_RETRY, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID_GPS_PERMISSION_REQUEST, builder.build());
    }

    private void hidePermissionRequestNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID_GPS_PERMISSION_REQUEST);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (isAccessFineLocationPermissionGranted()) {
            //noinspection MissingPermission
            mFusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            throw new IllegalStateException("required permission wasn't granted");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended(); index: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(); connection result: " + connectionResult);
    }



    public class LocationTrackerServiceBinder extends Binder {

        public void registerLocationListener(LocationListener listener) {
            mListeners.add(listener);
        }

        public void unregisterLocationListener(LocationListener listener) {
            mListeners.remove(listener);
        }
    }

}
