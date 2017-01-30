package il.co.idocare.screens.requests.fragments;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.Subscribe;

import il.co.idocare.R;
import il.co.idocare.requests.RequestsChangedEvent;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrable;
@EventBusRegistrable
public class RequestsAllFragment extends RequestsListBaseFragment {

    private static final String TAG = "RequestsAllFragment";


    @Override
    protected void fetchRequests() {
        mRequestsManager.fetchAllRequestsAndNotify();
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


    @Subscribe
    public void onRequestsChanged(RequestsChangedEvent event) {
        fetchRequests();
    }
}
