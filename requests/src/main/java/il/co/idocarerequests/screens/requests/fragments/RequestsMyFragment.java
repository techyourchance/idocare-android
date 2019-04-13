package il.co.idocarerequests.screens.requests.fragments;


import com.techyourchance.fragmenthelper.FragmentHelper;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import il.co.idocarecore.authentication.LoggedInUserEntity;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.requests.RequestsManager;
import il.co.idocarecore.requests.events.RequestsChangedEvent;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarecore.screens.common.dialogs.DialogsFactory;
import il.co.idocarecore.screens.common.dialogs.DialogsManager;
import il.co.idocarecore.users.UsersManager;
import il.co.idocarecore.utils.Logger;
import il.co.idocarecore.utils.eventbusregistrator.EventBusRegistrable;

@EventBusRegistrable
public class RequestsMyFragment extends RequestsListBaseFragment {

    private static final String TAG = "RequestsMyFragment";

    @Inject
    public RequestsMyFragment(ScreensNavigator screensNavigator, LoginStateManager loginStateManager, RequestsManager requestsManager, DialogsManager dialogsManager, DialogsFactory dialogsFactory, UsersManager usersManager, Logger logger, FragmentHelper fragmentHelper) {
        super(screensNavigator, loginStateManager, requestsManager, dialogsManager, dialogsFactory, usersManager, logger, fragmentHelper);
    }

    @Override
    protected void fetchRequests() {
        mLogger.d(TAG, "fetchRequests()");

        LoggedInUserEntity user = mLoginStateManager.getLoggedInUser();
        if (user == null) {
            mLogger.e(TAG, "no logged in user");
            return;
        }
        mRequestsManager.fetchRequestsAssignedToUserAndNotify(user.getUserId());
    }

    @Subscribe
    public void onRequestsChanged(RequestsChangedEvent event) {
        fetchRequests();
    }

}
