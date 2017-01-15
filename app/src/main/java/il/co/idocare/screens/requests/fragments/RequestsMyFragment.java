package il.co.idocare.screens.requests.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.controllers.fragments.RequestDetailsFragment;
import il.co.idocare.dialogs.DialogsFactory;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.dialogs.events.PromptDialogDismissedEvent;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.requests.RequestsManager;
import il.co.idocare.screens.common.FrameHelper;
import il.co.idocare.screens.common.MainFrameContainer;
import il.co.idocare.screens.common.fragments.BaseFragment;
import il.co.idocare.screens.requests.mvcviews.RequestsMyViewMvcImpl;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrable;

@EventBusRegistrable
public class RequestsMyFragment extends BaseFragment implements
        RequestsMyViewMvcImpl.RequestsMyViewMvcListener,
        RequestsManager.RequestsManagerListener {

    private final static String TAG = "RequestsMyFragment";

    private static final String USER_LOGIN_DIALOG_TAG = "USER_LOGIN_DIALOG_TAG";

    @Inject LoginStateManager mLoginStateManager;
    @Inject RequestsManager mRequestsManager;
    @Inject DialogsManager mDialogsManager;
    @Inject DialogsFactory mDialogsFactory;

    private RequestsMyViewMvcImpl mViewMvc;

    private FrameHelper mMainFrameHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        getControllerComponent().inject(this);

        mMainFrameHelper = ((MainFrameContainer)activity).getFrameHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewMvc = new RequestsMyViewMvcImpl(inflater, container);
        mViewMvc.registerListener(this);

        return mViewMvc.getRootView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mRequestsManager.registerListener(this);
        mRequestsManager.fetchRequestsAssignedToUser(mLoginStateManager.getActiveAccountUserId());
    }

    @Override
    public void onStop() {
        super.onStop();
        mRequestsManager.unregisterListener(this);
    }

    @Override
    public void onRequestClicked(RequestEntity request) {
        // Create a bundle and put the id of the selected item there
        Bundle args = new Bundle();
        args.putString(RequestDetailsFragment.ARG_REQUEST_ID, request.getId());
        // Replace with RequestDetailsFragment and pass the bundle as argument
        mMainFrameHelper.replaceFragment(RequestDetailsFragment.class, true, false, args);
    }

    @Override
    public void onCreateNewRequestClicked() {
        if (mLoginStateManager.isLoggedIn()) {
            mMainFrameHelper.replaceFragment(NewRequestFragment.class, true, false, null);
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
        if (event.getTag().equals(USER_LOGIN_DIALOG_TAG)) {
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
    public void onRequestsFetched(@NonNull List<RequestEntity> requests) {
        mViewMvc.bindRequests(requests);
    }
}
