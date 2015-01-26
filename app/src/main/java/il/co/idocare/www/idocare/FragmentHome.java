package il.co.idocare.www.idocare;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FragmentHome extends Fragment implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "FragmentHome";
    private final static String JSON_TAG_REQUESTS = "data";

   private RequestsListAdapter mListAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // This is required for automatic refresh of action bar options upon fragment's loading
        setHasOptionsMenu(true);

        mListAdapter = new RequestsListAdapter(getActivity(), 0);
        ListView listPictures = (ListView) view.findViewById(R.id.list_requests_thumbnails);
        listPictures.setAdapter(mListAdapter);

        // Fetch the requests from the server
        getRequestsFromServer();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_home_items, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_new_request:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack(null);
                ft.replace(R.id.frame_contents, new FragmentNewRequest());
                ft.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getRequestsFromServer() {
        ServerRequest serverRequest = new ServerRequest(Constants.GET_ALL_REQUESTS_URL,
                Constants.ServerRequestTag.GET_ALL_REQUESTS, this);
        serverRequest.addTextField("username", Constants.USERNAME);
        serverRequest.addTextField("password", Constants.PASSWORD);
        serverRequest.execute();
    }

    @Override
    public void serverResponse(Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.GET_ALL_REQUESTS) {
            mListAdapter.addAll(extractRequestsFromJSON(responseData));
            mListAdapter.notifyDataSetChanged();
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }

    private List<RequestItem> extractRequestsFromJSON(String jsonData) {
        ArrayList<RequestItem> requestItemsList = new ArrayList<RequestItem>();
        // TODO: continue

        if (jsonData != null && jsonData.length() > 0) {
            try {


                JSONObject jsonObj = new JSONObject(jsonData);

                // Getting JSON Array
                JSONArray requestsArray = jsonObj.getJSONArray(JSON_TAG_REQUESTS);

                if (requestsArray != null && requestsArray.length() > 0) {
                    RequestItem requestItem;
                    JSONObject request;

                    for (int i = 0; i < requestsArray.length(); i++) {

                        request = requestsArray.getJSONObject(i);
                        requestItem = new RequestItem();
                        // TODO: all the "key" strings must be hardcoded into Constants class!
                        requestItem.mCreationDate = request.getString("creation_date");
                        requestItem.mOpenedBy = request.getString("opened_by");

                        if (request.getString("images_before").length() > 0) {
                            // Remove the escape characters in front of / and split the list
                            requestItem.mPicturesUrls = (request.getString("images_before")).replaceAll("\\\\/", "/").split(", ");

                            for (String url : requestItem.mPicturesUrls) {
                                Log.d(LOG_TAG, "Found URL: " + url);
                            }
                        }

                        requestItemsList.add(requestItem);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(LOG_TAG,  "jsonData is null or empty");
        }

        return requestItemsList;
    }

    private static class RequestItem {
        String mCreationDate;
        String mOpenedBy;
        String[] mPicturesUrls;
    }

    private static class ViewHolder {
        TextView mTxtTitle;
        TextView mTxtRating;
        ImageView mImageView;
    }

    private class RequestsListAdapter extends ArrayAdapter<RequestItem> {

        private final static String LOG_TAG = "NewRequestPicturesAdapter";

        private LayoutInflater mInflater;

        public RequestsListAdapter(Context context, int resource) {
            super(context, resource);
            mInflater = LayoutInflater.from(context);
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.element_request_thumbnail, parent, false);
                holder = new ViewHolder();
                holder.mImageView = (ImageView) view.findViewById(R.id.image_request_thumbnail);
                holder.mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
                holder.mTxtRating = (TextView) view.findViewById(R.id.txt_rating);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            RequestItem request = getItem(position);

            holder.mTxtTitle.setText(request.mOpenedBy + "@" + request.mCreationDate);

            if (request.mPicturesUrls != null && request.mPicturesUrls.length > 0) {
                ImageLoader.getInstance().displayImage(request.mPicturesUrls[0], holder.mImageView);
            }

            return view;
        }

    }
}
