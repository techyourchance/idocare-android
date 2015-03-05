package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import il.co.idocare.Constants;
import il.co.idocare.Constants.FieldName;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.UtilMethods;
import il.co.idocare.views.CloseRequestViewMVC;


public class CloseRequestFragment extends AbstractFragment {

    private final static String LOG_TAG = "CloseRequestFragment";



    private CloseRequestViewMVC mCloseRequestViewMVC;

    private long mRequestId;

    private String mLastCameraPicturePath;
    private List<String> mCameraPicturesPaths = new ArrayList<String>(3);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCloseRequestViewMVC = new CloseRequestViewMVC(inflater, container);
        // Provide inbox Handler to the MVC View
        mCloseRequestViewMVC.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mCloseRequestViewMVC.getInboxHandler());


        Bundle args = getArguments();
        if (args != null) {
            mRequestId = args.getLong(Constants.FieldName.REQUEST_ID.getValue());
        } else {
            Log.e(LOG_TAG, "no arguments set for CloseRequestFragment");
            // TODO: add error case here
        }

        // Restore state from bundle (if required)
        restoreSavedStateIfNeeded(savedInstanceState);

        return mCloseRequestViewMVC.getRootView();
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
            case V_CLOSE_REQUEST_BUTTON_CLICKED:
                closeRequest();
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
        mCloseRequestViewMVC.showPicture(position, cameraPicturePath);
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
     * Create, populate and execute a new server request of type "close request" based
     * on the contents of fragment's views
     */
    private void closeRequest() {
        Bundle closeRequestBundle = mCloseRequestViewMVC.getViewState();

        ServerRequest serverRequest = new ServerRequest(Constants.CLOSE_REQUEST_URL);

        IDoCareHttpUtils.addStandardHeaders(getActivity(), serverRequest);

        // Set request ID
        serverRequest.addTextField(FieldName.REQUEST_ID.getValue(), String.valueOf(mRequestId));

        // Set closed comment
        if (closeRequestBundle.getString(FieldName.CLOSED_COMMENT.getValue()).length() > 0) {
            serverRequest.addTextField(FieldName.CLOSED_COMMENT.getValue(),
                    closeRequestBundle.getString(FieldName.CLOSED_COMMENT.getValue()));
        }

        // Set closed pictures
        for (int i = 0; i < mCameraPicturesPaths.size(); i++) {
            serverRequest.addPicture(FieldName.CLOSED_PICTURES.getValue(),
                    "picture" + String.valueOf(i) + ".jpg", mCameraPicturesPaths.get(i));
        }

        serverRequest.execute();

    }



}
