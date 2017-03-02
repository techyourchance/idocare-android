package il.co.idocare.screens.requestdetails.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.authentication.LoggedInUserEntity;
import il.co.idocare.mvcviews.closerequest.CloseRequestViewMvc;
import il.co.idocare.mvcviews.closerequest.CloseRequestViewMvcImpl;
import il.co.idocare.screens.requests.fragments.RequestsAllFragment;
import il.co.idocare.useractions.UserActionEntityFactory;
import il.co.idocare.useractions.UserActionsManager;
import il.co.idocare.useractions.entities.CloseRequestUserActionEntity;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.utils.Logger;


public class CloseRequestFragment extends NewAndCloseRequestBaseFragment
        implements CloseRequestViewMvc.CloseRequestViewMvcListener {

    private final static String TAG = "CloseRequestFragment";

    @Inject UserActionEntityFactory mUserActionEntityFactory;
    @Inject UserActionsManager mUserActionsManager;
    @Inject Logger mLogger;

    private CloseRequestViewMvc mCloseRequestViewMvc;

    private String mRequestId;
    private Location mRequestLocation;

    public static CloseRequestFragment newInstance(String requestId, double longitude, double latitude) {
        CloseRequestFragment fragment = new CloseRequestFragment();

        Bundle args = new Bundle();
        args.putString(Constants.FIELD_NAME_REQUEST_ID, requestId);
        args.putDouble(Constants.FIELD_NAME_LATITUDE, latitude);
        args.putDouble(Constants.FIELD_NAME_LONGITUDE, longitude);
        fragment.setArguments(args);

        return fragment;
    }


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

        mRequestId = args.getString(Constants.FIELD_NAME_REQUEST_ID);
        // TODO: the only argument should be request ID - use loader in order to load request's info!
        mRequestLocation = new Location("none");
        mRequestLocation.setLongitude(args.getDouble(Constants.FIELD_NAME_LONGITUDE));
        mRequestLocation.setLatitude(args.getDouble(Constants.FIELD_NAME_LATITUDE));

        return mCloseRequestViewMvc.getRootView();
    }


    @Nullable
    @Override
    public Class<? extends Fragment> getHierarchicalParentFragment() {
        return RequestsAllFragment.class;
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
        mLogger.d(TAG, "closeRequest()");

        LoggedInUserEntity user = mLoginStateManager.getLoggedInUser();

        if (user == null) {
            mLogger.e(TAG, "no logged in user - aborting");
            return;
        }
        String closedBy = user.getUserId();

        if (!isValidLocation()) {
            Log.d(TAG, "aborting request close due to invalid location");
            return;
        }

        List<String> closedPictures = getPicturesPaths();

        Bundle bundleCloseRequest = mCloseRequestViewMvc.getViewState();
        String closedComment =
                bundleCloseRequest.getString(CloseRequestViewMvc.KEY_CLOSED_COMMENT);

        if (!validRequestParameters(closedBy, closedPictures)) {
            Log.d(TAG, "aborting request close due to invalid parameters");
            return;
        }

        CloseRequestUserActionEntity closeRequestUserAction =
                mUserActionEntityFactory.newCloseRequest(mRequestId, closedBy, closedComment, closedPictures);

        mUserActionsManager.addUserActionAndNotify(
                closeRequestUserAction,
                new UserActionsManager.UserActionsManagerListener() {
                    @Override
                    public void onUserActionAdded(UserActionEntity userAction) {
                        RequestDetailsFragment fragment = RequestDetailsFragment.newInstance(userAction.getEntityId());
                        mMainFrameHelper.replaceFragment(fragment, false, true);
                    }
                });
    }


    private boolean isValidLocation() {
        Location currentLocation = getCurrentLocation();

        if (currentLocation == null) {
            Log.d(TAG, "current location not available");
            Toast.makeText(getActivity(), getString(R.string.msg_insufficient_location_accuracy),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (!mIdcLocationManager.areSameLocations(getCurrentLocation(), mRequestLocation)) {
            Log.d(TAG, "location is too far from request's location");
            Toast.makeText(getActivity(), getString(R.string.msg_too_far_from_original_location),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean validRequestParameters(String userId, List<String> pictures) {

        if (TextUtils.isEmpty(userId)) {
            onUserLoggedOut();
            return false;
        }

        if (pictures.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.msg_pictures_required),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

}
