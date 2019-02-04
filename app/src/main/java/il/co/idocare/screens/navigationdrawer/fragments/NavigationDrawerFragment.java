package il.co.idocare.screens.navigationdrawer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import il.co.idocare.R;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.screens.common.fragments.BaseFragment;
import il.co.idocare.screens.navigationdrawer.NavigationDrawerManager;
import il.co.idocare.screens.navigationdrawer.mvcviews.NavigationDrawerViewMvc;
import il.co.idocare.screens.navigationdrawer.mvcviews.NavigationDrawerViewMvcImpl;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.authentication.events.UserLoggedOutEvent;
import il.co.idocarecore.eventbusevents.LoginStateEvents;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarecore.screens.common.dialogs.DialogsFactory;
import il.co.idocarecore.screens.common.dialogs.DialogsManager;
import il.co.idocarecore.screens.common.dialogs.PromptDialogDismissedEvent;
import il.co.idocarecore.serversync.ServerSyncController;
import il.co.idocarecore.users.UserEntity;
import il.co.idocarecore.users.UsersDataMonitoringManager;
import il.co.idocarecore.utils.Logger;
import il.co.idocarecore.utils.eventbusregistrator.EventBusRegistrable;

/**
 * This fragment will be shown in navigation drawer
 */
@EventBusRegistrable
public class NavigationDrawerFragment extends BaseFragment implements
        NavigationDrawerViewMvc.NavigationDrawerViewMvcListener,
        UsersDataMonitoringManager.UsersDataMonitorListener {

    private static final String TAG = "NavigationDrawerFragment";

    private static final String USER_LOGIN_DIALOG_TAG = "USER_LOGIN_DIALOG_TAG";

    @Inject LoginStateManager mLoginStateManager;
    @Inject ServerSyncController mServerSyncController;
    @Inject DialogsManager mDialogsManager;
    @Inject DialogsFactory mDialogsFactory;
    @Inject NavigationDrawerManager mNavigationDrawerManager;
    @Inject UsersDataMonitoringManager mUsersDataMonitoringManager;
    @Inject EventBus mEventBus;
    @Inject Logger mLogger;
    @Inject ScreensNavigator mScreensNavigator;


    private NavigationDrawerViewMvcImpl mViewMvc;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        getControllerComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewMvc = new NavigationDrawerViewMvcImpl(inflater, container);
        mViewMvc.registerListener(this);

        return mViewMvc.getRootView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mUsersDataMonitoringManager.registerListener(this);
        fetchDataOfActiveUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        mUsersDataMonitoringManager.unregisterListener(this);
    }

    private void fetchDataOfActiveUser() {
        String activeUserId = mLoginStateManager.getLoggedInUser().getUserId();
        if (activeUserId != null && !activeUserId.isEmpty()) {
            // notify the view that there is a logged in user
            mViewMvc.bindUserData(UserEntity.newBuilder().setUserId(activeUserId).build());
            // fetch user's info
            mUsersDataMonitoringManager.fetchUserByIdAndNotifyIfExists(activeUserId);
        } else {
            // no active user
            mViewMvc.bindUserData(null);
        }
    }

    @Override
    public void onUserDataChange(UserEntity user) {
        if (user.getUserId().equals(mLoginStateManager.getLoggedInUser().getUserId())) {
            mViewMvc.bindUserData(user);
        }
    }

    @Override
    public void onRequestsListClicked() {
        mScreensNavigator.toAllRequests();
        closeNavDrawer();
    }

    @Override
    public void onMyRequestsClicked() {
        mScreensNavigator.toMyRequests();
        closeNavDrawer();
    }

    @Override
    public void onNewRequestClicked() {
        if (mLoginStateManager.isLoggedIn()) {
            mScreensNavigator.toNewRequest();
            closeNavDrawer();
        } else {
            mDialogsManager.showRetainedDialogWithTag(
                    mDialogsFactory.newPromptDialog(
                            getString(R.string.dialog_title_login_required),
                            getString(R.string.msg_ask_to_log_in_before_new_request),
                            getResources().getString(R.string.btn_dialog_positive),
                            getResources().getString(R.string.btn_dialog_negative)),
                    USER_LOGIN_DIALOG_TAG);
        }
    }

    @Subscribe
    public void onPromptDialogDismissed(PromptDialogDismissedEvent event) {
        if (event.getDialogTag().equals(USER_LOGIN_DIALOG_TAG)) {
            if (event.getClickedButtonIndex() == PromptDialogDismissedEvent.BUTTON_POSITIVE) {
                initiateLoginFlow();
                closeNavDrawer();
            }
        }
    }

    @Override
    public void onLogInClicked() {
        initiateLoginFlow();
        closeNavDrawer();
    }

    @Override
    public void onLogOutClicked() {
        initiateLogoutFlow();
        closeNavDrawer();
    }

    @Override
    public void onShowMapClicked() {
        // currently no op
        closeNavDrawer();
    }

    private void initiateLogoutFlow() {
        mLoginStateManager.logOut();
    }

    private void initiateLoginFlow() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }


    private void closeNavDrawer() {
        mNavigationDrawerManager.closeDrawer();
    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginSucceededEvent event) {
        fetchDataOfActiveUser();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserLoggedOutEvent event) {
        fetchDataOfActiveUser();
    }


    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



}
