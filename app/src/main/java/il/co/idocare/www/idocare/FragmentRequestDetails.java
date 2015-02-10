package il.co.idocare.www.idocare;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FragmentRequestDetails extends IDoCareFragment {

    private final static String LOG_TAG = "FragmentRequestDetails";

    RequestItem mRequestItem;
    boolean mIsClosed;
    boolean mIsPickedUp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_details, container, false);

        Bundle args = getArguments();
        if (args != null) {
            mRequestItem = (RequestItem) args.getParcelable("requestItem");
        }

        if (mRequestItem == null) {
            // TODO: handle this error somehow
            return view;
        }

        mIsClosed = mRequestItem.mCloseDate.length() > 0 &&
                !mRequestItem.mCloseDate.equals("null");
        mIsPickedUp = mRequestItem.mPickedUpDate.length() > 0 &&
                !mRequestItem.mPickedUpDate.equals("null");


        Log.v(LOG_TAG, "Request details:"
                + "\nCreated by " + mRequestItem.mOpenedBy + " at " + mRequestItem.mCreationDate
                + "\nPicked up by " + mRequestItem.mPickedUpBy + " at " + mRequestItem.mPickedUpDate
                + "\nClosed by " + mRequestItem.mPickedUpBy + " at " + mRequestItem.mCloseDate);


        populateChildViewsFromRequestItem(view);

        return view;
    }

    @Override
    public boolean isTopLevelFragment() {
        return false;
    }

    @Override
    public Class<? extends IDoCareFragment> getNavHierParentFragment() {
        return FragmentHome.class;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /**
     * Decide which of the views in this fragment should be visible and populate them with
     * data from the RequestItem object.
     * @param view root view of the fragment
     */
    private void populateChildViewsFromRequestItem(View view) {

        // Handle the views related to initial request
        populateRequestViewsFromRequestItem(view);
        // Handle the views related to pickup info
        populateClosedViewsFromRequestItem(view);
        // Handle the button functionality
        populatePickupCloseButtonFromRequestItem(view);

    }

    /**
     * Handle the views describing the initial request
     * @param view root view of the fragment
     */
    private void populateRequestViewsFromRequestItem(View view) {


        RequestPicturesAdapter listAdapter = new RequestPicturesAdapter(getActivity(), 0);
        ListView listPictures = (ListView) view.findViewById(R.id.list_new_request_pictures);
        listPictures.setAdapter(listAdapter);

        if (mRequestItem.mOpenedBy != null) {
            TextView openedBy = (TextView) view.findViewById(R.id.txt_opened_by);
            openedBy.setText("Opened by: " + mRequestItem.mOpenedBy);
        }

        if (mRequestItem.mCreationDate != null) {
            TextView creationDate = (TextView) view.findViewById(R.id.txt_creation_date);
            creationDate.setText(mRequestItem.mCreationDate);
        }

        if (mRequestItem.mImagesBefore != null) {
            listAdapter.addAll(mRequestItem.mImagesBefore);
            listAdapter.notifyDataSetChanged();
        }

        if (mRequestItem.mNoteBefore != null) {
            TextView commentBefore = (TextView) view.findViewById(R.id.txt_comment_before);
            commentBefore.setLines(UtilMethods.countLines(mRequestItem.mNoteBefore));
            commentBefore.setText(mRequestItem.mNoteBefore);
        }

    }

    /**
     * Handle the views describing the info about request's closure
     * @param view root view of the fragment
     */
    private void populateClosedViewsFromRequestItem(View view) {


        if (!mIsClosed) {
            // If the request is not closed then hide the "closed" layout altogether
            view.findViewById(R.id.layout_closed_request).setVisibility(View.GONE);
            return;
        }

        RequestPicturesAdapter listAdapter = new RequestPicturesAdapter(getActivity(), 0);
        ListView listPictures = (ListView) view.findViewById(R.id.list_closed_request_pictures);
        listPictures.setAdapter(listAdapter);


        // Populate the views concerning "closure" details

        if (mRequestItem.mPickedUpBy != null) {
            // Assuming that the user who closed the request is the same one who picked it up
            TextView closedBy = (TextView) view.findViewById(R.id.txt_closed_by);
            closedBy.setText("Closed by: " + mRequestItem.mPickedUpBy);
        }

        if (mRequestItem.mCloseDate != null) {
            TextView closeDate = (TextView) view.findViewById(R.id.txt_close_date);
            closeDate.setText(mRequestItem.mCloseDate);
        }

        if (mRequestItem.mImagesAfter != null) {
            listAdapter.addAll(mRequestItem.mImagesAfter);
            listAdapter.notifyDataSetChanged();
        }

        if (mRequestItem.mNoteAfter != null) {
            TextView commentAfter = (TextView) view.findViewById(R.id.txt_comment_after);
            commentAfter.setLines(UtilMethods.countLines(mRequestItem.mNoteAfter));
            commentAfter.setText(mRequestItem.mNoteAfter);
        }


    }

    /**
     * Handle the view of the button
     * @param view root view of the fragment
     */
    private void populatePickupCloseButtonFromRequestItem(View view) {

        String myUsername =
                getActivity().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
                .getString("username", "no_username");

        Button button = (Button) view.findViewById(R.id.btn_pickup_or_close_request);

        if (mIsClosed) {
            // No need for button if the request is closed
            button.setVisibility(View.GONE);
        }
        else if (mIsPickedUp) {
            if (mRequestItem.mPickedUpBy.equals(myUsername)) {
                // The request was picked up by the current user - he can close it
                button.setText(getActivity().getResources().getString(R.string.btn_close_request));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeRequest();
                    }
                });
            } else {
                // The request was picked up by another user - hide the "close" button
                button.setVisibility(View.GONE);
            }
        }
        else {
            // The request is open and the current user can pick it up
            button.setText(getActivity().getResources().getString(R.string.btn_pickup_request));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pickupRequest();
                }
            });
        }

    }

    private void pickupRequest() {

        ServerRequest serverRequest = new ServerRequest(Constants.PICKUP_REQUEST_URL,
                Constants.ServerRequestTag.PICKUP_REQUEST, null);

        // TODO: field names should come from constants and the values should not be hardcoded
        SharedPreferences prefs =
                getActivity().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        serverRequest.addTextField("username", prefs.getString("username", "no_username"));
        serverRequest.addTextField("password", prefs.getString("password", "no_password"));
        serverRequest.addTextField("requestId", mRequestItem.mId);
        serverRequest.addTextField("pickedUpBy", prefs.getString("username", "no_username"));

        serverRequest.execute();

    }

    private void closeRequest() {
        Bundle args = new Bundle();
        args.putBoolean("isCloseRequestType", true);
        args.putString("requestId", mRequestItem.mId);
        replaceFragment(FragmentNewAndCloseRequest.class, true, args);
    }

    private static class ViewHolder {
        ImageView imageView;
    }

    private class RequestPicturesAdapter extends ArrayAdapter<String> {

        private LayoutInflater mInflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        public RequestPicturesAdapter(Context context, int resource) {
            super(context, resource);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.element_camera_picture, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ImageLoader.getInstance().displayImage(getItem(position), holder.imageView, animateFirstListener);

            return view;
        }

        /**
         * Get all the items of this adapter
         * @return array of items or null if there are none
         */
        public String[] getItems() {
            if (getCount() == 0) {
                return null;
            }
            String[] items = new String[getCount()];
            for (int i = 0; i < getCount(); i++) {
                items[i] = getItem(i);
            }
            return items;
        }
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new ArrayList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }


}
