package il.co.idocare.controllers.fragments;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import il.co.idocare.Constants;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.views.RequestDetailsViewMVC;


public class RequestDetailsFragment extends AbstractFragment {

    private final static String LOG_TAG = "RequestDetailsFragment";

    private RequestDetailsViewMVC mRequestDetailsViewMVC;

    private long mRequestId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRequestDetailsViewMVC =
                new RequestDetailsViewMVC(getActivity(), container, savedInstanceState,
                        getRequestsModel(), getUsersModel());

        obtainRequestItemAndShowItsDetails();

        return mRequestDetailsViewMVC.getRootView();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Provide inbox Handler to the MVC View
        mRequestDetailsViewMVC.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());

        // Notify the MVC view about models' changes
        getUsersModel().addOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());
        getRequestsModel().addOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove "listener" handlers between this MVC controller and MVC views
        mRequestDetailsViewMVC.removeOutboxHandler(getInboxHandler());
        removeOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());

        // Remove "listener" handlers between MVC views and MVC Models
        getUsersModel().removeOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());
        getRequestsModel().removeOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());
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

    private void obtainRequestItemAndShowItsDetails() {
        // Get the ID of the request
        Bundle args = getArguments();
        if (args == null) {
            // TODO: handle this error somehow (maybe pop back stack?)
            return;
        }

        mRequestId = args.getLong(Constants.FieldName.REQUEST_ID.getValue());
        mRequestDetailsViewMVC.showRequest(mRequestId);

    }


    private void pickupRequest() {

        ServerRequest serverRequest = new ServerRequest(Constants.PICKUP_REQUEST_URL,
                Constants.ServerRequestTag.PICKUP_REQUEST, null);

        IDoCareHttpUtils.addStandardHeaders(getActivity(), serverRequest);
        serverRequest.addTextField(Constants.FieldName.REQUEST_ID.getValue(),
                String.valueOf(mRequestId));

        serverRequest.execute();

    }

    private void closeRequest() {
        Bundle args = new Bundle();
        args.putLong(Constants.FieldName.REQUEST_ID.getValue(), mRequestId);
        replaceFragment(CloseRequestFragment.class, true, args);
    }


}
