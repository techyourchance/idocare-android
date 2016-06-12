package il.co.idocare.helpers;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import il.co.idocare.Constants;
import il.co.idocare.eventbusevents.LocationEvents;

/**
 * This class encapsulates the logic related to user's location
 */
public class LocationHelper {

    public static int MINIMUM_ACCEPTABLE_LOCATION_ACCURACY_METERS = 30;

    public static int MAXIMAL_DISTANCE_SAME_LOCATION_DETERMINATION = 30;

    /**
     * Check whether the location info is accurate enough in order to be used
     */
    public boolean isAccurateLocation(@Nullable Location location) {
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

    /**
     * This method should be called when high accuracy location is required. Location updates will
     * be delivered on event bus using {@link LocationEvents.BestLocationEstimateEvent} events
     */
    public void highAccuracyLocationRequired() {
        // TODO: maybe use startService here?
        EventBus.getDefault().post(new LocationEvents.HighAccuracyLocationRequiredEvent());
    }
}
