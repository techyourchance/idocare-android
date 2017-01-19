package il.co.idocare.screens.navigationdrawer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.dialogs.DialogsFactory;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.dialogs.events.PromptDialogDismissedEvent;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.screens.common.FrameHelper;
import il.co.idocare.screens.common.MainFrameContainer;
import il.co.idocare.screens.common.fragments.BaseFragment;
import il.co.idocare.screens.navigationdrawer.NavigationDrawerController;
import il.co.idocare.screens.navigationdrawer.mvcviews.NavigationDrawerViewMvc;
import il.co.idocare.screens.navigationdrawer.mvcviews.NavigationDrawerViewMvcImpl;
import il.co.idocare.screens.requests.fragments.RequestsAllFragment;
import il.co.idocare.screens.requests.fragments.RequestsMyFragment;
import il.co.idocare.users.UserEntity;
import il.co.idocare.users.UsersDataMonitoringManager;
import il.co.idocare.utils.Logger;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrable;

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
    @Inject NavigationDrawerController mNavigationDrawerController;
    @Inject UsersDataMonitoringManager mUsersDataMonitoringManager;
    @Inject EventBus mEventBus;
    @Inject Logger mLogger;


    private NavigationDrawerViewMvcImpl mViewMvc;

    private FrameHelper mMainFrameHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        getControllerComponent().inject(this);

        mMainFrameHelper = ((MainFrameContainer)activity).getFrameHelper();
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
        String activeUserId = mLoginStateManager.getActiveAccountUserId();
        if (activeUserId != null && !activeUserId.isEmpty()) {
            mUsersDataMonitoringManager.fetchUserByIdAndNotifyIfExists(activeUserId);
        }
    }

    @Override
    public void onUserDataChange(UserEntity user) {
        if (user.getUserId().equals(mLoginStateManager.getActiveAccountUserId())) {
            mViewMvc.bindUserData(user);
        }
    }

    @Override
    public void onRequestsListClicked() {
        mMainFrameHelper.replaceFragment(RequestsAllFragment.class, false, true, null);
        closeNavDrawer();
    }

    @Override
    public void onMyRequestsClicked() {
        mMainFrameHelper.replaceFragment(RequestsMyFragment.class, true, false, null);
        closeNavDrawer();
    }

    @Override
    public void onNewRequestClicked() {
        if (mLoginStateManager.isLoggedIn()) {
            mMainFrameHelper.replaceFragment(NewRequestFragment.class, true, false, null);
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
        if (event.getTag().equals(USER_LOGIN_DIALOG_TAG)) {
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
        mNavigationDrawerController.closeDrawer();
    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginSucceededEvent event) {
        fetchDataOfActiveUser();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateManager.UserLoggedOutEvent event) {
        fetchDataOfActiveUser();
    }


    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



}
