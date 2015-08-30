package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.networking.ServerHttpRequest;
import il.co.idocare.utils.UtilMethods;
import il.co.idocare.views.CloseRequestViewMVC;


public class CloseRequestFragment extends AbstractFragment {

    private final static String LOG_TAG = CloseRequestFragment.class.getSimpleName();



    private CloseRequestViewMVC mCloseRequestViewMVC;

    private long mRequestId;

    private String mLastCameraPicturePath;
    private List<String> mCameraPicturesPaths = new ArrayList<String>(3);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCloseRequestViewMVC = new CloseRequestViewMVC(inflater, container);

        Bundle args = getArguments();
        if (args != null) {
            mRequestId = args.getLong(Constants.FIELD_NAME_REQUEST_ID);
        } else {
            Log.e(LOG_TAG, "no arguments set for CloseRequestFragment");
            // TODO: add error case here
        }

        // Restore state from bundle (if required)
        restoreSavedStateIfNeeded(savedInstanceState);

        setActionBarTitle(getTitle());

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
    public String getTitle() {
        return getResources().getString(R.string.close_request_fragment_title);
    }



    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    public void onEvent(CloseRequestViewMVC.TakePictureButtonClickEvent event) {
        takePictureWithCamera();
    }

    public void onEvent(CloseRequestViewMVC.CloseRequestButtonClickEvent event) {
        closeRequest();
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------


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
     * Store information about request closure in a local database
     */
    private void closeRequest() {

        String closedBy = getActiveAccount().name;
        if (TextUtils.isEmpty(closedBy)) {
            Toast.makeText(getActivity(), "No active account found", Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG, "No active account found - request close failed");
            return;
        }

        showProgressDialog("Please wait...", "Closing the request...");

        StringBuilder sb = new StringBuilder("");
        for (int i=0; i<mCameraPicturesPaths.size(); i++) {
            sb.append(mCameraPicturesPaths.get(i));
            if (i < mCameraPicturesPaths.size()-1) sb.append(", ");
        }
        String closedPictures = sb.toString();

        Bundle bundleCloseRequest = mCloseRequestViewMVC.getViewState();
        String closedComment =
                bundleCloseRequest.getString(CloseRequestViewMVC.KEY_CLOSED_COMMENT);

        // Create JSON object containing comment and pictures

        JSONObject userActionParamJson = new JSONObject();
        try {
            userActionParamJson.put(Constants.FIELD_NAME_CLOSED_BY, closedBy);
            userActionParamJson.put(Constants.FIELD_NAME_CLOSED_COMMENT, closedComment);
            userActionParamJson.put(Constants.FIELD_NAME_CLOSED_PICTURES, closedPictures);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            dismissProgressDialog();
            return;
        }
        String userActionParam = userActionParamJson.toString();

        // Entries to update request with LOCALLY_MODIFIED flag
        final ContentValues requestCV = new ContentValues();
        requestCV.put(IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG, 1);

        // Create entries for user action corresponding to request's close
        final ContentValues userActionCV = new ContentValues();
        userActionCV.put(IDoCareContract.UserActions.COL_TIMESTAMP, System.currentTimeMillis());
        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_TYPE,
                IDoCareContract.UserActions.ENTITY_TYPE_REQUEST);
        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_ID, mRequestId);
        userActionCV.put(IDoCareContract.UserActions.COL_ACTION_TYPE,
                IDoCareContract.UserActions.ACTION_TYPE_CLOSE_REQUEST);
        userActionCV.put(IDoCareContract.UserActions.COL_ACTION_PARAM, userActionParam);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getContentResolver().update(
                        ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI, mRequestId),
                        requestCV,
                        null,
                        null);
                getContentResolver().insert(IDoCareContract.UserActions.CONTENT_URI, userActionCV);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dismissProgressDialog();

                // Create a bundle and put the id there
                Bundle args = new Bundle();
                args.putLong(Constants.FIELD_NAME_REQUEST_ID, mRequestId);

                replaceFragment(RequestDetailsFragment.class, false, true, args);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] {null});


    }

}
