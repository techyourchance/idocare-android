package il.co.idocare.controllers.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.interfaces.RequestUserActionApplier;
import il.co.idocare.controllers.listadapters.UserActionsOnRequestApplierImpl;
import il.co.idocare.datamodels.functional.RequestItem;
import il.co.idocare.datamodels.functional.UserActionItem;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.managers.VoteManager;
import il.co.idocare.mvcviews.requestdetails.RequestDetailsViewMvc;
import il.co.idocare.mvcviews.requestdetails.RequestDetailsViewMvcImpl;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.pictures.ImageViewPictureLoader;


public class RequestDetailsFragment extends AbstractFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,RequestDetailsViewMvc.RequestDetailsViewMvcListener {

    private final static String LOG_TAG = RequestDetailsFragment.class.getSimpleName();

    private final static int REQUEST_LOADER = 0;
    private final static int USERS_LOADER = 1;
    private final static int USER_ACTIONS_LOADER = 2;

    private RequestDetailsViewMvc mRequestDetailsViewMvc;

    @Inject LoginStateManager mLoginStateManager;
    @Inject ServerSyncController mServerSyncController;
    @Inject ImageViewPictureLoader mImageViewPictureLoader;
    @Inject VoteManager mVoteManager;

    private long mRequestId;
    private RequestItem mRawRequestItem;
    private RequestItem mRequestItem;

    private Cursor mUsersCursor;
    private Cursor mUserActionsCursor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getControllerComponent().inject(this);

        mRequestDetailsViewMvc =
                new RequestDetailsViewMvcImpl(inflater, container, mImageViewPictureLoader);
        mRequestDetailsViewMvc.registerListener(this);


        setActionBarTitle(getTitle());

        Bundle args = getArguments();
        if (args == null) {
            Log.e(LOG_TAG, "RequestDetailsFragment was started with no arguments. Switching" +
                    "to HomeFragment.");
            replaceFragment(HomeFragment.class, false, true, null);
        } else {
            mRequestId = args.getLong(Constants.FIELD_NAME_REQUEST_ID);
        }

        // Initialize the MapView inside the MVC view
        ((MapView) mRequestDetailsViewMvc.getRootView().findViewById(R.id.map_preview))
                .onCreate(savedInstanceState);

        getLoaderManager().initLoader(REQUEST_LOADER, null, this);

        return mRequestDetailsViewMvc.getRootView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putLong(Constants.FIELD_NAME_REQUEST_ID, mRequestId);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mRequestId = savedInstanceState.getLong(Constants.FIELD_NAME_REQUEST_ID);
        }
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



    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateManager.UserLoggedOutEvent event) {
        getLoaderManager().restartLoader(REQUEST_LOADER, null, this);
    }


    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // Callbacks from MVC view(s)

    @Override
    public void onCloseRequestClicked() {
        closeRequest();
    }

    @Override
    public void onPickupRequestClicked() {
        pickupRequest();
    }

    @Override
    public void onClosedVoteUpClicked() {
        voteForRequest(VoteManager.VOTE_UP_CLOSED);
    }

    @Override
    public void onClosedVoteDownClicked() {
        voteForRequest(VoteManager.VOTE_DOWN_CLOSED);
    }

    @Override
    public void onCreatedVoteUpClicked() {
        voteForRequest(VoteManager.VOTE_UP_CREATED);
    }

    @Override
    public void onCreatedVoteDownClicked() {
        voteForRequest(VoteManager.VOTE_DOWN_CREATED);
    }

    // End of callbacks from MVC view(s)
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // User actions handling


    private void pickupRequest() {

        // TODO: request pickup should be allowed only when there is a network connection

        if (mRequestItem.getPickedUpBy() != 0) {
            Log.e(LOG_TAG, "tried to pickup an already picked up request");
            return;
        }

        final String pickedUpBy = mLoginStateManager.getActiveAccountUserId();

        // If no logged in user - ask him to log in and rerun this method in case he does
        if (TextUtils.isEmpty(pickedUpBy)) {
            askUserToLogIn(getResources().getString(R.string.msg_ask_to_log_in_before_pickup),
                    new Runnable() {
                        @Override
                        public void run() {
                            pickupRequest();
                        }
                    });
            return;
        }

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog("Please wait...", "Updating the request...");
            }

            @Override
            protected Void doInBackground(Void... voids) {

                ContentValues userActionCV = new ContentValues(5);
                userActionCV.put(IDoCareContract.UserActions.COL_TIMESTAMP, System.currentTimeMillis());
                userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_TYPE,
                        IDoCareContract.UserActions.ENTITY_TYPE_REQUEST);
                userActionCV.put(IDoCareContract.UserActions.COL_ENTITY_ID, mRequestId);
                userActionCV.put(IDoCareContract.UserActions.COL_ACTION_TYPE,
                        IDoCareContract.UserActions.ACTION_TYPE_PICKUP_REQUEST);
                userActionCV.put(IDoCareContract.UserActions.COL_ACTION_PARAM, pickedUpBy);

                Uri newUri = getActivity().getContentResolver().insert(
                        IDoCareContract.UserActions.CONTENT_URI,
                        userActionCV
                );

                if (newUri != null) {
                    ContentValues requestCV = new ContentValues(1);
                    requestCV.put(IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG, 1);
                    int updated = getActivity().getContentResolver().update(
                            ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI,
                                    mRequestId),
                            requestCV,
                            null,
                            null
                    );
                    if (updated != 1)
                        Log.e(LOG_TAG, "failed to set 'LOCALLY_MODIFIED' flag on request entry" +
                                "after a vote");
                }

                // Request pickup is time critical action - need to be uploaded to the server ASAP
                mServerSyncController.requestImmediateSync();

                return (Void) null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dismissProgressDialog();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void closeRequest() {

        if (mRequestItem == null) {
            Log.e(LOG_TAG, "closeRequest() was called, but there is no data about the request");
            return;
        }

        Bundle args = new Bundle();
        args.putLong(Constants.FIELD_NAME_REQUEST_ID, mRequestId);
        args.putDouble(Constants.FIELD_NAME_LATITUDE, mRequestItem.getLatitude());
        args.putDouble(Constants.FIELD_NAME_LONGITUDE, mRequestItem.getLongitude());
        replaceFragment(CloseRequestFragment.class, true, false, args);
    }


    private void voteForRequest(final int voteType) {

        String activeUserId = mLoginStateManager.getActiveAccountUserId();

        // If no logged in user - ask him to log in
        if (TextUtils.isEmpty(activeUserId)) {
            askUserToLogIn(
                    getResources().getString(R.string.msg_ask_to_log_in_before_vote),
                    new Runnable() {
                        @Override
                        public void run() {
                            voteForRequest(voteType);
                        }
                    });
            return;
        }

        mVoteManager.voteForRequest(mRequestId, voteType);

    }

    // End of user actions handling
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // LoaderCallback methods

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        if (id == REQUEST_LOADER) {

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

        } else if (id == USERS_LOADER) {

            if (mRequestItem == null) {
                Log.e(LOG_TAG, "can't initialize users CursorLoader without request data!");
                return null;
            }

            String[] projection = IDoCareContract.Users.PROJECTION_ALL;

            StringBuilder placeHolders = new StringBuilder(10);
            ArrayList<String> selectionArgsList = new ArrayList<>(3);
            if (mRequestItem.getCreatedBy() != 0) {
                placeHolders.append("?");
                selectionArgsList.add(String.valueOf(mRequestItem.getCreatedBy()));
            }
            if (mRequestItem.getPickedUpBy() != 0) {
                placeHolders.append(", ?");
                selectionArgsList.add(String.valueOf(mRequestItem.getPickedUpBy()));
            }
            if (mRequestItem.getClosedBy() != 0) {
                placeHolders.append(", ?");
                selectionArgsList.add(String.valueOf(mRequestItem.getClosedBy()));
            }

            // Change these values when adding filtering and sorting
            String selection = IDoCareContract.Users.COL_USER_ID +
                    " IN (" + placeHolders.toString() + ")";

            String[] selectionArgs = selectionArgsList.toArray(new String[selectionArgsList.size()]);

            String sortOrder = null;

            //noinspection ConstantConditions
            return new CursorLoader(getActivity(),
                    IDoCareContract.Users.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);

        } else if (id == USER_ACTIONS_LOADER) {

            if (mRequestItem == null) {
                Log.e(LOG_TAG, "can't initialize user actions CursorLoader without request data!");
                return null;
            }

            String[] projection = IDoCareContract.UserActions.PROJECTION_ALL;

            // Change these values when adding filtering and sorting
            String selection = IDoCareContract.UserActions.COL_ENTITY_TYPE + " = ? AND " +
                    IDoCareContract.UserActions.COL_ENTITY_ID + " = ?";

            String[] selectionArgs = new String[] {IDoCareContract.UserActions.ENTITY_TYPE_REQUEST,
                    String.valueOf(mRequestId)};

            String sortOrder = null;

            //noinspection ConstantConditions
            return new CursorLoader(getActivity(),
                    IDoCareContract.UserActions.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);
        }
        else {
            Log.e(LOG_TAG, "onCreateLoader() called with unrecognized id: " + id);
            return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == REQUEST_LOADER) {

            if (cursor != null && cursor.moveToFirst()) {

                mRawRequestItem = RequestItem.create(cursor);

                if (mRawRequestItem != null) {

                    refreshRequest();

                    // Once got request's data - init user actions loader
                    if (getLoaderManager().getLoader(USER_ACTIONS_LOADER) == null)
                        getLoaderManager().initLoader(USER_ACTIONS_LOADER, null, this);
                }

            } else {
                // If the returned cursor is empty, this might indicate that the ID of the request
                // changed (due to uploading to the server) - if it is the case, we need to restart
                // the loader
                if (cursor != null) {
                    Cursor idMappingCursor = null;
                    idMappingCursor = getActivity().getContentResolver().query(
                            ContentUris.withAppendedId(IDoCareContract.TempIdMappings.CONTENT_URI,
                                    mRequestId),
                            IDoCareContract.TempIdMappings.PROJECTION_ALL,
                            null,
                            null,
                            null
                    );
                    if (idMappingCursor != null && idMappingCursor.moveToFirst()) {
                        mRequestId = idMappingCursor.getLong(idMappingCursor.getColumnIndexOrThrow(
                                IDoCareContract.TempIdMappings.COL_PERMANENT_ID));

                        getLoaderManager().restartLoader(REQUEST_LOADER, null, this);
                    }
                    if (idMappingCursor != null) idMappingCursor.close();
                }
            }

        } else if (loader.getId() == USERS_LOADER) {

            mUsersCursor = cursor;
            refreshUsers();

        } else if (loader.getId() == USER_ACTIONS_LOADER) {

            mUserActionsCursor = cursor;

            refreshRequest();

            // Once got users actions and applied them to the request - restart users loader
            // TODO: this is wasteful operation - users loaders should be restarted only if data for new user should be fetched!
            getLoaderManager().restartLoader(USERS_LOADER, null, this);

        } else {
            Log.e(LOG_TAG, "onLoadFinished() called with unrecognized loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == REQUEST_LOADER) {
            // TODO: should we do s.t. here? Maybe mRequestDetailsViewMvc.bindRequestItem(null)?
        } else if (loader.getId() == USERS_LOADER) {
            // TODO: should do anything here?
        } else if (loader.getId() == USER_ACTIONS_LOADER) {
            // TODO: should do anything here?
        } else {
            Log.e(LOG_TAG, "onLoaderReset() called with unrecognized loader id: " + loader.getId());
        }

    }


    // End of LoaderCallback methods
    //
    // ---------------------------------------------------------------------------------------------


    private void refreshRequest() {

        RequestItem combinedRequestItem = RequestItem.create(mRawRequestItem);

        if (mUserActionsCursor != null && mUserActionsCursor.moveToFirst()) {

            RequestUserActionApplier requestUserActionApplier = new UserActionsOnRequestApplierImpl();
            do {
                UserActionItem userAction = UserActionItem.create(mUserActionsCursor);
                combinedRequestItem = requestUserActionApplier.applyUserAction(combinedRequestItem,
                        userAction);
            } while (mUserActionsCursor.moveToNext());
        }

        mRequestItem = combinedRequestItem;

        mRequestItem.setStatus(mLoginStateManager.getActiveAccountUserId());

        mRequestDetailsViewMvc.bindRequestItem(mRequestItem);

        refreshUsers();

    }


    private void refreshUsers() {


        if (mUsersCursor != null && mUsersCursor.moveToFirst()) {
            do {
                UserItem user = UserItem.create(mUsersCursor);

                boolean used = false;

                if (user.getId() == mRequestItem.getCreatedBy()) {
                    mRequestDetailsViewMvc.bindCreatedByUser(user);
                    used = true;
                }
                if (user.getId() == mRequestItem.getPickedUpBy()) {
                    mRequestDetailsViewMvc.bindPickedUpByUser(user);
                    used = true;
                }
                if (user.getId() == mRequestItem.getClosedBy()) {
                    mRequestDetailsViewMvc.bindClosedByUser(user);
                    used = true;
                }

                if (!used)
                    Log.e(LOG_TAG, "user's data returned in the mUsersCursor does not correspond to" +
                            "either of creating, picking up or closing user IDs in the request.");


            } while (mUsersCursor.moveToNext());
        }
    }

}
