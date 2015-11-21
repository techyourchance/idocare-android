package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.GlobalEvents;
import il.co.idocare.R;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.pictures.CameraAdapter;
import il.co.idocare.utils.UtilMethods;
import il.co.idocare.views.NewRequestViewMVC;


public class NewRequestFragment extends AbstractFragment {

    private final static String TAG = NewRequestFragment.class.getSimpleName();

    NewRequestViewMVC mViewMVC;

    private long mRequestId;

    private String mLastCameraPicturePath;
    private List<String> mCameraPicturesPaths = new ArrayList<String>(3);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewMVC = new NewRequestViewMVC(inflater, container);

        setActionBarTitle(getTitle());

        // Restore state from bundle (if required)
        restoreSavedStateIfNeeded(savedInstanceState);

        return mViewMVC.getRootView();
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
    public void onResume() {
        super.onResume();
        if (getUserStateManager().getActiveAccount() == null) {
            // The user logged out while this fragment was paused
            userLoggedOut();
            return;
        }

        // high accuracy location required for request creation
        EventBus.getDefault().post(new GlobalEvents.HighAccuracyLocationRequiredEvent());
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
        return getResources().getString(R.string.new_request_fragment_title);
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
        if (requestCode == Constants.REQUEST_CODE_TAKE_PICTURE) {
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


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    public void onEvent(NewRequestViewMVC.TakePictureButtonClickEvent event) {
        takePictureWithCamera();
    }

    public void onEvent(NewRequestViewMVC.CreateNewRequestButtonClickEvent event) {
        createRequest();
    }

    public void onEventMainThread(UserStateManager.UserLoggedOutEvent event) {
        userLoggedOut();
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------


    private void showPicture(String cameraPicturePath) {
        showPicture(mCameraPicturesPaths.size(), cameraPicturePath);
    }

    private void showPicture(int position, String cameraPicturePath) {
        if (position >= 3) {
            Log.e(TAG, "maximal number of pictures exceeded!");
            return;
        }
        if (mCameraPicturesPaths.size() > position) {
            mCameraPicturesPaths.remove(position);
        }
        mCameraPicturesPaths.add(position, cameraPicturePath);
        mViewMVC.showPicture(position, cameraPicturePath);
    }


    /**
     * Take a new picture with camera
     */
    private void takePictureWithCamera() {
        CameraAdapter cameraAdapter = new CameraAdapter(getActivity());
        mLastCameraPicturePath = cameraAdapter.takePicture(
                Constants.REQUEST_CODE_TAKE_PICTURE, "new_request");
    }


    /**
     * Add new request to the local cache, mark it as modified and add the corresponding user
     * action to the local cache of user actions
     */
    private void createRequest() {
        String createdBy = getUserStateManager().getActiveAccountUserId();

        GlobalEvents.BestLocationEstimateEvent bestLocationEstimateEvent =
                EventBus.getDefault().getStickyEvent(GlobalEvents.BestLocationEstimateEvent.class);
        if (bestLocationEstimateEvent == null
                || !isValidLocation(bestLocationEstimateEvent.location)) {
            Log.d(TAG, "aborting request creation due to lack of, or insufficiently accurate " +
                    "location estimate");
            Toast.makeText(getActivity(), getString(R.string.msg_insufficient_location_accuracy),
                    Toast.LENGTH_LONG).show();
            return;
        }

        Location location = bestLocationEstimateEvent.location;

        String latitude = "", longitude = "";
        if (location != null) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
        }
        StringBuilder sb = new StringBuilder("");
        for (int i=0; i<mCameraPicturesPaths.size(); i++) {
            sb.append(mCameraPicturesPaths.get(i));
            if (i < mCameraPicturesPaths.size()-1) sb.append(Constants.PICTURES_LIST_SEPARATOR);
        }
        String createdPictures = sb.toString();

        Bundle bundleNewRequest = mViewMVC.getViewState();
        String pollutionLevel =
                bundleNewRequest.getString(NewRequestViewMVC.KEY_CREATED_POLLUTION_LEVEL);
        String createdComment =
                bundleNewRequest.getString(NewRequestViewMVC.KEY_CREATED_COMMENT);

        // Generate a temporary ID for this request - the actual ID will be assigned by the server
        // TODO: this ID might be not unique
        long tempId = UUID.randomUUID().getLeastSignificantBits();

        long timestamp = System.currentTimeMillis();

        if (!validRequestParameters(createdBy, createdPictures)) {
            Log.d(TAG, "aborting request creation due to invalid parameters");
            return;
        }

        // Create entries for a newly created request
        final ContentValues requestCV = new ContentValues();
        requestCV.put(IDoCareContract.Requests.COL_REQUEST_ID, tempId);
        requestCV.put(IDoCareContract.Requests.COL_CREATED_BY, createdBy);
        requestCV.put(IDoCareContract.Requests.COL_CREATED_AT, timestamp);
        requestCV.put(IDoCareContract.Requests.COL_CREATED_COMMENT, createdComment);
        requestCV.put(IDoCareContract.Requests.COL_CREATED_PICTURES, createdPictures);
        requestCV.put(IDoCareContract.Requests.COL_LONGITUDE, longitude);
        requestCV.put(IDoCareContract.Requests.COL_LATITUDE, latitude);
        requestCV.put(IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG, "1");

        // Create entries for user action corresponding to request's creation
        final ContentValues userActionCV = new ContentValues();
        userActionCV.put(IDoCareContract.UserActions.COL_TIMESTAMP, timestamp);
        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_TYPE,
                IDoCareContract.UserActions.ENTITY_TYPE_REQUEST);
        userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_ID, tempId);
        userActionCV.put(IDoCareContract.UserActions.COL_ACTION_TYPE, IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST);

        showProgressDialog("Please wait...", "Creating new request...");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getActivity().getContentResolver().insert(IDoCareContract.Requests.CONTENT_URI, requestCV);
                getActivity().getContentResolver().insert(IDoCareContract.UserActions.CONTENT_URI, userActionCV);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dismissProgressDialog();
                ((MainActivity)getActivity()).requestImmediateSync(); // TODO: remove this after geocoder and names appear without sync
                replaceFragment(HomeFragment.class, false, true, null);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] {null});

    }


    private boolean isValidLocation(Location location) {
        if (location == null || !location.hasAccuracy()
                || location.getAccuracy() > Constants.MINIMUM_ACCEPTABLE_LOCATION_ACCURACY_METERS) {
            return false;
        }
        return true;
    }

    private boolean validRequestParameters(String userId, String pictures) {

        if (TextUtils.isEmpty(userId)) {
            userLoggedOut();
            return false;
        }

        if (TextUtils.isEmpty(pictures)) {
            Toast.makeText(getActivity(), getString(R.string.msg_pictures_required),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void userLoggedOut() {
        Log.d(TAG, "userLoggedOut() is called");
        // This is a very simplified handling of user's logout
        // TODO: think of a better handling and possible corner cases
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            replaceFragment(getNavHierParentFragment(), false, false, null);
        }
    }

}
