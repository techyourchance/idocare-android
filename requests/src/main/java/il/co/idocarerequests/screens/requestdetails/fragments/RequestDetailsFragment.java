package il.co.idocarerequests.screens.requestdetails.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapView;
import com.techyourchance.fragmenthelper.FragmentHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.authentication.events.UserLoggedOutEvent;
import il.co.idocarecore.pictures.ImageViewPictureLoader;
import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.requests.RequestsManager;
import il.co.idocarecore.requests.events.RequestIdChangedEvent;
import il.co.idocarecore.requests.events.RequestsChangedEvent;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarecore.screens.common.dialogs.DialogsFactory;
import il.co.idocarecore.screens.common.dialogs.DialogsManager;
import il.co.idocarecore.screens.common.dialogs.PromptDialogDismissedEvent;
import il.co.idocarecore.useractions.UserActionEntityFactory;
import il.co.idocarecore.useractions.UserActionsManager;
import il.co.idocarecore.useractions.entities.PickUpRequestUserActionEntity;
import il.co.idocarecore.useractions.entities.UserActionEntity;
import il.co.idocarecore.useractions.entities.VoteForRequestUserActionEntity;
import il.co.idocarecore.users.UserEntity;
import il.co.idocarecore.users.UsersManager;
import il.co.idocarecore.users.events.UserDataChangedEvent;
import il.co.idocarecore.utils.Logger;
import il.co.idocarecore.utils.eventbusregistrator.EventBusRegistrable;
import il.co.idocarerequests.R;
import il.co.idocarerequests.screens.requestdetails.mvcviews.RequestDetailsViewMvc;
import il.co.idocarerequests.screens.requestdetails.mvcviews.RequestDetailsViewMvcImpl;

@EventBusRegistrable
public class RequestDetailsFragment extends Fragment implements
        RequestDetailsViewMvc.RequestDetailsViewMvcListener,
        RequestsManager.RequestsManagerListener,
        UsersManager.UsersManagerListener {

    private final static String TAG = "RequestDetailsFragment";

    public static final String ARG_REQUEST_ID = "ARG_REQUEST_ID";

    private static final String USER_LOGIN_DIALOG_TAG = "USER_LOGIN_DIALOG_TAG";


    private RequestDetailsViewMvc mRequestDetailsViewMvc;

    private final ScreensNavigator mScreensNavigator;
    private final LoginStateManager mLoginStateManager;
    private final ImageViewPictureLoader mImageViewPictureLoader;
    private final UserActionsManager mUserActionsManager;
    private final RequestsManager mRequestsManager;
    private final UsersManager mUsersManager;
    private final UserActionEntityFactory mUserActionEntityFactory;
    private final FragmentHelper mFragmentHelper;
    private final DialogsManager mDialogsManager;
    private final DialogsFactory mDialogsFactory;
    private final Logger mLogger;

    private String mRequestId;
    private RequestEntity mRequest;

    @Inject
    public RequestDetailsFragment(ScreensNavigator screensNavigator, LoginStateManager loginStateManager, ImageViewPictureLoader imageViewPictureLoader, UserActionsManager userActionsManager, RequestsManager requestsManager, UsersManager usersManager, UserActionEntityFactory userActionEntityFactory, FragmentHelper fragmentHelper, DialogsManager dialogsManager, DialogsFactory dialogsFactory, Logger logger) {
        mScreensNavigator = screensNavigator;
        mLoginStateManager = loginStateManager;
        mImageViewPictureLoader = imageViewPictureLoader;
        mUserActionsManager = userActionsManager;
        mRequestsManager = requestsManager;
        mUsersManager = usersManager;
        mUserActionEntityFactory = userActionEntityFactory;
        mFragmentHelper = fragmentHelper;
        mDialogsManager = dialogsManager;
        mDialogsFactory = dialogsFactory;
        mLogger = logger;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserLoggedOutEvent event) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RequestIdChangedEvent event) {
        if (mRequestId.equals(event.getOldId())) {
            mRequestId = event.getNewId();
            refreshRequest();
        }
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
                        mRequestsManager.syncRequestsFromServer();

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
        if (event.getDialogTag().equals(USER_LOGIN_DIALOG_TAG)) {
            if (event.getClickedButtonIndex() == PromptDialogDismissedEvent.BUTTON_POSITIVE) {
                mScreensNavigator.toLogin();
            }
        }
    }

    private void closeRequest() {

        mScreensNavigator.toCloseRequest(mRequestId, mRequest.getLongitude(), mRequest.getLatitude());
    }


    private void voteForRequest(VoteForRequestUserActionEntity voteAction) {
        String activeUserId = mLoginStateManager.getLoggedInUser().getUserId();

        // If no logged in user - ask him to log in
        if (TextUtils.isEmpty(activeUserId)) {
            askUserToLogIn();
            return;
        }

        mUserActionsManager.addUserActionAndNotify(
                voteAction,
                new UserActionsManager.UserActionsManagerListener() {
                    @Override
                    public void onUserActionAdded(UserActionEntity userAction) {
                        refreshRequest();
                    }
                });
    }

    private void refreshRequest() {
        mRequestsManager.fetchRequestByIdAndNotify(mRequestId);
    }


    private void refreshUsers() {

        if (mRequest == null) {
            // request's info hasn't been fetched yet - users info can't be refreshed
            return;
        }

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
            mLogger.e(TAG, "couldn't fetch request's info; navigating up");
            mFragmentHelper.navigateUp();
            return;
        }
        mRequest = requests.get(0);
        mRequestDetailsViewMvc.bindRequest(mRequest);
        refreshUsers();
    }

    @Override
    public void onUsersFetched(List<UserEntity> users) {
        mLogger.d(TAG, "onUsersFetched() called");

        for (UserEntity user : users) {
            // one user can have multiple "roles"

            if (user.getUserId().equals(mRequest.getCreatedBy())) {
                mRequestDetailsViewMvc.bindCreatedByUser(user);
            }

            if (user.getUserId().equals(mRequest.getPickedUpBy())) {
                mRequestDetailsViewMvc.bindPickedUpByUser(user);
            }

            if (user.getUserId().equals(mRequest.getClosedBy())) {
                mRequestDetailsViewMvc.bindClosedByUser(user);
            }
        }
    }
}
