package il.co.idocare.location;

/**
 * Created by Vasiliy on 8/30/2015.
 */
public class OpenStreetMapsReverseGeocoderFactory implements ReverseGeocoderFactory {
    @Override
    public ReverseGeocoder newInstance() {
        return new OpenStreetMapsReverseGeocoder();
    }
}
