package il.co.idocare.screens.requests.fragments;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import il.co.idocare.R;

public class RequestsAllFragment extends RequestsListBaseFragment {

    private static final String TAG = "RequestsAllFragment";


    @Override
    protected void fetchRequests() {
        mRequestsManager.fetchAllRequests();
    }

    @Override
    public String getTitle() {
        return getString(R.string.home_fragment_title);
    }

    @Nullable
    @Override
    public Class<? extends Fragment> getHierarchicalParentFragment() {
        return null;
    }
}
