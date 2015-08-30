package il.co.idocare.location;

/**
 * Created by Vasiliy on 8/30/2015.
 */
public interface ReverseGeocoderFactory {

    /**
     *
     * @return a new instance of ReverseGeocoder implementation
     */
    public ReverseGeocoder newInstance();
}
