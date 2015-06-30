package il.co.idocare.controllers.fragments;


import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.listadapters.HomeFragmentListAdapter;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.views.HomeViewMVC;


public class HomeFragment extends AbstractFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final static String LOG_TAG = HomeFragment.class.getSimpleName();

    private final static int LOADER_ID = 0;

    HomeFragmentListAdapter mAdapter;
    HomeViewMVC mHomeViewMVC;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mHomeViewMVC = new HomeViewMVC(inflater, container);

        // This is required for automatic refresh of action bar options upon fragment's loading
        setHasOptionsMenu(true);

        initializeThumbnailsList(); // TODO: move the adapter and the click listener to MVC view

        setActionBarTitle(getTitle());

        return mHomeViewMVC.getRootView();
    }

    private void initializeThumbnailsList() {

        mAdapter = new HomeFragmentListAdapter(getActivity(), null, 0);
        final ListView requestThumbnails =
                (ListView) mHomeViewMVC.getRootView().findViewById(R.id.list_requests_thumbnails);
        requestThumbnails.setAdapter(mAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        // TODO: remove this listener from here and put it in MVCView and add message for click
        requestThumbnails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Get the selected item
                Cursor cursor = (Cursor) mAdapter.getItem(position);
                long requestId = cursor.getLong(cursor.getColumnIndex(Constants.FIELD_NAME_REQUEST_ID));

                // Create a bundle and put the id of the selected item there
                Bundle args = new Bundle();
                args.putLong(Constants.FIELD_NAME_REQUEST_ID, requestId);
                // Replace with RequestDetailsFragment and pass the bundle as argument
                replaceFragment(RequestDetailsFragment.class, true, args);
            }
        });



    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Provide inbox Handler to the MVC View
        mHomeViewMVC.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mHomeViewMVC.getInboxHandler());
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove "listener" handlers between the MVC controller and MVC views
        mHomeViewMVC.removeOutboxHandler(getInboxHandler());
        removeOutboxHandler(mHomeViewMVC.getInboxHandler());

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
    protected void handleMessage(Message msg) {

        switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {

            default:
                break;
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_home_items, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_new_request:
                replaceFragment(NewRequestFragment.class, true, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    // ---------------------------------------------------------------------------------------------
    //
    // LoaderCallback methods

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        if (id == LOADER_ID) {

            // The data needed for displaying the requests's thumbnail + the _id column which is
            // required by CursorAdapter framework
            String[] projection = new String[RequestItem.MANDATORY_REQUEST_FIELDS.length + 1];
            projection[0] = IDoCareContract.Requests._ID;
            System.arraycopy(RequestItem.MANDATORY_REQUEST_FIELDS, 0, projection, 1, RequestItem.MANDATORY_REQUEST_FIELDS.length);


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
        } else {
            Log.e(LOG_TAG, "onCreateLoader() called with unrecognized id: " + id);
            return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == LOADER_ID) {
            mAdapter.swapCursor(cursor);
        } else {
            Log.e(LOG_TAG, "onLoadFinished() called with unrecognized loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_ID) {
            // Releasing the resources
            mAdapter.swapCursor(null);
        } else {
            Log.e(LOG_TAG, "onLoaderReset() called with unrecognized loader id: " + loader.getId());
        }

    }


    // End of LoaderCallback methods
    //
    // ---------------------------------------------------------------------------------------------



}
