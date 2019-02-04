package il.co.idocarecore.location;

import android.location.Location;

import androidx.annotation.NonNull;

public interface IdcLocationManager {

    interface LocationUpdateListener {
        void onLocationUpdateReceived(Location location);
    }

    void registerListener(LocationUpdateListener listener);

    void unregisterListener(LocationUpdateListener listener);

    boolean areSameLocations(@NonNull Location location1, @NonNull Location location2);

}
