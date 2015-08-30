package il.co.idocare.location;

import java.util.Locale;

/**
 * Created by Vasiliy on 8/28/2015.
 */
public interface ReverseGeocoder {

    /**
     * Obtain the textual representation of the location localized for the specified Locale.<br>
     *
     * Note: This method can take a significant amount of time to execute, therefore it mustn't
     * be called from UI thread
     * @param latitude the latitude of the location
     * @param longitude the longitude of the location
     * @param locale localization parameter. If localization for the specified Locale couldn't be
     *               achieved, the returned String should be localized to US
     * @return localized description of the specified location, or null in case of any error
     */
    public String getFromLocation(double latitude, double longitude, Locale locale);
}
