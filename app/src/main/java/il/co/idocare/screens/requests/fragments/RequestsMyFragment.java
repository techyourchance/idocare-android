package il.co.idocare.screens.requests.fragments;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import il.co.idocare.R;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrable;

@EventBusRegistrable
public class RequestsMyFragment extends RequestsListBaseFragment {

    @Override
    protected void fetchRequests() {
        mRequestsManager.fetchRequestsAssignedToUser(mLoginStateManager.getActiveAccountUserId());
    }

    @Nullable
    @Override
    public Class<? extends Fragment> getHierarchicalParentFragment() {
        return null;
    }

    @Override
    public String getTitle() {
        return getString(R.string.requests_my_fragment_title);
    }
}
