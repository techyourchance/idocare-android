package il.co.idocare.controllers.fragments;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import il.co.idocare.Constants;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.views.RequestDetailsViewMVC;


public class RequestDetailsFragment extends AbstractFragment {

    private final static String LOG_TAG = "RequestDetailsFragment";

    RequestDetailsViewMVC mRequestDetailsViewMVC;

    RequestItem mRequestItem;
    boolean mIsClosed;
    boolean mIsPickedUp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRequestDetailsViewMVC = new RequestDetailsViewMVC(getActivity(), inflater, container);
        // Provide inbox Handler to the MVC View
        mRequestDetailsViewMVC.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());

        Bundle args = getArguments();
        if (args != null) {
            mRequestItem = getRequestsModel().
                    getRequest(args.getLong(Constants.FieldName.REQUEST_ID.getValue()));
        }

        if (mRequestItem == null) {
            // TODO: handle this error somehow
            return mRequestDetailsViewMVC.getRootView();
        }

        mRequestDetailsViewMVC.populateChildViewsFromRequestItem(mRequestItem);

        return mRequestDetailsViewMVC.getRootView();
    }

    @Override
    public boolean isTopLevelFragment() {
        return false;
    }

    @Override
    public Class<? extends AbstractFragment> getNavHierParentFragment() {
        return HomeFragment.class;
    }

    @Override
    protected void handleMessage(Message msg) {
        // TODO: complete this method
        switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {
            case V_PICKUP_REQUEST_BUTTON_CLICKED:
                pickupRequest();
                break;
            case V_CLOSE_REQUEST_BUTTON_CLICKED:
                closeRequest();
                break;
            default:
                Log.w(LOG_TAG, "Message of type "
                        + Constants.MESSAGE_TYPE_VALUES[msg.what].toString() + " wasn't consumed");
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void pickupRequest() {

        ServerRequest serverRequest = new ServerRequest(Constants.PICKUP_REQUEST_URL,
                Constants.ServerRequestTag.PICKUP_REQUEST, null);

        IDoCareHttpUtils.addStandardHeaders(getActivity(), serverRequest);
        serverRequest.addTextField(Constants.FieldName.REQUEST_ID.getValue(),
                String.valueOf(mRequestItem.getId()));

        serverRequest.execute();

    }

    private void closeRequest() {
        Bundle args = new Bundle();
        args.putLong(Constants.FieldName.REQUEST_ID.getValue(), mRequestItem.getId());
        replaceFragment(CloseRequestFragment.class, true, args);
    }


}
