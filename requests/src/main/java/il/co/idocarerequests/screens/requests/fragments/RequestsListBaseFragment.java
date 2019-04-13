package il.co.idocarerequests.screens.requests.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.techyourchance.fragmenthelper.FragmentHelper;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import androidx.fragment.app.Fragment;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.requests.RequestsManager;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarecore.screens.common.dialogs.DialogsFactory;
import il.co.idocarecore.screens.common.dialogs.DialogsManager;
import il.co.idocarecore.screens.common.dialogs.PromptDialogDismissedEvent;
import il.co.idocarecore.users.UsersManager;
import il.co.idocarecore.utils.Logger;
import il.co.idocarerequests.R;
import il.co.idocarerequests.screens.requests.mvcviews.RequestsListViewMvcImpl;

public abstract class RequestsListBaseFragment extends Fragment implements
        RequestsListViewMvcImpl.RequestsListViewMvcListener,
        RequestsManager.RequestsManagerListener {

    private static final String USER_LOGIN_DIALOG_TAG = "USER_LOGIN_DIALOG_TAG";

    protected final ScreensNavigator mScreensNavigator;
    protected final LoginStateManager mLoginStateManager;
    protected final RequestsManager mRequestsManager;
    protected final DialogsManager mDialogsManager;
    protected final DialogsFactory mDialogsFactory;
    protected final UsersManager mUsersManager;
    protected final Logger mLogger;
    protected final FragmentHelper mFragmentHelper;

    private RequestsListViewMvcImpl mViewMvc;

    protected RequestsListBaseFragment(ScreensNavigator screensNavigator, LoginStateManager loginStateManager,
                                       RequestsManager requestsManager,
                                       DialogsManager dialogsManager,
                                       DialogsFactory dialogsFactory,
                                       UsersManager usersManager,
                                       Logger logger,
                                       FragmentHelper fragmentHelper) {
        mScreensNavigator = screensNavigator;
        mLoginStateManager = loginStateManager;
        mRequestsManager = requestsManager;
        mDialogsManager = dialogsManager;
        mDialogsFactory = dialogsFactory;
        mUsersManager = usersManager;
        mLogger = logger;
        mFragmentHelper = fragmentHelper;
    }

    /**
     * This method will be called whenever requests need to be loaded. Call one of the methods
     * of RequestsManager in order to fetch appropriate requests.
     */
    protected abstract void fetchRequests();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: find approach that doesn't involve passing users manager directly!
        mViewMvc = new RequestsListViewMvcImpl(inflater, container, mUsersManager);
        mViewMvc.registerListener(this);

        return mViewMvc.getRootView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mRequestsManager.registerListener(this);
        mRequestsManager.syncRequestsFromServer();
        fetchRequests();

    }

    @Override
    public void onStop() {
        super.onStop();
        mRequestsManager.unregisterListener(this);
    }

    @Override
    public void onRequestClicked(RequestEntity request) {
        mScreensNavigator.toRequestDetails(request.getId());
    }

    @Override
    public void onCreateNewRequestClicked() {
        if (mLoginStateManager.isLoggedIn()) {
            mScreensNavigator.toNewRequest();
        } else {
            mDialogsManager.showRetainedDialogWithTag(
                    mDialogsFactory.newPromptDialog(
                            null,
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
            }
        }
    }

    private void initiateLoginFlow() {
        mScreensNavigator.toLogin();
    }

    @Override
    public void onRequestsFetched(List<RequestEntity> requests) {
        mViewMvc.bindRequests(requests);
    }


}
