package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.ServerRequest;
import il.co.idocare.controllers.activities.IDoCareActivity;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.UtilMethods;
import il.co.idocare.views.NewRequestViewMVC;


public class NewRequestFragment extends AbstractFragment {

    private final static String LOG_TAG = "NewRequestFragment";

    NewRequestViewMVC mViewMVCNewRequest;

    String mLastCameraPicturePath;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewMVCNewRequest = new NewRequestViewMVC(inflater, container);
        // Provide inbox Handler to the MVC View
        mViewMVCNewRequest.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mViewMVCNewRequest.getInboxHandler());


        // Restore state from bundle (if required)
        restoreSavedStateIfNeeded(savedInstanceState);

        return mViewMVCNewRequest.getRootView();
    }


    private void restoreSavedStateIfNeeded(Bundle savedInstanceState) {

        if (savedInstanceState == null) return;

        // Get the list of pictures from saved state and pass them to adapter
        String[] adapterItems = savedInstanceState.getStringArray("adapterItems");
        if (adapterItems != null) {
        }

        // Restore the last path to camera picture
        if (savedInstanceState.getString("lastCameraPicturePath") != null) {
            mLastCameraPicturePath = savedInstanceState.getString("lastCameraPicturePath");
        }
    }


    @Override
    public boolean isTopLevelFragment() {
        return false;
    }

    @Override
    public Class<? extends AbstractFragment> getNavHierParentFragment() {
        return HomeFragment.class;
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {
            case V_ADD_NEW_REQUEST_BUTTON_CLICKED:
                addNewRequest();
                break;
            case V_TAKE_PICTURE_BUTTON_CLICKED:
                takePictureWithCamera();
                break;
            default:
                Log.w(LOG_TAG, "Message of type "
                        + Constants.MESSAGE_TYPE_VALUES[msg.what].toString() + " wasn't consumed");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the absolute path of the last picture (otherwise we get NullPointerException when
        // trying to access it in onActivityResult())
        outState.putString("lastCameraPicturePath", mLastCameraPicturePath);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.StartActivityTag.CAPTURE_PICTURE_FOR_NEW_REQUEST.ordinal()) {
            if (resultCode == Activity.RESULT_OK) {
                UtilMethods.adjustCameraPicture(mLastCameraPicturePath);
            } else {
                // TODO: do we need anything here?
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Create ACTION_IMAGE_CAPTURE intent with EXTRA_OUTPUT path and call startActivityForResult()
     * with this intent
     */
    private void takePictureWithCamera() {
        String currDateTime =
                new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault()).format(new Date());

        File outputFile = new File(getActivity()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES), currDateTime + ".jpg");

        mLastCameraPicturePath = outputFile.getAbsolutePath();

        Uri cameraPictureUri = Uri.fromFile(outputFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPictureUri);
        startActivityForResult(intent, Constants.StartActivityTag.CAPTURE_PICTURE_FOR_NEW_REQUEST.ordinal());
    }


    /**
     * Create, populate and execute a new server request of type "add new request" based
     * on the contents of fragment's views
     */
    private void addNewRequest() {

        Bundle bundleNewRequest = mViewMVCNewRequest.getViewState();

        ServerRequest serverRequest = new ServerRequest(Constants.ADD_REQUEST_URL);

        IDoCareHttpUtils.addStandardHeaders(getActivity(), serverRequest);


        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                ((IDoCareActivity)getActivity()).mGoogleApiClient);
        if (lastLocation != null) {
            serverRequest.addTextField(Constants.FieldName.LATITUDE.getValue(),
                    String.valueOf(lastLocation.getLatitude()));
            serverRequest.addTextField(Constants.FieldName.LONGITUDE.getValue(),
                    String.valueOf(lastLocation.getLongitude()));
        }

        serverRequest.addTextField(Constants.FieldName.CREATED_POLLUTION_LEVEL.getValue(),
                bundleNewRequest.getString(Constants.FieldName.CREATED_POLLUTION_LEVEL.getValue()));

        if (bundleNewRequest.getString(Constants.FieldName.CREATED_COMMENT.getValue()).length() > 0) {
            serverRequest.addTextField(Constants.FieldName.CREATED_COMMENT.getValue(),
                    bundleNewRequest.getString(Constants.FieldName.CREATED_COMMENT.getValue()));
        }
        serverRequest.execute();
    }

}
