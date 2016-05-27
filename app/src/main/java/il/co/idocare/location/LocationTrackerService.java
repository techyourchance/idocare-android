package il.co.idocare.location;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.co.idocare.R;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.eventbusevents.LocationEvents;

/**
 * This service tracks the location of the user and provides this information to other app's
 * components
 */
public class LocationTrackerService extends Service {

    private static final String TAG = "LocationTrackerService";

    private static final int LOCATION_UPDATE_INTERVAL_SHORT = 500; // in ms
    private static final int LOCATION_UPDATE_INTERVAL_LONG = 5000; // in ms

    private static final int TARGET_LOCATION_ACCURACY = 30; // in meters

    private static final int STATE_DISABLED = 0;
    private static final int STATE_HIGHEST_ACCURACY = 1;
    private static final int STATE_PASSIVE = 2;

    private static final int GPS_PERMISSION_RECHECK_INTERVAL = 2000; // in ms

    private static final int NOTIFICATION_ID_GPS_PERMISSION_REQUEST = 0;


    // This listener should be registered with LocationManager in order to handle location updates
    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            processNewLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };



    private int mState = STATE_DISABLED;

    private Location mCurrentBestEstimate = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called with intent: " + intent);

        if (!checkGpsPermission()) {
            EventBus.getDefault().removeStickyEvent(LocationEvents.BestLocationEstimateEvent.class);
            scheduleGpsPermissionRecheck();
        } else {
            init();
        }

        return Service.START_STICKY;
    }

    private void scheduleGpsPermissionRecheck() {
        Log.d(TAG, "scheduling ACCESS_FINE_LOCATION permission recheck in " +
                GPS_PERMISSION_RECHECK_INTERVAL + " ms");
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (checkGpsPermission()) {
                    init();
                } else {
                    scheduleGpsPermissionRecheck();
                }
            }
        }, GPS_PERMISSION_RECHECK_INTERVAL);
    }

    private void init() {
        // Use the latest best estimate as a starting point (if exists)
        if (mCurrentBestEstimate == null) {
            LocationEvents.BestLocationEstimateEvent pastBestEstimate = EventBus.getDefault()
                    .getStickyEvent(LocationEvents.BestLocationEstimateEvent.class);
            if (pastBestEstimate != null) {
                mCurrentBestEstimate = pastBestEstimate.location;
            }
        }

        gotoState(STATE_HIGHEST_ACCURACY);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterFromLocationUpdates();
    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    @Subscribe
    public void onEvent(LocationEvents.HighAccuracyLocationRequiredEvent event) {
        gotoState(STATE_HIGHEST_ACCURACY);
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------


    private void gotoState(int state) {
        Log.d(TAG, "transition to state " + state + " initiated. Current state: " + mState);
        if (state == mState) {
            Log.d(TAG, "aborting transition to the same state");
            return;
        }

        switch (state) {
            case STATE_DISABLED:
                unregisterFromLocationUpdates();
                break;
            case STATE_HIGHEST_ACCURACY:
                unregisterFromLocationUpdates();
                registerForLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_INTERVAL_SHORT, 0);
                break;
            case STATE_PASSIVE:
                unregisterFromLocationUpdates();
                registerForLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                        LOCATION_UPDATE_INTERVAL_LONG, 0);
                break;
            default:
                Log.e(TAG, "aborting transition to unrecognized state: " + state);
                return;
        }
        mState = state;
    }

    private void registerForLocationUpdates(String provider, long minTime, float minDist) {
        Log.d(TAG, "registering for location updates using provider: " + provider
                + " minTime: " + minTime + " minDist: " + minDist);
        LocationManager locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(provider, minTime, minDist, locationListener);
    }


    private void unregisterFromLocationUpdates() {
        Log.d(TAG, "unregistering from location updates");
        LocationManager locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);

    }

    private void processNewLocation(Location locationUpdate) {
        Log.d(TAG, "processing new location update: " + locationUpdate.toString());

        if (!locationUpdate.hasAccuracy()) {
            Log.e(TAG, "location update discarded because it doesn't have accuracy info");
            return;
        }

        boolean locationUpdateIsBetterEstimate = false;

        if (mCurrentBestEstimate == null) {
            Log.d(TAG, "received the first location update - treating it as the best estimate");
            locationUpdateIsBetterEstimate = true;
        } else if (isBetterEstimate(locationUpdate)) {
            Log.d(TAG, "new location update is a better estimate");
            locationUpdateIsBetterEstimate = true;
        }

        if (locationUpdateIsBetterEstimate) {
            Log.d(TAG, "new best location estimate is " + locationUpdate.toString());
            mCurrentBestEstimate = locationUpdate;
            EventBus.getDefault()
                    .postSticky(new LocationEvents.BestLocationEstimateEvent(mCurrentBestEstimate));
        }

        // once reached target accuracy - go to passive state in order to conserve power
        if (mState != STATE_PASSIVE
                && mCurrentBestEstimate.getAccuracy() < TARGET_LOCATION_ACCURACY) {
            Log.d(TAG, "reached the target accuracy threshold");
            gotoState(STATE_PASSIVE);
        }
    }

    private boolean isBetterEstimate(Location locationUpdate) {
        float distanceDiffMeters = Math.abs(mCurrentBestEstimate.distanceTo(locationUpdate));

        /*
         Reject location update only if one of the following holds (CBE = Current Best Estimate):
         1) CBE's accuracy is better (or equal), and update falls inside CBE's accuracy radius

         TODO: enhance comparison logic
          */

        if (mCurrentBestEstimate.getAccuracy() <= locationUpdate.getAccuracy()
                && mCurrentBestEstimate.getAccuracy() > distanceDiffMeters) {
            return false;
        }

        return true;
    }

    private boolean checkGpsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            hidePermissionRequestNotification();
            return true;
        } else {
            showPermissionRequestNotification();
            return false;
        }
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

}
