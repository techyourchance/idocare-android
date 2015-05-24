package il.co.idocare.controllers.fragments;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.connectivity.ServerRequest;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.IDoCareJSONUtils;
import il.co.idocare.views.RequestDetailsViewMVC;


public class RequestDetailsFragment extends AbstractFragment implements ServerRequest.OnServerResponseCallback {

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

        // Exchange "listener" handlers between MVC views and MVC models
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
    public int getTitle() {
        return R.string.request_details_fragment_title;
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
            case V_CREATED_VOTE_UP_BUTTON_CLICKED:
                voteForRequest(1, false);
                break;
            case V_CREATED_VOTE_DOWN_BUTTON_CLICKED:
                voteForRequest(-1, false);
                break;
            case V_CLOSED_VOTE_UP_BUTTON_CLICKED:
                voteForRequest(1, true);
                break;
            case V_CLOSED_VOTE_DOWN_BUTTON_CLICKED:
                voteForRequest(-1, true);
                break;
            default:
                break;
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

        String activeAccountId = getActiveAccount().name;
        if (TextUtils.isEmpty(activeAccountId)) {
            Toast.makeText(getActivity(), "No active account found", Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG, "No active account found - request pickup failed");
            return;
        }
        showProgressDialog("Please wait...", "Assigning the request...");

        ServerRequest serverRequest = new ServerRequest(ServerRequest.PICKUP_REQUEST_URL,
                ServerRequest.ServerRequestTag.PICKUP_REQUEST, this);

        try {
            IDoCareHttpUtils.addStandardHeaders(serverRequest, activeAccountId, getAuthTokenForActiveAccount());
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverRequest.addTextField(Constants.FieldName.REQUEST_ID.getValue(),
                String.valueOf(mRequestId));

        serverRequest.execute();

    }

    private void closeRequest() {
        Bundle args = new Bundle();
        args.putLong(Constants.FieldName.REQUEST_ID.getValue(), mRequestId);
        replaceFragment(CloseRequestFragment.class, true, args);
    }

    private void voteForRequest(int amount, boolean voteForClosed) {

        String activeAccountId = getActiveAccount().name;
        if (TextUtils.isEmpty(activeAccountId)) {
            Toast.makeText(getActivity(), "No active account found", Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG, "No active account found - request pickup failed");
            return;
        }

        // Don't allow voting for yourself
        if ((voteForClosed && Long.valueOf(activeAccountId) == getRequestsModel().getRequest(getContentResolver(), mRequestId).getClosedBy()) ||
                (!voteForClosed && Long.valueOf(activeAccountId) == getRequestsModel().getRequest(getContentResolver(), mRequestId).getCreatedBy())) {
            Toast.makeText(getActivity(), getActivity().getResources()
                    .getString(R.string.self_voting_error_message), Toast.LENGTH_LONG).show();
            return;
        }

        ServerRequest serverRequest = new ServerRequest(ServerRequest.VOTE_REQUEST_URL,
                ServerRequest.ServerRequestTag.VOTE_FOR_REQUEST, this);

        try {
            IDoCareHttpUtils.addStandardHeaders(serverRequest, activeAccountId, getAuthTokenForActiveAccount());
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverRequest.addTextField(Constants.FieldName.ENTITY_ID.getValue(),
                String.valueOf(mRequestId));
        serverRequest.addTextField(Constants.FieldName.ENTITY_PARAM.getValue(),
                voteForClosed ? "closed" : "created");
        serverRequest.addTextField(Constants.FieldName.SCORE.getValue(),
                String.valueOf(amount));

        serverRequest.execute();
    }


    @Override
    public void serverResponse(boolean responseStatusOk, ServerRequest.ServerRequestTag tag,
                               String responseData) {

        if (tag == ServerRequest.ServerRequestTag.PICKUP_REQUEST) {
            if (responseStatusOk && IDoCareJSONUtils.verifySuccessfulStatus(responseData)) {
                dismissProgressDialog();
                // TODO: update local model with the change
                Toast.makeText(getActivity(), "This request was assigned to you", Toast.LENGTH_SHORT).show();
            }
        }
        else if (tag == ServerRequest.ServerRequestTag.VOTE_FOR_REQUEST) {
            if (responseStatusOk && IDoCareJSONUtils.verifySuccessfulStatus(responseData)) {
                // TODO: update local model with the change
            }

        }
        else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }
}
