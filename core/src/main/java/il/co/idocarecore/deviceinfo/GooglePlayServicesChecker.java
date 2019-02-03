package il.co.idocarecore.deviceinfo;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * This class checks for availability of Google Play Services on a device
 */
public class GooglePlayServicesChecker {

    private Activity mActivity;

    public GooglePlayServicesChecker(Activity activity) {
        mActivity = activity;
    }

    /**
     *
     * @return true if Google Play Services available on the device; false otherwise
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(mActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            return false;
        } else {
            return true;
        }
    }
}
