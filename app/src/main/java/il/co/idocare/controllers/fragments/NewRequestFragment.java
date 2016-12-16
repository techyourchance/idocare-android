package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.content.ContentValues;
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

import java.util.List;
import java.util.UUID;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.eventbusevents.LocationEvents;
import il.co.idocare.mvcviews.newrequest.NewRequestViewMvc;
import il.co.idocare.mvcviews.newrequest.NewRequestViewMvcImpl;
import il.co.idocare.screens.requests.fragments.RequestsAllFragment;


public class NewRequestFragment extends NewAndCloseRequestBaseFragment
        implements NewRequestViewMvc.NewRequestViewMvcListener {

    private static final String TAG = "NewRequestFragment";

    NewRequestViewMvc mNewRequestViewMvc;



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getControllerComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mNewRequestViewMvc = new NewRequestViewMvcImpl(inflater, container);
        mNewRequestViewMvc.registerListener(this);

        return mNewRequestViewMvc.getRootView();
    }


    @Override
    public boolean isTopLevelFragment() {
        return false;
    }

    @Override
    public Class<? extends AbstractFragment> getNavHierParentFragment() {
        return RequestsAllFragment.class;
    }

    @Override
    public String getTitle() {
        return getResources().getString(R.string.new_request_fragment_title);
    }




    // ---------------------------------------------------------------------------------------------
    //
    // Callbacks from MVC view(s)

    @Override
    public void onCreateRequestClicked() {
        createRequest();
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
        mNewRequestViewMvc.showPicture(index, pathToPicture);
    }

    @Override
    protected int getMaxPictures() {
        return NewRequestViewMvc.MAX_PICTURES;
    }

    /**
     * Add new request to the local cache, mark it as modified and add the corresponding user
     * action to the local cache of user actions
     */
    private void createRequest() {
        String createdBy = mLoginStateManager.getActiveAccountUserId();

        LocationEvents.BestLocationEstimateEvent bestLocationEstimateEvent =
                EventBus.getDefault().getStickyEvent(LocationEvents.BestLocationEstimateEvent.class);
        if (bestLocationEstimateEvent == null
                || !mLocationHelper.isAccurateLocation(bestLocationEstimateEvent.location)) {
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
        List<String> picturesPahts = getPicturesPaths();
        StringBuilder sb = new StringBuilder("");
        for (int i=0; i < picturesPahts.size(); i++) {
            sb.append(picturesPahts.get(i));
            if (i < picturesPahts.size()-1) sb.append(Constants.PICTURES_LIST_SEPARATOR);
        }
        String createdPictures = sb.toString();

        Bundle bundleNewRequest = mNewRequestViewMvc.getViewState();
        String createdComment =
                bundleNewRequest.getString(NewRequestViewMvc.KEY_CREATED_COMMENT);

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
                mServerSyncController.requestImmediateSync(); // TODO: remove this after geocoder and names appear without sync
                replaceFragment(RequestsAllFragment.class, false, true, null);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] {null});

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
