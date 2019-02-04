package il.co.idocarerequests.screens.requestdetails.fragments;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.location.IdcLocationManager;
import il.co.idocarecore.pictures.CameraAdapter;
import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.requests.RequestsManager;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarecore.screens.common.fragmenthelper.FragmentHelper;
import il.co.idocarecore.serversync.ServerSyncController;
import il.co.idocarecore.utils.IdcDateTimeUtils;
import il.co.idocarecore.utils.Logger;
import il.co.idocarerequests.R;
import il.co.idocarerequests.screens.requestdetails.mvcviews.NewRequestViewMvc;
import il.co.idocarerequests.screens.requestdetails.mvcviews.NewRequestViewMvcImpl;


public class NewRequestFragment extends NewAndCloseRequestBaseFragment
        implements NewRequestViewMvc.NewRequestViewMvcListener {

    private static final String TAG = "NewRequestFragment";

    private final ScreensNavigator mScreensNavigator;
    private final RequestsManager mRequestsManager;
    private final Logger mLogger;

    private NewRequestViewMvc mNewRequestViewMvc;

    @Inject
    public NewRequestFragment(LoginStateManager loginStateManager, CameraAdapter cameraAdapter, IdcLocationManager idcLocationManager, ServerSyncController serverSyncController, FragmentHelper fragmentHelper, ScreensNavigator screensNavigator, RequestsManager requestsManager, Logger logger) {
        super(loginStateManager, cameraAdapter, idcLocationManager, serverSyncController, fragmentHelper);
        mScreensNavigator = screensNavigator;
        mRequestsManager = requestsManager;
        mLogger = logger;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mNewRequestViewMvc = new NewRequestViewMvcImpl(inflater, container);
        mNewRequestViewMvc.registerListener(this);

        return mNewRequestViewMvc.getRootView();
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
        mLogger.d(TAG, "createRequest()");

        Location location = getCurrentLocation();

        if (location == null) {
            Log.d(TAG, "aborting request creation due to unavailable accurate location");
            Toast.makeText(getActivity(), getString(R.string.msg_insufficient_location_accuracy),
                    Toast.LENGTH_LONG).show();
            return;
        }

        String createdBy = mLoginStateManager.getLoggedInUser().getUserId();

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        List<String> createdPictures = getPicturesPaths();

        Bundle bundleNewRequest = mNewRequestViewMvc.getViewState();
        String createdComment =
                bundleNewRequest.getString(NewRequestViewMvc.KEY_CREATED_COMMENT);

        // Generate a temporary ID for this request - the actual ID will be assigned by the server
        String tempId = UUID.randomUUID().toString();

        if (!validRequestParameters(createdBy, createdPictures)) {
            Log.d(TAG, "aborting request creation due to invalid parameters");
            return;
        }

        RequestEntity newRequest = RequestEntity.getBuilder()
                .setId(tempId)
                .setCreatedBy(createdBy)
                .setCreatedAt(IdcDateTimeUtils.getCurrentDateTimeLocalized())
                .setCreatedComment(createdComment)
                .setCreatedPictures(createdPictures)
                .setLongitude(longitude)
                .setLatitude(latitude)
                .build();

        mRequestsManager.addNewRequest(newRequest);

        mServerSyncController.requestImmediateSync(); // TODO: remove this after geocoder and names appear without sync
        mScreensNavigator.toRequestDetails(tempId);
    }

    private boolean validRequestParameters(String userId, List<String> pictures) {

        if (TextUtils.isEmpty(userId)) {
            onUserLoggedOut();
            return false;
        }

        if (pictures == null || pictures.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.msg_pictures_required),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


}
