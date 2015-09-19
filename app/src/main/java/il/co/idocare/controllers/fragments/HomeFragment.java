package il.co.idocare.controllers.fragments;


import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.interfaces.RequestsCombinedCursorAdapter;
import il.co.idocare.controllers.listadapters.HomeListAdapter;
import il.co.idocare.controllers.listadapters.UserActionsOnRequestApplierImpl;
import il.co.idocare.controllers.listadapters.UserActionsOnUserApplierImpl;
import il.co.idocare.datamodels.functional.RequestItem;
import il.co.idocare.datamodels.pojos.RequestItemPojo;
import il.co.idocare.views.HomeViewMVC;


public class HomeFragment extends AbstractFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = HomeFragment.class.getSimpleName();

    private final static int REQUESTS_LOADER_ID = 0;
    private final static int USERS_LOADER_ID = 1;
    private final static int USER_ACTIONS_LOADER_ID = 2;

    RequestsCombinedCursorAdapter mAdapter;
    HomeViewMVC mHomeViewMVC;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mHomeViewMVC = new HomeViewMVC(inflater, container);

        // This is required for automatic refresh of action bar options upon fragment's loading
        setHasOptionsMenu(true);

        setActionBarTitle(getTitle());

        // Create an adapter and pass the reference to MVC view
        mAdapter = new HomeListAdapter(getActivity(), null, 0,
                new UserActionsOnRequestApplierImpl(), new UserActionsOnUserApplierImpl());
        mHomeViewMVC.setListAdapter(mAdapter);

        // Initiate loaders
        getLoaderManager().initLoader(REQUESTS_LOADER_ID, null, this);
        getLoaderManager().initLoader(USERS_LOADER_ID, null, this);
        getLoaderManager().initLoader(USER_ACTIONS_LOADER_ID, null, this);

        return mHomeViewMVC.getRootView();
    }


    @Override
    public boolean isTopLevelFragment() {
        return true;
    }

    @Override
    public Class<? extends AbstractFragment> getNavHierParentFragment() {
        return null;
    }

    @Override
    public String getTitle() {
        return getResources().getString(R.string.home_fragment_title);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_home_items, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_new_request:
                replaceFragment(NewRequestFragment.class, true, false, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    public void onEvent(HomeViewMVC.ListItemClickEvent event) {
        // Get the selected request ID
        long requestId = mAdapter.getRequestAtPosition(event.mPosition).getId();

        // Create a bundle and put the id of the selected item there
        Bundle args = new Bundle();
        args.putLong(Constants.FIELD_NAME_REQUEST_ID, requestId);
        // Replace with RequestDetailsFragment and pass the bundle as argument
        replaceFragment(RequestDetailsFragment.class, true, false, args);
    }


    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------




    // ---------------------------------------------------------------------------------------------
    //
    // LoaderCallback methods

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        if (id == REQUESTS_LOADER_ID) {

            // The data needed for displaying the requests's thumbnail + the _id column which is
            // required by CursorAdapter framework
            String[] projection = new String[RequestItem.MANDATORY_REQUEST_FIELDS.length + 1];
            projection[0] = IDoCareContract.Requests._ID;
            System.arraycopy(RequestItem.MANDATORY_REQUEST_FIELDS, 0, projection, 1,
                    RequestItem.MANDATORY_REQUEST_FIELDS.length);


            // Change these values when adding filtering and sorting
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;

            //noinspection ConstantConditions
            return new CursorLoader(getActivity(),
                    IDoCareContract.Requests.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);

        }  else if (id == USERS_LOADER_ID) {

            String[] projection = IDoCareContract.Users.PROJECTION_ALL;

            // Change these values when adding filtering and sorting
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;

            //noinspection ConstantConditions
            return new CursorLoader(getActivity(),
                    IDoCareContract.Users.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);

        } else if (id == USER_ACTIONS_LOADER_ID) {

            String[] projection = IDoCareContract.UserActions.PROJECTION_ALL;

            // Change these values when adding filtering and sorting
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;

            //noinspection ConstantConditions
            return new CursorLoader(getActivity(),
                    IDoCareContract.UserActions.CONTENT_URI,
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
        if (loader.getId() == REQUESTS_LOADER_ID) {
            mAdapter.swapRequestsCursor(cursor);
        } else if(loader.getId() == USERS_LOADER_ID) {
            mAdapter.swapUsersCursor(cursor);
        } else if (loader.getId() == USER_ACTIONS_LOADER_ID) {
            mAdapter.swapUserActionsCursor(cursor);
        } else {
            Log.e(LOG_TAG, "onLoadFinished() called with unrecognized loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Releasing the resources
        if (loader.getId() == REQUESTS_LOADER_ID) {
            mAdapter.swapRequestsCursor(null);
        } else if(loader.getId() == USERS_LOADER_ID) {
            mAdapter.swapUsersCursor(null);
        } else if (loader.getId() == USER_ACTIONS_LOADER_ID) {
            mAdapter.swapUserActionsCursor(null);
        } else {
            Log.e(LOG_TAG, "onLoaderReset() called with unrecognized loader id: " + loader.getId());
        }

    }

    // End of LoaderCallback methods
    //
    // ---------------------------------------------------------------------------------------------




}
