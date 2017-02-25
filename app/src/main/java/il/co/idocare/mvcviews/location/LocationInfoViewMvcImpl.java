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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;

/**
 * Implementation of LocationInfoViewMvc interface
 */
public class LocationInfoViewMvcImpl
        extends AbstractViewMVC<LocationInfoViewMvc.LocationInfoViewMvcListener>
        implements LocationInfoViewMvc, OnMapReadyCallback {

    private TextView mTxtLocationTitle;
    private TextView mTxtFineLocation;
    private MapView mMapView;

    private GoogleMap mGoogleMap;
    private LatLng mLocation;


    public LocationInfoViewMvcImpl(@NonNull LayoutInflater inflater,
                                   @Nullable ViewGroup container) {
        setRootView(inflater.inflate(R.layout.element_location_info, container, false));
        initialize();
    }

    private void initialize() {

        mTxtFineLocation = (TextView) getRootView().findViewById(R.id.txt_request_fine_location);
        mTxtLocationTitle = (TextView) getRootView().findViewById(R.id.txt_location_title);

        mMapView = (MapView) getRootView().findViewById(R.id.map_preview);
        mMapView.getMapAsync(this);
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        MapsInitializer.initialize(getRootView().getContext());
        mGoogleMap.setMyLocationEnabled(false); // Don't show my location
        mGoogleMap.setBuildingsEnabled(false); // Don't show 3D buildings
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false); // No toolbar needed in a lite preview

        if (mLocation != null) {
            // location has already been bound - bind it to the map
            setLocation(mLocation);
        }
    }

    @Override
    public void setLocation(double latitude, double longitude) {
        mLocation = new LatLng(latitude, longitude);

        if (mGoogleMap != null) {
            // map initialized - bind location to it
            setLocation(mLocation);
        }
    }

    private void setLocation(LatLng location) {
        // Center the camera at request location
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        // Put a marker
        mGoogleMap.addMarker(new MarkerOptions().position(location));
    }

    @Override
    public void setLocationString(@Nullable String location) {
        if (location == null || location.length() == 0) {
            mTxtLocationTitle.setVisibility(View.INVISIBLE);
            mTxtFineLocation.setVisibility(View.INVISIBLE);
        } else {
            mTxtLocationTitle.setVisibility(View.VISIBLE);
            mTxtFineLocation.setVisibility(View.VISIBLE);
            mTxtFineLocation.setText(location);
        }
    }

}
