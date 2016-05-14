package il.co.idocare.mvcviews.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;

/**
 * Implementation of LocationInfoViewMvc interface
 */
public class LocationInfoViewMvcImpl
        extends AbstractViewMVC<LocationInfoViewMvc.LocationInfoViewMvcListener>
        implements LocationInfoViewMvc {

    private TextView mTxtLocationTitle;
    private TextView mTxtFineLocation;
    private MapView mMapPreview;


    public LocationInfoViewMvcImpl(@NonNull LayoutInflater inflater,
                                   @Nullable ViewGroup container) {
        setRootView(inflater.inflate(R.layout.element_location_info, container, false));
        initialize();
    }

    private void initialize() {

        mTxtFineLocation = (TextView) getRootView().findViewById(R.id.txt_request_fine_location);
        mTxtLocationTitle = (TextView) getRootView().findViewById(R.id.txt_location_title);

        mMapPreview = (MapView) getRootView().findViewById(R.id.map_preview);
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    @Override
    public void setLocation(double latitude, double longitude) {
        GoogleMap map = mMapPreview.getMap();

        MapsInitializer.initialize(getRootView().getContext());
        map.setMyLocationEnabled(false); // Don't show my location
        map.setBuildingsEnabled(false); // Don't show 3D buildings
        map.getUiSettings().setMapToolbarEnabled(false); // No toolbar needed in a lite preview

        LatLng location = new LatLng(latitude, longitude);
        // Center the camera at request location
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
        // Put a marker
        map.addMarker(new MarkerOptions().position(location));
    }

    @Override
    public void setLocationString(@Nullable String location) {
        if (location == null || location.length() == 0) {
            mTxtLocationTitle.setVisibility(View.GONE);
            mTxtFineLocation.setVisibility(View.GONE);
        } else {
            mTxtLocationTitle.setVisibility(View.VISIBLE);
            mTxtFineLocation.setVisibility(View.VISIBLE);
            mTxtFineLocation.setText(location);
        }
    }
}
