package il.co.idocare.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.utils.UtilMethods;

/**
 * MVC View for New Request screen.
 */
public class RequestDetailsViewMVC extends AbstractViewMVC {

    private final static String LOG_TAG = "RequestDetailsViewMVC";


    View mRootView;
    Context mContext;

    LinearLayout mLayoutNewRequest;
    LinearLayout mLayoutClosedRequest;
    Button mBtnPickupRequest;
    Button mBtnCloseRequest;


    public RequestDetailsViewMVC(Context context, LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.fragment_request_details, container, false);
        mContext = context;

        mLayoutNewRequest = (LinearLayout) mRootView.findViewById(R.id.layout_new_request);
        mLayoutClosedRequest = (LinearLayout) mRootView.findViewById(R.id.layout_request_closed);
        mBtnPickupRequest = (Button) mRootView.findViewById(R.id.btn_pickup_request);
        mBtnCloseRequest = (Button) mRootView.findViewById(R.id.btn_close_request);

    }

    @Override
    protected void handleMessage(Message msg) {
        // TODO: complete this method
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        return null;
    }


    /**
     *
     * Decide which of the Android Views in this MVC View should be visible and populate them with
     * data from the RequestItem object.
     * @param requestItem request item to take teh data from
     */
    // TODO: these methods should be changed such that they use MVC Model
    public void populateChildViewsFromRequestItem(RequestItem requestItem) {


        boolean isClosed = requestItem.getClosedBy() != null;
        boolean isPickedUp = requestItem.getPickedUpBy() != null;


        // Handle the views related to initial request
        populateRequestViewsFromRequestItem(requestItem, isPickedUp, isClosed);
        // Handle the views related to pickup info
        populateClosedViewsFromRequestItem(requestItem, isPickedUp, isClosed);
        // Handle the pickup button functionality
        populatePickupButtonFromRequestItem(requestItem, isPickedUp, isClosed);
        // Handle the close button functionality
        populateCloseButtonFromRequestItem(requestItem, isPickedUp, isClosed);

    }

    /**
     * Handle the views describing the initial request
     * @param requestItem request item to take teh data from
     */
    private void populateRequestViewsFromRequestItem(RequestItem requestItem, boolean isPickedUp,
                                                     boolean isClosed) {

        RequestPicturesAdapter listAdapter = new RequestPicturesAdapter(mContext, 0);
        ListView listPictures = (ListView) mRootView.findViewById(R.id.list_new_request_pictures);
        listPictures.setAdapter(listAdapter);

        if (requestItem.getCreatedBy() != null) {
            TextView createdBy = (TextView) mRootView.findViewById(R.id.txt_created_by);
            createdBy.setText("Created by: " + requestItem.getCreatedBy().mNickname);
        }

        if (requestItem.getCreatedAt() != null) {
            TextView createdAt = (TextView) mRootView.findViewById(R.id.txt_created_at);
            createdAt.setText(requestItem.getCreatedAt());
        }

        if (requestItem.getCreatedPictures() != null) {
            listAdapter.addAll(requestItem.getCreatedPictures());
            listAdapter.notifyDataSetChanged();
        }

        if (requestItem.getCreatedComment() != null) {
            TextView commentBefore = (TextView) mRootView.findViewById(R.id.txt_created_comment);
            commentBefore.setLines(UtilMethods.countLines(requestItem.getCreatedComment()));
            commentBefore.setText(requestItem.getCreatedComment());
        }

    }

    /**
     * Handle the views describing the info about request's closure
     * @param requestItem request item to take teh data from
     */
    private void populateClosedViewsFromRequestItem(RequestItem requestItem, boolean isPickedUp,
                                                    boolean isClosed) {

        if (!isClosed) {
            // If the request is not closed then hide the "closed" layout altogether
            mRootView.findViewById(R.id.layout_request_closed).setVisibility(View.GONE);
            return;
        }

        RequestPicturesAdapter listAdapter = new RequestPicturesAdapter(mContext, 0);
        ListView listPictures = (ListView) mRootView.findViewById(R.id.list_request_closed_pictures);
        listPictures.setAdapter(listAdapter);


        // Populate the views concerning "closure" details

        if (requestItem.getClosedBy() != null) {
            TextView closedBy = (TextView) mRootView.findViewById(R.id.txt_closed_by);
            closedBy.setText("Closed by: " + requestItem.getClosedBy().mNickname);
        }

        if (requestItem.getClosedAt() != null) {
            TextView closedAt = (TextView) mRootView.findViewById(R.id.txt_closed_at);
            closedAt.setText(requestItem.getClosedAt());
        }

        if (requestItem.getClosedPictures() != null) {
            listAdapter.addAll(requestItem.getClosedPictures());
            listAdapter.notifyDataSetChanged();
        }

        if (requestItem.getClosedComment() != null) {
            TextView closedComment = (TextView) mRootView.findViewById(R.id.txt_closed_comment);
            closedComment.setLines(UtilMethods.countLines(requestItem.getClosedComment()));
            closedComment.setText(requestItem.getClosedComment());
        }


    }

    /**
     * Handle the view of the pickup button
     * @param requestItem request item to take teh data from
     */
    private void populatePickupButtonFromRequestItem(RequestItem requestItem, boolean isPickedUp,
                                                     boolean isClosed) {

        Button btnPickupRequest = (Button) mRootView.findViewById(R.id.btn_pickup_request);

        if (!isPickedUp && !isClosed) {
            btnPickupRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifyOutboxHandlers(Constants.MessageType.V_PICKUP_REQUEST_BUTTON_CLICKED.ordinal(),
                            0, 0, null);
                }
            });
        } else {
            btnPickupRequest.setVisibility(View.GONE);
        }

    }


    /**
     * Handle the view of the close button
     * @param requestItem request item to take teh data from
     */
    private void populateCloseButtonFromRequestItem(RequestItem requestItem, boolean isPickedUp,
                                                     boolean isClosed) {


        long myId = mContext.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
                .getLong(Constants.FieldName.USER_ID.getValue(), 0);

        boolean isPickedUpByMe = isPickedUp && requestItem.getPickedUpBy().getId() == myId;

        Button btnCloseRequest = (Button) mRootView.findViewById(R.id.btn_close_request);

        if (isPickedUpByMe && !isClosed) {
            btnCloseRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifyOutboxHandlers(Constants.MessageType.V_CLOSE_REQUEST_BUTTON_CLICKED.ordinal(),
                            0, 0, null);
                }
            });
        } else {
            btnCloseRequest.setVisibility(View.GONE);
        }

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
