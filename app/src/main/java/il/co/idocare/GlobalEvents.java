package il.co.idocare;

import android.location.Location;

/**
 * This class contains definitions of global events used in the app.<br>
 * The only reason to put events in this class (as opposed to putting them in e.g. the
 * class that produces them) is to prevent unhealthy dependencies between the producers
 * and the consumers of those events. Please note that in many cases this dependency exists anyway
 * (e.g. MVC controller depends on particular MVC views), in which case the events used to
 * communicate between already dependent classes should be defined as static inner classes of the
 * producer.
 */
public class GlobalEvents {

    private GlobalEvents() {}

    /**
     * This event should be used to broadcast the current best estimate of the location. It  makes
     * sense to make this event sticky, in which case the consumers should ensure that the latest
     * best estimate still relevant.
     */
    public static class BestLocationEstimateEvent {
        public Location location;
        public BestLocationEstimateEvent(Location location) {
            this.location = location;
        }
    }

    /**
     * This event should be broadcast when accurate location is required. Please note that it takes
     * time to obtain an accurate location, therefore producers of this event should broadcast it
     * ahead of time.<br>
     * Note: our current assumption is that the user will be static while using the app, therefore,
     * once a location having high enough accuracy is obtained, we no longer need to track the user
     * with high accuracy and can transition to low power modes (in location context).
     */
    public static class HighAccuracyLocationRequiredEvent {}
}
