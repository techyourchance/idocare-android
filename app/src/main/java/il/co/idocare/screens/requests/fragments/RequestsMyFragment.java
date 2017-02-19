package il.co.idocare.screens.requests.fragments;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.Subscribe;

import il.co.idocare.R;
import il.co.idocare.authentication.LoggedInUserEntity;
import il.co.idocare.requests.events.RequestsChangedEvent;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrable;

@EventBusRegistrable
public class RequestsMyFragment extends RequestsListBaseFragment {

    private static final String TAG = "RequestsMyFragment";

    @Override
    protected void fetchRequests() {
        mLogger.d(TAG, "fetchRequests()");

        LoggedInUserEntity user = mLoginStateManager.getLoggedInUser();
        if (user == null) {
            mLogger.e(TAG, "no logged in user");
            return;
        }
        mRequestsManager.fetchRequestsAssignedToUserAndNotify(user.getUserId());
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

    @Subscribe
    public void onRequestsChanged(RequestsChangedEvent event) {
        fetchRequests();
    }

}
