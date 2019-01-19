package il.co.idocare.location;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.Locale;

public interface ReverseGeocoder {

    /**
     * Obtain the textual representation of the location localized for the specified Locale.<br>
     *
     * @param latitude the latitude of the location
     * @param longitude the longitude of the location
     * @param locale localization parameter. If localization for the specified Locale couldn't be
     *               achieved, the returned String should be localized to US
     * @return localized description of the specified location, or null in case of any error
     */
    @WorkerThread
    @Nullable
    public String getFromLocation(double latitude, double longitude, Locale locale);
}
