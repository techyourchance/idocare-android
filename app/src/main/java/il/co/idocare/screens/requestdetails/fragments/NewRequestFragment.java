package il.co.idocare.screens.requestdetails.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.mvcviews.newrequest.NewRequestViewMvc;
import il.co.idocare.mvcviews.newrequest.NewRequestViewMvcImpl;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.requests.RequestsManager;
import il.co.idocare.screens.requests.fragments.RequestsAllFragment;
import il.co.idocare.utils.IdcDateTimeUtils;
import il.co.idocare.utils.Logger;


public class NewRequestFragment extends NewAndCloseRequestBaseFragment
        implements NewRequestViewMvc.NewRequestViewMvcListener {

    private static final String TAG = "NewRequestFragment";

    @Inject RequestsManager mRequestsManager;
    @Inject Logger mLogger;

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

    @Nullable
    @Override
    public Class<? extends Fragment> getHierarchicalParentFragment() {
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
        mMainFrameHelper.replaceFragment(RequestDetailsFragment.newInstance(tempId), false, true);
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
