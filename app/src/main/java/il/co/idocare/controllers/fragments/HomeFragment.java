package il.co.idocare.controllers.fragments;


import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.IDoCareJSONUtils;
import il.co.idocare.views.HomeViewMVC;
import il.co.idocare.widgets.RequestThumbnailRelativeLayout;


public class HomeFragment extends AbstractFragment {

    private final static String LOG_TAG = "HomeFragment";

    HomeListAdapter mListAdapter;
    HomeViewMVC mViewMVCHome;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mViewMVCHome = new HomeViewMVC(inflater, container);
        // Provide inbox Handler to the MVC View
        mViewMVCHome.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mViewMVCHome.getInboxHandler());

        // This is required for automatic refresh of action bar options upon fragment's loading
        setHasOptionsMenu(true);

        mListAdapter = new HomeListAdapter(getActivity(), 0);
        final ListView listPictures =
                (ListView) mViewMVCHome.getRootView().findViewById(R.id.list_requests_thumbnails);
        listPictures.setAdapter(mListAdapter);

        listPictures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Get the selected item
                RequestItem item = (RequestItem) listPictures.getItemAtPosition(position);
                // Create a bundle and put the id of the selected item there
                Bundle args = new Bundle();
                args.putLong(Constants.FieldName.REQUEST_ID.getValue(), item.getId());
                // Replace with RequestDetailsFragment and pass the bundle as argument
                replaceFragment(RequestDetailsFragment.class, true, args);
            }
        });

        return mViewMVCHome.getRootView();
    }

    @Override
    public void onResume() {
        super.onResume();


        // Since the call to getRequestsModel().getAllRequests() can block, we have to put it
        // in a separate thread
        Thread t = new Thread() {
            public void run() {
                final List<RequestItem> requests;
                try {
                    requests = getRequestsModel().getAllRequests();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                // Modifications of list adapter's contents should be done on UI thread
                HomeFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HomeFragment.this.mListAdapter.clear();
                        HomeFragment.this.mListAdapter.addAll(requests);
                    }
                });

            }
        };
        t.start();
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
    protected void handleMessage(Message msg) {
        // TODO: implement this method
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



    private class HomeListAdapter extends ArrayAdapter<RequestItem> {

        private final static String LOG_TAG = "HomeListAdapter";

        public HomeListAdapter(Context context, int resource) {
            super(context, resource);
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            RequestThumbnailRelativeLayout view;
            if (convertView == null) {
                view = new RequestThumbnailRelativeLayout(getContext());
            } else {
                view = (RequestThumbnailRelativeLayout) convertView;
            }

            RequestItem request = getItem(position);

            view.showRequestThumbnail(request);

            return view;
        }

    }

}
