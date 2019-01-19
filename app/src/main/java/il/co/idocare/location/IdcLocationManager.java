package il.co.idocare.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationListener;
import com.techyourchance.threadposter.UiThreadPoster;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import il.co.idocare.utils.Logger;

/**
 * This class encapsulates the logic related to user's location
 */
public class IdcLocationManager implements LocationListener {

    private static final String TAG = "IdcLocationManager";


    public interface LocationUpdateListener {
        void onLocationUpdateReceived(Location location);
    }

    public static int MINIMUM_ACCEPTABLE_LOCATION_ACCURACY_METERS = 30;
    public static int MAXIMAL_DISTANCE_SAME_LOCATION_DETERMINATION = 30;


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mLogger.d(TAG, "onServiceConnected()");
            mLocationTrackerServiceBinder =
                    (LocationTrackerService.LocationTrackerServiceBinder) service;
            mLocationTrackerServiceBinder.registerLocationListener(IdcLocationManager.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mLogger.d(TAG, "onServiceDisconnected()");
            mLocationTrackerServiceBinder = null;
        }
    };

    private final Object MONITOR = new Object();

    private final Context mContext;
    private final UiThreadPoster mUiThreadPoster;
    private final Logger mLogger;


    private Set<LocationUpdateListener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<LocationUpdateListener, Boolean>(1));

    private LocationTrackerService.LocationTrackerServiceBinder mLocationTrackerServiceBinder;


    public IdcLocationManager(Context context, UiThreadPoster uiThreadPoster, Logger logger) {
        mContext = context;
        mUiThreadPoster = uiThreadPoster;
        mLogger = logger;
    }

    public void registerListener(LocationUpdateListener listener) {
        synchronized (MONITOR) {
            if (mListeners.isEmpty() && mListeners.add(listener)) {
                onFirstListenerAdded();
            }
        }
    }

    public void unregisterListener(LocationUpdateListener listener) {
        synchronized (MONITOR) {
            if (mListeners.remove(listener) && mListeners.isEmpty()) {
                onLastListenerRemoved();
            }
        }
    }

    private void onFirstListenerAdded() {
        Intent intent = new Intent(mContext, LocationTrackerService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void onLastListenerRemoved() {
        mContext.unbindService(mConnection);
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (isAccurateLocation(location)) {
            mUiThreadPoster.post(new Runnable() {
                @Override
                public void run() {
                    for (LocationUpdateListener listener : mListeners) {
                        listener.onLocationUpdateReceived(location);
                    }
                }
            });
        }
    }

    /**
     * Check whether the location info is accurate enough in order to be used
     */
    private boolean isAccurateLocation(@Nullable Location location) {
        if (location != null
                && location.hasAccuracy()
                && location.getAccuracy() < MINIMUM_ACCEPTABLE_LOCATION_ACCURACY_METERS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine whether two locations can be treated as same location
     * @return true if the locations can be treated as same; false otherwise
     */
    public boolean areSameLocations(@NonNull Location location1, @NonNull Location location2) {
        return location1.distanceTo(location2) < MAXIMAL_DISTANCE_SAME_LOCATION_DETERMINATION;
    }


}
