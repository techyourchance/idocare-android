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

import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.IDoCareJSONUtils;
import il.co.idocare.views.HomeViewMVC;


public class FragmentHome extends AbstractFragment implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "FragmentHome";

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
                // Replace with FragmentRequestDetails and pass the bundle as argument
                replaceFragment(FragmentRequestDetails.class, true, args);
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
                replaceFragment(FragmentNewRequest.class, true, null);
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
            if (responseStatusOk && FragmentHome.this.isAdded() &&
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
            holder.mTxtTitle.setText(request.mCreatedBy.mNickname + " @ " + request.mCreatedAt);

            if (request.mCreatedPictures != null && request.mCreatedPictures.length > 0) {

                ImageLoader.getInstance().displayImage(
                        request.mCreatedPictures[0],
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
