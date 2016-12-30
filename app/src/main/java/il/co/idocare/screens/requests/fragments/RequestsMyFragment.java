package il.co.idocare.screens.requests.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.fragments.AbstractFragment;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.requests.RequestsManager;
import il.co.idocare.screens.common.fragments.BaseFragment;
import il.co.idocare.screens.requests.mvcviews.RequestsMyViewMvcImpl;


public class RequestsMyFragment extends BaseFragment implements RequestsMyViewMvcImpl.RequestsMyViewMvcListener {

    private final static String TAG = "RequestsMyFragment";

    @Inject LoginStateManager mLoginStateManager;
    @Inject RequestsManager mRequestsManager;

    private RequestsMyViewMvcImpl mViewMvc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewMvc = new RequestsMyViewMvcImpl(inflater, container);
        mViewMvc.registerListener(this);

        return mViewMvc.getRootView();
    }


    @Override
    public void onRequestClicked(RequestEntity request) {
        // TODO
    }

    @Override
    public void onCreateNewRequestClicked() {
        // TODO
    }
}
