package il.co.idocare.www.idocare;


import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import java.util.List;


public class FragmentHome extends IDoCareFragment implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "FragmentHome";

   private RequestsListAdapter mListAdapter;


    @Override
    public boolean isTopLevelFragment() {
        return true;
    }

    @Override
    public Class<? extends IDoCareFragment> getNavHierParentFragment() {
        return null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // This is required for automatic refresh of action bar options upon fragment's loading
        setHasOptionsMenu(true);

        mListAdapter = new RequestsListAdapter(getActivity(), 0);
        final ListView listPictures = (ListView) view.findViewById(R.id.list_requests_thumbnails);
        listPictures.setAdapter(mListAdapter);



        listPictures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Get the selected item
                RequestItem item = (RequestItem) listPictures.getItemAtPosition(position);
                // Create a bundle and put the selected item there
                Bundle args = new Bundle();
                args.putParcelable("requestItem", item);
                // Replace with FragmentRequestDetails and pass the bundle as argument
                replaceFragment(FragmentRequestDetails.class, true, args);
            }
        });



        // Fetch the requests from the server
        IDoCareApplication app = (IDoCareApplication) getActivity().getApplication();
        if (app.getRequests() == null) {
            getRequestsFromServer();
        } else {
            mListAdapter.addAll(app.getRequests());
            mListAdapter.notifyDataSetChanged();
        }

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
                ft.replace(R.id.frame_contents, new FragmentNewAndCloseRequest());
                ft.commit();
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

        SharedPreferences prefs =
                getActivity().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        serverRequest.addTextField("username", prefs.getString("username", "no_username"));
        serverRequest.addTextField("password", prefs.getString("password", "no_password"));
        serverRequest.execute();
    }

    @Override
    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.GET_ALL_REQUESTS) {
            if (responseStatusOk && FragmentHome.this.isAdded()) {
                List<RequestItem> requests = UtilMethods.extractRequestsFromJSON(responseData);

                mListAdapter.addAll(requests);
                mListAdapter.notifyDataSetChanged();

                // TODO: remove this workaround
                IDoCareApplication app = (IDoCareApplication) getActivity().getApplication();
                app.setRequests(requests);
            }
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }


    private static class ViewHolder {
        TextView mTxtTitle;
        TextView mTxtRating;
        TextView mTxtBeforeImage;
        ImageView mImageView;
    }

    private class RequestsListAdapter extends ArrayAdapter<RequestItem> {

        private final static String LOG_TAG = "NewPicturesAdapter";

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
                holder.mTxtBeforeImage = (TextView) view.findViewById(R.id.text_request_thumbnail);
                holder.mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
                holder.mTxtRating = (TextView) view.findViewById(R.id.txt_rating);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            RequestItem request = getItem(position);

            // Set title
            holder.mTxtTitle.setText(request.mOpenedBy + "  @ " + request.mCreationDate);

            if (request.mImagesBefore != null && request.mImagesBefore.length > 0) {

                ImageLoader.getInstance().displayImage(
                        request.mImagesBefore[0],
                        holder.mImageView,
                        Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS,
                        new RequestThumbnailLoadingListener(holder.mTxtBeforeImage,
                                getResources().getString(R.string.text_request_thumbnail_loading),
                                getResources().getString(R.string.text_request_thumbnail_failed),
                                getResources().getString(R.string.text_request_thumbnail_cancelled)),
                        new RequestThumbnailLoadingProgressListener(holder.mTxtBeforeImage,
                                getResources().getString(R.string.text_request_thumbnail_loading)));

            } else {
                holder.mImageView.setVisibility(View.GONE);
                holder.mTxtBeforeImage.setVisibility(View.VISIBLE);
                holder.mTxtBeforeImage.setText(getResources().getString(R.string.text_request_thumbnail_no_picture));
            }
            return view;
        }

    }

    /**
     * ImageLoadingListener used for alternating between TextView and ImageView when
     * request thumbnail is being loaded
     */
    private static class RequestThumbnailLoadingListener implements ImageLoadingListener {

        TextView mTxtBeforeImage;
        String mLoading;
        String mFailed;
        String mCancelled;

        public RequestThumbnailLoadingListener(TextView txtBeforeImage, String loading,
                                               String failed, String cancelled) {
            mTxtBeforeImage = txtBeforeImage;
            mLoading = loading;
            mFailed = failed;
            mCancelled = cancelled;
        }


        @Override
        public void onLoadingStarted(String imageUri, View view) {
            // Make TextView visible and set loading text
            mTxtBeforeImage.setVisibility(View.VISIBLE);
            mTxtBeforeImage.setText(mLoading);
            // Hide the ImageView
            view.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            mTxtBeforeImage.setText(mFailed);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            // Hide TextView
            mTxtBeforeImage.setVisibility(View.GONE);
            // Show ImageView
            view.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            mTxtBeforeImage.setText(mCancelled);
        }
    }

    /**
     * ImageLoadingProgressListener used to animate request thumbnail while the picture hasn't
     * been loaded yet.
     */
    private static class RequestThumbnailLoadingProgressListener implements ImageLoadingProgressListener {

        TextView mTxtBeforeImage;
        String mLoadingText;
        int mNumOfDots;

        public RequestThumbnailLoadingProgressListener(TextView txtBeforeImage, String text) {
            mTxtBeforeImage = txtBeforeImage;
            mLoadingText = text;
            mNumOfDots = 0;
        }

        @Override
        public void onProgressUpdate(String imageUri, View view, int current, int total) {
            // Add the required amount of dots to the end of loading string
            String dot = ".";
            String textWithDots = mLoadingText + new String(new char[mNumOfDots % 4]).replace("\0", dot);
            // Set the new text and increase the "dot counter"
            mTxtBeforeImage.setText(textWithDots);
            mNumOfDots++;


        }
    }
}
