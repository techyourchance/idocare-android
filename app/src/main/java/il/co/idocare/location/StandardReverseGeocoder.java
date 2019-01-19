package il.co.idocare.location;

import android.location.Address;
import android.location.Geocoder;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class StandardReverseGeocoder implements ReverseGeocoder {

    private final Geocoder mGeocoder;

    public StandardReverseGeocoder(Geocoder geocoder) {
        mGeocoder = geocoder;
    }

    @Nullable
    @Override
    public String getFromLocation(double latitude, double longitude, Locale locale) {
        if (Geocoder.isPresent()) {
            try {
                List<Address> addresses = mGeocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    StringBuilder sb = new StringBuilder();

                    if (address.getLocality() != null) {
                        sb.append(address.getLocality()).append(" ");
                    }

                    return sb.toString();
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
