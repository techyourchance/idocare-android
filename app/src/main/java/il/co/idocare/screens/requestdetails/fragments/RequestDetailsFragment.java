package il.co.idocare.screens.requestdetails.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.dialogs.DialogsFactory;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.dialogs.events.PromptDialogDismissedEvent;
import il.co.idocare.mvcviews.requestdetails.RequestDetailsViewMvc;
import il.co.idocare.mvcviews.requestdetails.RequestDetailsViewMvcImpl;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.pictures.ImageViewPictureLoader;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.requests.RequestsChangedEvent;
import il.co.idocare.requests.RequestsManager;
import il.co.idocare.screens.common.MainFrameHelper;
import il.co.idocare.screens.common.fragments.BaseScreenFragment;
import il.co.idocare.useractions.UserActionEntityFactory;
import il.co.idocare.useractions.UserActionsManager;
import il.co.idocare.useractions.entities.PickUpRequestUserActionEntity;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.useractions.entities.VoteForRequestUserActionEntity;
import il.co.idocare.users.UserEntity;
import il.co.idocare.users.UsersManager;
import il.co.idocare.users.events.UserDataChangedEvent;
import il.co.idocare.utils.Logger;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrable;

@EventBusRegistrable
public class RequestDetailsFragment extends BaseScreenFragment implements
        RequestDetailsViewMvc.RequestDetailsViewMvcListener,
        RequestsManager.RequestsManagerListener, UsersManager.UsersManagerListener {

    private final static String TAG = "RequestDetailsFragment";

    private static final String ARG_REQUEST_ID = "ARG_REQUEST_ID";

    private static final String USER_LOGIN_DIALOG_TAG = "USER_LOGIN_DIALOG_TAG";


    private RequestDetailsViewMvc mRequestDetailsViewMvc;

    @Inject LoginStateManager mLoginStateManager;
    @Inject ServerSyncController mServerSyncController;
    @Inject ImageViewPictureLoader mImageViewPictureLoader;
    @Inject UserActionsManager mUserActionsManager;
    @Inject RequestsManager mRequestsManager;
    @Inject UsersManager mUsersManager;
    @Inject UserActionEntityFactory mUserActionEntityFactory;
    @Inject MainFrameHelper mMainFrameHelper;
    @Inject DialogsManager mDialogsManager;
    @Inject DialogsFactory mDialogsFactory;
    @Inject Logger mLogger;

    private String mRequestId;
    private RequestEntity mRequest;

    public static RequestDetailsFragment newInstance(String requestId) {
        RequestDetailsFragment fragment = new RequestDetailsFragment();
        Bundle args = new Bundle();
        args.putString(RequestDetailsFragment.ARG_REQUEST_ID, requestId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getControllerComponent().inject(this);

        mRequestDetailsViewMvc =
                new RequestDetailsViewMvcImpl(inflater, container, mImageViewPictureLoader);
        mRequestDetailsViewMvc.registerListener(this);

        mRequestId = getArguments().getString(ARG_REQUEST_ID);

        // Initialize the MapView inside the MVC view
        ((MapView) mRequestDetailsViewMvc.getRootView().findViewById(R.id.map_preview))
                .onCreate(savedInstanceState);

        return mRequestDetailsViewMvc.getRootView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mRequestsManager.registerListener(this);
        mRequestsManager.fetchRequestByIdAndNotify(mRequestId);
        mRequestDetailsViewMvc.bindCurrentUserId(mLoginStateManager.getLoggedInUser().getUserId());
    }

    @Override
    public void onStop() {
        super.onStop();
        mRequestsManager.unregisterListener(this);
    }

    @Override
    public String getTitle() {
        return getResources().getString(R.string.request_details_fragment_title);
    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateManager.UserLoggedOutEvent event) {
        mRequestDetailsViewMvc.bindCurrentUserId(mLoginStateManager.getLoggedInUser().getUserId());
        refreshRequest();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserDataChangedEvent event) {
        refreshUsers();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RequestsChangedEvent event) {
        refreshRequest();
    }


    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // Callbacks from MVC view(s)

    @Override
    public void onCloseRequestClicked() {
        closeRequest();
    }

    @Override
    public void onPickupRequestClicked() {
        pickupRequest();
    }

    @Override
    public void onClosedVoteUpClicked() {
        voteForRequest(mUserActionEntityFactory.newVoteUpForRequestClosed(mRequestId));
    }

    @Override
    public void onClosedVoteDownClicked() {
        voteForRequest(mUserActionEntityFactory.newVoteDownForRequestClosed(mRequestId));
    }

    @Override
    public void onCreatedVoteUpClicked() {
        voteForRequest(mUserActionEntityFactory.newVoteUpForRequestCreated(mRequestId));
    }

    @Override
    public void onCreatedVoteDownClicked() {
        voteForRequest(mUserActionEntityFactory.newVoteDownForRequestCreated(mRequestId));
    }

    // End of callbacks from MVC view(s)
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // User actions handling


    private void pickupRequest() {

        // TODO: request pickup should be allowed only when there is a network connection

        final String pickedUpBy = mLoginStateManager.getLoggedInUser().getUserId();

        // If no logged in user - ask him to log in and rerun this method in case he does
        if (TextUtils.isEmpty(pickedUpBy)) {
            askUserToLogIn();
            return;
        }

        PickUpRequestUserActionEntity pickUpRequestUserAction =
                mUserActionEntityFactory.newPickUpRequest(mRequestId, pickedUpBy);

        mUserActionsManager.addUserActionAndNotify(
                pickUpRequestUserAction,
                new UserActionsManager.UserActionsManagerListener() {
                    @Override
                    public void onUserActionAdded(UserActionEntity userAction) {
                        refreshRequest();
                        // Request pickup is time critical action - need to be uploaded to the server ASAP
                        mServerSyncController.requestImmediateSync();

                    }
                });


    }

    private void askUserToLogIn() {
        mDialogsManager.showRetainedDialogWithTag(
                mDialogsFactory.newPromptDialog(
                        getString(R.string.dialog_title_login_required),
                        getString(R.string.msg_ask_to_log_in_before_pickup),
                        getResources().getString(R.string.btn_dialog_positive),
                        getResources().getString(R.string.btn_dialog_negative)),
                USER_LOGIN_DIALOG_TAG);
    }

    @Subscribe
    public void onPromptDialogDismissed(PromptDialogDismissedEvent event) {
        if (event.getTag().equals(USER_LOGIN_DIALOG_TAG)) {
            if (event.getClickedButtonIndex() == PromptDialogDismissedEvent.BUTTON_POSITIVE) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        }
    }

    private void closeRequest() {

        CloseRequestFragment fragment = CloseRequestFragment.newInstance(
                mRequestId, mRequest.getLongitude(), mRequest.getLatitude());
        mMainFrameHelper.replaceFragment(fragment, true, false);
    }


    private void voteForRequest(VoteForRequestUserActionEntity entity) {
        String activeUserId = mLoginStateManager.getLoggedInUser().getUserId();

        // If no logged in user - ask him to log in
        if (TextUtils.isEmpty(activeUserId)) {
            askUserToLogIn();
            return;
        }

        mRequestsManager.voteForRequest(entity);

    }

    private void refreshRequest() {
        mRequestsManager.fetchRequestByIdAndNotify(mRequestId);
    }


    private void refreshUsers() {

        List<String> usersIds = new ArrayList<>(3);

        usersIds.add(mRequest.getCreatedBy());

        String pickedUpBy = mRequest.getPickedUpBy();
        String closedBy = mRequest.getClosedBy();

        if (pickedUpBy != null && !pickedUpBy.isEmpty()) {
            usersIds.add(pickedUpBy);
        }

        if (closedBy != null && !closedBy.isEmpty()) {
            usersIds.add(closedBy);
        }

        mUsersManager.fetchUsersByIdAndNotify(usersIds, this);

    }

    @Override
    public void onRequestsFetched(List<RequestEntity> requests) {
        mLogger.d(TAG, "onRequestsFetched() called");
        if (requests.isEmpty()) {
            throw new RuntimeException("failed to fetch request info");
        }
        mRequest = requests.get(0);
        mRequestDetailsViewMvc.bindRequest(mRequest);
        refreshUsers();
    }

    @Override
    public void onUsersFetched(List<UserEntity> users) {
        mLogger.d(TAG, "onUsersFetched() called");

        for (UserEntity user : users) {
            if (user.getUserId().equals(mRequest.getCreatedBy())) {
                mRequestDetailsViewMvc.bindCreatedByUser(user);
            } else if (user.getUserId().equals(mRequest.getPickedUpBy())) {
                mRequestDetailsViewMvc.bindPickedUpByUser(user);
            } else if (user.getUserId().equals(mRequest.getClosedBy())) {
                mRequestDetailsViewMvc.bindClosedByUser(user);
            } else {
                mLogger.w(TAG, "user not related to request; user ID: " + user.getUserId());
            }
        }
    }
}
