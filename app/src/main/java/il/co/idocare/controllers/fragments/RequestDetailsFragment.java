package il.co.idocare.controllers.fragments;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.connectivity.ServerHttpRequest;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.views.RequestDetailsViewMVC;


public class RequestDetailsFragment extends AbstractFragment implements
        ServerHttpRequest.OnServerResponseCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = RequestDetailsFragment.class.getSimpleName();

    private final static int SINGLE_REQUEST_LOADER = 0;

    private RequestDetailsViewMVC mRequestDetailsViewMVC;

    private long mRequestId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRequestDetailsViewMVC =
                new RequestDetailsViewMVC(getActivity(), container, savedInstanceState);

        initialize();

        setActionBarTitle(getTitle());

        return mRequestDetailsViewMVC.getRootView();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Provide inbox Handler to the MVC View
        mRequestDetailsViewMVC.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());

    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove "listener" handlers between this MVC controller and MVC views
        mRequestDetailsViewMVC.removeOutboxHandler(getInboxHandler());
        removeOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());

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
    public String getTitle() {
        return getResources().getString(R.string.request_details_fragment_title);
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

    private void initialize() {
        // Get the ID of the request
        Bundle args = getArguments();
        if (args == null) {
            // TODO: handle this error somehow (maybe pop back stack?)
            return;
        }

        mRequestId = args.getLong(Constants.FIELD_NAME_REQUEST_ID);
        getLoaderManager().initLoader(SINGLE_REQUEST_LOADER, null, this);

    }


    private void pickupRequest() {
//
//        String activeAccountId = getActiveAccount().name;
//        if (TextUtils.isEmpty(activeAccountId)) {
//            Toast.makeText(getActivity(), "No active account found", Toast.LENGTH_LONG).show();
//            Log.i(LOG_TAG, "No active account found - request pickup failed");
//            return;
//        }
//        showProgressDialog("Please wait...", "Assigning the request...");
//
//        ServerRequest serverRequest = new ServerRequest(ServerRequest.PICKUP_REQUEST_URL,
//                ServerRequest.ServerRequestTag.PICKUP_REQUEST, this);
//
//        try {
//            IDoCareHttpUtils.addStandardHeaders(serverRequest, activeAccountId, getAuthTokenForActiveAccount());
//        } catch (AuthenticatorException e) {
//            e.printStackTrace();
//        } catch (OperationCanceledException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        serverRequest.addTextField(Constants.FIELD_NAME_REQUEST_ID,
//                String.valueOf(mRequestId));
//
//        serverRequest.execute();

    }

    private void closeRequest() {
        Bundle args = new Bundle();
        args.putLong(Constants.FIELD_NAME_REQUEST_ID, mRequestId);
        replaceFragment(CloseRequestFragment.class, true, args);
    }

    private void voteForRequest(int amount, boolean voteForClosed) {
//
//        String activeAccountId = getActiveAccount().name;
//        if (TextUtils.isEmpty(activeAccountId)) {
//            Toast.makeText(getActivity(), "No active account found", Toast.LENGTH_LONG).show();
//            Log.i(LOG_TAG, "No active account found - request pickup failed");
//            return;
//        }
//
//        // TODO: rewrite this logic without Models...
////        // Don't allow voting for yourself
////        if ((voteForClosed && Long.valueOf(activeAccountId) == getRequestsModel().getRequest(getContentResolver(), mRequestId).getClosedBy()) ||
////                (!voteForClosed && Long.valueOf(activeAccountId) == getRequestsModel().getRequest(getContentResolver(), mRequestId).getCreatedBy())) {
////            Toast.makeText(getActivity(), getActivity().getResources()
////                    .getString(R.string.self_voting_error_message), Toast.LENGTH_LONG).show();
////            return;
////        }
//
//        ServerRequest serverRequest = new ServerRequest(ServerRequest.VOTE_REQUEST_URL,
//                ServerRequest.ServerRequestTag.VOTE_FOR_REQUEST, this);
//
//        try {
//            IDoCareHttpUtils.addStandardHeaders(serverRequest, activeAccountId, getAuthTokenForActiveAccount());
//        } catch (AuthenticatorException e) {
//            e.printStackTrace();
//        } catch (OperationCanceledException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        serverRequest.addTextField(Constants.FIELD_NAME_ENTITY_ID,
//                String.valueOf(mRequestId));
//        serverRequest.addTextField(Constants.FIELD_NAME_ENTITY_PARAM,
//                voteForClosed ? "closed" : "created");
//        serverRequest.addTextField(Constants.FIELD_NAME_SCORE,
//                String.valueOf(amount));
//
//        serverRequest.execute();
    }



    // ---------------------------------------------------------------------------------------------
    //
    // LoaderCallback methods

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        if (id == SINGLE_REQUEST_LOADER) {


            String[] projection = IDoCareContract.Requests.PROJECTION_ALL;

            // Change these values when adding filtering and sorting
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;

            //noinspection ConstantConditions
            return new CursorLoader(getActivity(),
                    ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI, mRequestId),
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);
        } else {
            Log.e(LOG_TAG, "onCreateLoader() called with unrecognized id: " + id);
            return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == SINGLE_REQUEST_LOADER) {
            try {
                if (cursor.moveToFirst()) {
                    RequestItem request = RequestItem.createRequestItem(cursor);
                    mRequestDetailsViewMVC.showRequest(request);
                } else {
                    // TODO: think of how to handle this error - the returned cursor is empty!
                }
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "Couldn't create RequestItem from the Cursor provided to onLoadFinished()");
                e.printStackTrace();
            }
        } else {
            Log.e(LOG_TAG, "onLoadFinished() called with unrecognized loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == SINGLE_REQUEST_LOADER) {
            // TODO: should we do s.t. here? Maybe mRequestDetailsViewMVC.showRequest(null)?
        } else {
            Log.e(LOG_TAG, "onLoaderReset() called with unrecognized loader id: " + loader.getId());
        }

    }


    // End of LoaderCallback methods
    //
    // ---------------------------------------------------------------------------------------------


//
//    @Override
//    public void serverResponse(UserActionItem userAction,
//                               int statusCode, String reasonPhrase, String entityString) {
//
//        if (userAction == ServerRequest.ServerRequestTag.PICKUP_REQUEST) {
//            if (responseStatusOk && IDoCareJSONUtils.verifySuccessfulStatus(entityString)) {
//                dismissProgressDialog();
//                // TODO: update local model with the change
//                Toast.makeText(getActivity(), "This request was assigned to you", Toast.LENGTH_SHORT).show();
//            }
//        }
//        else if (userAction == ServerRequest.ServerRequestTag.VOTE_FOR_REQUEST) {
//            if (responseStatusOk && IDoCareJSONUtils.verifySuccessfulStatus(entityString)) {
//                // TODO: update local model with the change
//            }
//
//        }
//        else {
//            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + userAction.toString());
//        }
//    }

    @Override
    public void serverResponse(int statusCode, String reasonPhrase, String entityString, Object asyncCompletionToken) {

    }
}
