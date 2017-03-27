package il.co.idocare.mvcviews.location;

import android.support.annotation.Nullable;

import il.co.idocare.mvcviews.ObservableViewMvc;

/**
 * This MVC view displays information about location (both on map and as a reverse-geocoded string)
 */
public interface LocationInfoViewMvc
        extends ObservableViewMvc<LocationInfoViewMvc.LocationInfoViewMvcListener> {

    public interface LocationInfoViewMvcListener {
        /**
         * Will be called when user clicks on map
         */
        void onMapClicked();
    }

    /**
     * Show the location represented by the provided coordinates on map
     * @param latitude location's latitude
     * @param longitude location's longitude
     */
    void setLocation(double latitude, double longitude);

    /**
     * Set the textual (reverse-geocoded) representation of the location
     * @param location a location string to show; if empty - location string fields will be gone
     */
    public void setLocationString(@Nullable String location);
}
