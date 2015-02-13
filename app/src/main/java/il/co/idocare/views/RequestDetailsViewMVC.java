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
        mLayoutClosedRequest = (LinearLayout) mRootView.findViewById(R.id.layout_closed_request);
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


        boolean isClosed = requestItem.mCloseDate.length() > 0 &&
                !requestItem.mCloseDate.equals("null");
        boolean isPickedUp = requestItem.mPickedUpDate.length() > 0 &&
                !requestItem.mPickedUpDate.equals("null");


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

        if (requestItem.mOpenedBy != null) {
            TextView openedBy = (TextView) mRootView.findViewById(R.id.txt_opened_by);
            openedBy.setText("Opened by: " + requestItem.mOpenedBy);
        }

        if (requestItem.mCreationDate != null) {
            TextView creationDate = (TextView) mRootView.findViewById(R.id.txt_creation_date);
            creationDate.setText(requestItem.mCreationDate);
        }

        if (requestItem.mImagesBefore != null) {
            listAdapter.addAll(requestItem.mImagesBefore);
            listAdapter.notifyDataSetChanged();
        }

        if (requestItem.mNoteBefore != null) {
            TextView commentBefore = (TextView) mRootView.findViewById(R.id.txt_comment_before);
            commentBefore.setLines(UtilMethods.countLines(requestItem.mNoteBefore));
            commentBefore.setText(requestItem.mNoteBefore);
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
            mRootView.findViewById(R.id.layout_closed_request).setVisibility(View.GONE);
            return;
        }

        RequestPicturesAdapter listAdapter = new RequestPicturesAdapter(mContext, 0);
        ListView listPictures = (ListView) mRootView.findViewById(R.id.list_closed_request_pictures);
        listPictures.setAdapter(listAdapter);


        // Populate the views concerning "closure" details

        if (requestItem.mPickedUpBy != null) {
            // Assuming that the user who closed the request is the same one who picked it up
            TextView closedBy = (TextView) mRootView.findViewById(R.id.txt_closed_by);
            closedBy.setText("Closed by: " + requestItem.mPickedUpBy);
        }

        if (requestItem.mCloseDate != null) {
            TextView closeDate = (TextView) mRootView.findViewById(R.id.txt_close_date);
            closeDate.setText(requestItem.mCloseDate);
        }

        if (requestItem.mImagesAfter != null) {
            listAdapter.addAll(requestItem.mImagesAfter);
            listAdapter.notifyDataSetChanged();
        }

        if (requestItem.mNoteAfter != null) {
            TextView commentAfter = (TextView) mRootView.findViewById(R.id.txt_comment_after);
            commentAfter.setLines(UtilMethods.countLines(requestItem.mNoteAfter));
            commentAfter.setText(requestItem.mNoteAfter);
        }


    }

    /**
     * Handle the view of the pickup button
     * @param requestItem request item to take teh data from
     */
    private void populatePickupButtonFromRequestItem(RequestItem requestItem, boolean isPickedUp,
                                                     boolean isClosed) {

        String myUsername =
                mContext.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
                        .getString("username", "no_username");

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

        String myUsername =
                mContext.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
                        .getString("username", "no_username");

        Button btnCloseRequest = (Button) mRootView.findViewById(R.id.btn_close_request);

        if (isPickedUp && !isClosed) {
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
