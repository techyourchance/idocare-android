package il.co.idocare.screens.requests.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.fragments.AbstractFragment;
import il.co.idocare.requests.RequestsManager;


public class RequestsMyFragment extends AbstractFragment {

    private final static String LOG_TAG = "RequestsMyFragment";

    @Inject LoginStateManager mLoginStateManager;
    @Inject RequestsManager mRequestsManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // This is required for automatic refresh of action bar options upon fragment's loading
        setHasOptionsMenu(true);

        setActionBarTitle(getTitle());

        return null;
    }


    @Override
    public boolean isTopLevelFragment() {
        return false;
    }

    @Override
    public Class<? extends AbstractFragment> getNavHierParentFragment() {
        return RequestsAllFragment.class;
    }

    @Override
    public String getTitle() {
        return getResources().getString(R.string.requests_my_fragment_title);
    }



}
