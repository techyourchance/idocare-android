package il.co.idocarerequests.screens.requestdetails.fragments;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.techyourchance.fragmenthelper.FragmentHelper;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import il.co.idocarecore.Constants;
import il.co.idocarecore.authentication.LoggedInUserEntity;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.location.IdcLocationManager;
import il.co.idocarecore.pictures.CameraAdapter;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarecore.serversync.ServerSyncController;
import il.co.idocarecore.useractions.UserActionEntityFactory;
import il.co.idocarecore.useractions.UserActionsManager;
import il.co.idocarecore.useractions.entities.CloseRequestUserActionEntity;
import il.co.idocarecore.useractions.entities.UserActionEntity;
import il.co.idocarecore.utils.Logger;
import il.co.idocarerequests.R;
import il.co.idocarerequests.screens.requestdetails.mvcviews.CloseRequestViewMvc;
import il.co.idocarerequests.screens.requestdetails.mvcviews.CloseRequestViewMvcImpl;


public class CloseRequestFragment extends NewAndCloseRequestBaseFragment
        implements CloseRequestViewMvc.CloseRequestViewMvcListener {

    private final static String TAG = "CloseRequestFragment";

    public static final String ARG_REQUEST_ID = "ARG_REQUEST_ID";

    private final ScreensNavigator mScreensNavigator;
    private final UserActionEntityFactory mUserActionEntityFactory;
    private final UserActionsManager mUserActionsManager;
    private final Logger mLogger;

    private CloseRequestViewMvc mCloseRequestViewMvc;

    private String mRequestId;
    private Location mRequestLocation;

    @Inject
    public CloseRequestFragment(LoginStateManager loginStateManager,
                                CameraAdapter cameraAdapter,
                                IdcLocationManager idcLocationManager,
                                ServerSyncController serverSyncController,
                                FragmentHelper fragmentHelper,
                                ScreensNavigator screensNavigator,
                                UserActionEntityFactory userActionEntityFactory,
                                UserActionsManager userActionsManager,
                                Logger logger) {
        super(loginStateManager, cameraAdapter, idcLocationManager, serverSyncController, fragmentHelper);
        mScreensNavigator = screensNavigator;
        mUserActionEntityFactory = userActionEntityFactory;
        mUserActionsManager = userActionsManager;
        mLogger = logger;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCloseRequestViewMvc = new CloseRequestViewMvcImpl(inflater, container);
        mCloseRequestViewMvc.registerListener(this);

        Bundle args = getArguments();

        mRequestId = args.getString(ARG_REQUEST_ID);
        // TODO: the only argument should be request ID - use loader in order to load request's info!
        mRequestLocation = new Location("none");
        mRequestLocation.setLongitude(args.getDouble(Constants.FIELD_NAME_LONGITUDE));
        mRequestLocation.setLatitude(args.getDouble(Constants.FIELD_NAME_LATITUDE));

        return mCloseRequestViewMvc.getRootView();
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
                        mScreensNavigator.toRequestDetails(userAction.getEntityId());
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
