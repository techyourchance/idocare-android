package il.co.idocarerequests.screens.requests.fragments;


import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.requests.RequestsManager;
import il.co.idocarecore.requests.events.RequestsChangedEvent;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarecore.screens.common.dialogs.DialogsFactory;
import il.co.idocarecore.screens.common.dialogs.DialogsManager;
import il.co.idocarecore.screens.common.fragmenthelper.FragmentHelper;
import il.co.idocarecore.users.UsersManager;
import il.co.idocarecore.utils.Logger;
import il.co.idocarecore.utils.eventbusregistrator.EventBusRegistrable;

@EventBusRegistrable
public class RequestsAllFragment extends RequestsListBaseFragment {

    private static final String TAG = "RequestsAllFragment";

    @Inject
    public RequestsAllFragment(ScreensNavigator screensNavigator, LoginStateManager loginStateManager, RequestsManager requestsManager, DialogsManager dialogsManager, DialogsFactory dialogsFactory, UsersManager usersManager, Logger logger, FragmentHelper fragmentHelper) {
        super(screensNavigator, loginStateManager, requestsManager, dialogsManager, dialogsFactory, usersManager, logger, fragmentHelper);
    }

    @Override
    protected void fetchRequests() {
        mRequestsManager.fetchAllRequestsAndNotify();
    }

    @Subscribe
    public void onRequestsChanged(RequestsChangedEvent event) {
        fetchRequests();
    }

    /*
    This is an ugly workaround for the first time we fetch the requests from the server
    TODO: come up with a proper solution
     */
    @Override
    public void onRequestsFetched(List<RequestEntity> requests) {
        super.onRequestsFetched(requests);
    }
}
