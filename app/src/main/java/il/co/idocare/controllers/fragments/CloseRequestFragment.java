package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.eventbusevents.LocationEvents;
import il.co.idocare.helpers.LocationHelper;
import il.co.idocare.mvcviews.closerequest.CloseRequestViewMvc;
import il.co.idocare.mvcviews.closerequest.CloseRequestViewMvcImpl;
import il.co.idocare.pictures.CameraAdapter;
import il.co.idocare.utils.UtilMethods;


public class CloseRequestFragment extends NewAndCloseRequestBaseFragment
        implements CloseRequestViewMvc.CloseRequestViewMvcListener {

    private final static String TAG = "CloseRequestFragment";

    private CloseRequestViewMvc mCloseRequestViewMvc;

    private long mRequestId;
    private Location mRequestLocation;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getControllerComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCloseRequestViewMvc = new CloseRequestViewMvcImpl(inflater, container);
        mCloseRequestViewMvc.registerListener(this);

        Bundle args = getArguments();
        if (args != null) {
            mRequestId = args.getLong(Constants.FIELD_NAME_REQUEST_ID);
            // TODO: the only argument should be request ID - use loader in order to load request's info!
            mRequestLocation = new Location("none");
            mRequestLocation.setLongitude(args.getDouble(Constants.FIELD_NAME_LONGITUDE));
            mRequestLocation.setLatitude(args.getDouble(Constants.FIELD_NAME_LATITUDE));
        } else {
            Log.e(TAG, "no arguments set for CloseRequestFragment");
            navigateUp();
        }

        return mCloseRequestViewMvc.getRootView();
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
    // Callbacks from MVC view(s)


    @Override
    public void onCloseRequestClicked() {
        closeRequest();
    }

    @Override
    public void onTakePictureClicked() {
        takePictureWithCamera();
    }

    // End callbacks from MVC view(s)
    //
    // ---------------------------------------------------------------------------------------------



    @Override
    protected void onNewPictureAdded(int index, @NonNull String pathToPicture) {
        mCloseRequestViewMvc.showPicture(index, pathToPicture);
    }

    @Override
    protected int getMaxPictures() {
        return CloseRequestViewMvc.MAX_PICTURES;
    }


    /**
     * Store information about request closure in a local database
     */
    private void closeRequest() {

        String closedBy = mLoginStateManager.getActiveAccountUserId();

        if (!isValidLocation()) {
            Log.d(TAG, "aborting request close due to invalid location");
            return;
        }

        List<String> picturesPaths = getPicturesPaths();
        StringBuilder sb = new StringBuilder("");
        for (int i=0; i < picturesPaths.size(); i++) {
            sb.append(picturesPaths.get(i));
            if (i < picturesPaths.size()-1) sb.append(", ");
        }
        String closedPictures = sb.toString();

        Bundle bundleCloseRequest = mCloseRequestViewMvc.getViewState();
        String closedComment =
                bundleCloseRequest.getString(CloseRequestViewMvc.KEY_CLOSED_COMMENT);

        if (!validRequestParameters(closedBy, closedPictures)) {
            Log.d(TAG, "aborting request close due to invalid parameters");
            return;
        }


        // Create JSON object containing comment and pictures

        JSONObject userActionParamJson = new JSONObject();
        try {
            userActionParamJson.put(Constants.FIELD_NAME_CLOSED_BY, closedBy);
            userActionParamJson.put(Constants.FIELD_NAME_CLOSED_COMMENT, closedComment);
            userActionParamJson.put(Constants.FIELD_NAME_CLOSED_PICTURES, closedPictures);
        } catch (JSONException e) {
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


        showProgressDialog("Please wait...", "Closing the request...");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getActivity().getContentResolver().update(
                        ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI, mRequestId),
                        requestCV,
                        null,
                        null);
                getActivity().getContentResolver().insert(IDoCareContract.UserActions.CONTENT_URI, userActionCV);
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


    private boolean isValidLocation() {

        LocationEvents.BestLocationEstimateEvent bestLocationEstimateEvent =
                EventBus.getDefault().getStickyEvent(LocationEvents.BestLocationEstimateEvent.class);
        if (bestLocationEstimateEvent == null
                || !mLocationHelper.isAccurateLocation(bestLocationEstimateEvent.location)) {
            Toast.makeText(getActivity(), getString(R.string.msg_insufficient_location_accuracy),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        Location location = bestLocationEstimateEvent.location;

        if (mLocationHelper.areSameLocations(location, mRequestLocation)) {
            Log.d(TAG, "location is too far from request's location");
            Toast.makeText(getActivity(), getString(R.string.msg_too_far_from_original_location),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean validRequestParameters(String userId, String pictures) {

        if (TextUtils.isEmpty(userId)) {
            onUserLoggedOut();
            return false;
        }

        if (TextUtils.isEmpty(pictures)) {
            Toast.makeText(getActivity(), getString(R.string.msg_pictures_required),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

}
