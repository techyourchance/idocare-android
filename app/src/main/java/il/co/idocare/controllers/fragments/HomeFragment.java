package il.co.idocare.controllers.fragments;


import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import org.json.JSONArray;
import org.json.JSONException;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.IDoCareJSONUtils;
import il.co.idocare.views.HomeViewMVC;
import il.co.idocare.widgets.RequestThumbnailLayout;


public class HomeFragment extends AbstractFragment implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "HomeFragment";

    RequestsListAdapter mListAdapter;
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

        mListAdapter = new RequestsListAdapter(getActivity(), 0);
        final ListView listPictures =
                (ListView) mViewMVCHome.getRootView().findViewById(R.id.list_requests_thumbnails);
        listPictures.setAdapter(mListAdapter);

        listPictures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Get the selected item
                RequestItem item = (RequestItem) listPictures.getItemAtPosition(position);
                // Create a bundle and put the selected item there
                Bundle args = new Bundle();
                args.putParcelable("requestItem", item);
                // Replace with RequestDetailsFragment and pass the bundle as argument
                replaceFragment(RequestDetailsFragment.class, true, args);
            }
        });


        getRequestsFromServer();

        return mViewMVCHome.getRootView();
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

    /**
     * Create a new server request asking to fetch all requests and set its credentials
     */
    private void getRequestsFromServer() {
        ServerRequest serverRequest = new ServerRequest(Constants.GET_ALL_REQUESTS_URL,
                Constants.ServerRequestTag.GET_ALL_REQUESTS, this);

        IDoCareHttpUtils.addStandardHeaders(getActivity(), serverRequest);

        serverRequest.execute();
    }

    @Override
    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.GET_ALL_REQUESTS) {
            if (responseStatusOk && HomeFragment.this.isAdded() &&
                    IDoCareJSONUtils.verifySuccessfulStatus(responseData)) {

                JSONArray requestsArray;
                try {
                    requestsArray = IDoCareJSONUtils.extractDataJSONArray(responseData);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "error in parsing of the response data");
                    e.printStackTrace();
                    return;
                }

                RequestItem requestItem;

                for (int i=0; i<requestsArray.length(); i++) {

                    try {
                        // Try to parse element at position i as JSON object and create RequestItem
                        requestItem = IDoCareJSONUtils
                                .extractRequestItemFromJSONObject(requestsArray.getJSONObject(i));
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Exception when parsing JSON object at position: "
                                + String.valueOf(i) + ". The contents of JSON string:\n"
                                + requestsArray.optString(i));
                        e.printStackTrace();
                        continue;
                    }

                    // Add the created RequestItem everything was fine
                    if (requestItem != null) mListAdapter.add(requestItem);

                }
                mListAdapter.notifyDataSetChanged();
            }
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }


    private class RequestsListAdapter extends ArrayAdapter<RequestItem> {

        private final static String LOG_TAG = "NewPicturesAdapter";

        public RequestsListAdapter(Context context, int resource) {
            super(context, resource);
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            RequestThumbnailLayout view;
            if (convertView == null) {
                view = new RequestThumbnailLayout(getContext());
            } else {
                view = (RequestThumbnailLayout) convertView;
            }

            RequestItem request = getItem(position);

            view.showRequestThumbnail(request);

            return view;
        }

    }

}
