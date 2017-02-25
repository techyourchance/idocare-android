package il.co.idocare.screens.requests.fragments;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import il.co.idocare.R;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.requests.events.RequestsChangedEvent;
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


    /*
    This is an ugly workaround for the first time we fetch the requests from the server
    TODO: come up with a proper solution
     */
    @Override
    public void onRequestsFetched(List<RequestEntity> requests) {
        if (requests.size() > 0) {
            super.onRequestsFetched(requests);
        }
    }
}
