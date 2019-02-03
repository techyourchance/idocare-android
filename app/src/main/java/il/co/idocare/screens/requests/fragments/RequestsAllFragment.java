package il.co.idocare.screens.requests.fragments;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import il.co.idocare.R;
import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.requests.events.RequestsChangedEvent;
import il.co.idocarecore.utils.eventbusregistrator.EventBusRegistrable;
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
