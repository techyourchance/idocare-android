package il.co.idocare.screens.requests.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.techyourchance.fragmenthelper.FragmentHelper;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.screens.requestdetails.fragments.NewRequestFragment;
import il.co.idocare.screens.requestdetails.fragments.RequestDetailsFragment;
import il.co.idocare.dialogs.DialogsFactory;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.dialogs.events.PromptDialogDismissedEvent;
import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.requests.RequestsManager;
import il.co.idocare.screens.common.fragments.BaseScreenFragment;
import il.co.idocare.screens.requests.mvcviews.RequestsListViewMvcImpl;
import il.co.idocarecore.users.UsersManager;
import il.co.idocarecore.utils.Logger;

public abstract class RequestsListBaseFragment extends BaseScreenFragment implements
        RequestsListViewMvcImpl.RequestsListViewMvcListener,
        RequestsManager.RequestsManagerListener {

    private static final String USER_LOGIN_DIALOG_TAG = "USER_LOGIN_DIALOG_TAG";

    @Inject LoginStateManager mLoginStateManager;
    @Inject RequestsManager mRequestsManager;
    @Inject DialogsManager mDialogsManager;
    @Inject DialogsFactory mDialogsFactory;
    @Inject UsersManager mUsersManager;
    @Inject Logger mLogger;
    @Inject FragmentHelper mFragmentHelper;

    private RequestsListViewMvcImpl mViewMvc;

    /**
     * This method will be called whenever requests need to be loaded. Call one of the methods
     * of RequestsManager in order to fetch appropriate requests.
     */
    protected abstract void fetchRequests();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        getControllerComponent().inject(this);
    }

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
        RequestDetailsFragment fragment = RequestDetailsFragment.newInstance(request.getId());
        mFragmentHelper.replaceFragment(fragment);
    }

    @Override
    public void onCreateNewRequestClicked() {
        if (mLoginStateManager.isLoggedIn()) {
            mFragmentHelper.replaceFragment(new NewRequestFragment());
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
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestsFetched(List<RequestEntity> requests) {
        mViewMvc.bindRequests(requests);
    }


}
