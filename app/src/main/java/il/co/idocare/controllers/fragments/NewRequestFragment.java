package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.ServerRequest;
import il.co.idocare.controllers.activities.IDoCareActivity;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.IDoCareJSONUtils;
import il.co.idocare.utils.UtilMethods;
import il.co.idocare.views.NewRequestViewMVC;


public class NewRequestFragment extends AbstractFragment implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "NewRequestFragment";

    NewRequestViewMVC mViewMVCNewRequest;

    private long mRequestId;

    private String mLastCameraPicturePath;
    private List<String> mCameraPicturesPaths = new ArrayList<String>(3);

    private ProgressDialog mProgressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewMVCNewRequest = new NewRequestViewMVC(inflater, container);
        // Provide inbox Handler to the MVC View
        mViewMVCNewRequest.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mViewMVCNewRequest.getInboxHandler());


        // Restore state from bundle (if required)
        restoreSavedStateIfNeeded(savedInstanceState);

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setTitle(R.string.new_request_fragment_title);
        }


        return mViewMVCNewRequest.getRootView();
    }


    private void restoreSavedStateIfNeeded(Bundle savedInstanceState) {

        if (savedInstanceState == null) return;


        mLastCameraPicturePath = savedInstanceState.getString("lastCameraPicturePath");

        // Get the list of pictures from saved state and pass them to adapter
        String[] cameraPicturesPaths = savedInstanceState.getStringArray("cameraPicturesPaths");

        for (int i=0; i<cameraPicturesPaths.length; i++) {
            if (cameraPicturesPaths[i] != null) {
                showPicture(i, cameraPicturesPaths[i]);
            }
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
            case V_CREATE_NEW_REQUEST_BUTTON_CLICKED:
                createRequest();
                break;
            case V_TAKE_PICTURE_BUTTON_CLICKED:
                takePictureWithCamera();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String[] cameraPicturesPaths = new String[mCameraPicturesPaths.size()];
        mCameraPicturesPaths.toArray(cameraPicturesPaths);

        // Save pictures' paths
        outState.putStringArray("cameraPicturesPaths", cameraPicturesPaths);

        // If not saved, the path will be lost when Camera activity starts
        outState.putString("lastCameraPicturePath", mLastCameraPicturePath);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.StartActivityTag.CAPTURE_PICTURE.ordinal()) {
            if (resultCode == Activity.RESULT_OK) {
                UtilMethods.adjustCameraPicture(mLastCameraPicturePath);
                showPicture(mLastCameraPicturePath);
            } else {
                // TODO: do we need anything here?
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showPicture(String cameraPicturePath) {
        showPicture(mCameraPicturesPaths.size(), cameraPicturePath);
    }

    private void showPicture(int position, String cameraPicturePath) {
        if (position >= 3) {
            Log.e(LOG_TAG, "maximal number of pictures exceeded!");
            return;
        }
        if (mCameraPicturesPaths.size() > position) {
            mCameraPicturesPaths.remove(position);
        }
        mCameraPicturesPaths.add(position, cameraPicturePath);
        mViewMVCNewRequest.showPicture(position, cameraPicturePath);
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
        startActivityForResult(intent, Constants.StartActivityTag.CAPTURE_PICTURE.ordinal());
    }


    /**
     * Create, populate and execute a new server request of type "add new request" based
     * on the contents of fragment's views
     */
    private void createRequest() {
        
        showProgressDialog();

        Bundle bundleNewRequest = mViewMVCNewRequest.getViewState();

        ServerRequest serverRequest = new ServerRequest(Constants.CREATE_REQUEST_URL,
                Constants.ServerRequestTag.CREATE_REQUEST, this);

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


        // Set closed pictures
        for (int i = 0; i < mCameraPicturesPaths.size(); i++) {
            serverRequest.addPicture(Constants.FieldName.CREATED_PICTURES.getValue(),
                    "picture" + String.valueOf(i) + ".jpg", mCameraPicturesPaths.get(i));
        }

        serverRequest.execute();
    }



    @Override
    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag,
                               String responseData) {

        if (tag == Constants.ServerRequestTag.CREATE_REQUEST) {
            if (responseStatusOk && IDoCareJSONUtils.verifySuccessfulStatus(responseData)) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
                replaceFragment(HomeFragment.class, false, null);
                Toast.makeText(getActivity(), "Request created successfully", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }

    private void showProgressDialog() {
        mProgressDialog = ProgressDialog.
                show(getActivity(), "Please wait ...", "Creating new request ...", true);
    }
}
