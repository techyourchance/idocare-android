package il.co.idocare.views;

import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.handlermessaging.HandlerMessagingSlave;
import il.co.idocare.pojos.RequestItem;

/**
 * This is the top level View which should be used as a "thumbnail" for requests
 * when they are displayed in a list.
 */
public class RequestThumbnailViewMVC extends RelativeLayout implements
        ViewMVC,
        HandlerMessagingSlave {

    private static final String LOG_TAG = RequestThumbnailViewMVC.class.getSimpleName();

    private Context mContext;

    private Handler mInboxHandler;
    private RequestThumbnailContentObserver mContentObserver;

    private RequestItem mRequestItem;

    private TextView mTxtRequestStatus;
    private TextView mTxtRequestLocation;
    private ImageView mImgRequestThumbnail;
    private TextView mTxtCreatedComment;
    private TextView mTxtCreatedBy;
    private TextView mTxtCreatedAt;
    private TextView mTxtCreatedReputation;

    private boolean mIsClosed;
    private boolean mIsPickedUp;

    private String mCurrentPictureUrl;



    public RequestThumbnailViewMVC(Context context) {
        super(context);

        mContext = context;
        mCurrentPictureUrl = "";

        init();
    }



    /**
     * Initialize this MVC view. Must be called from constructor
     */
    private void init() {

        // Inflate the underlying layout
        LayoutInflater.from(mContext).inflate(R.layout.layout_request_thumbnail, this, true);

        // This padding is required in order not to hide the border when colorizing inner views
        int padding = (int) getResources().getDimension(R.dimen.border_background_width);
        getRootView().setPadding(padding, padding, padding, padding);

        // Set background color and border for the whole item
        getRootView().setBackgroundColor(getResources().getColor(android.R.color.white));
        getRootView().setBackgroundResource(R.drawable.border_background);


        mTxtRequestStatus = (TextView) findViewById(R.id.txt_request_status);
        mTxtRequestLocation = (TextView) findViewById(R.id.txt_request_fine_location);
        mImgRequestThumbnail = (ImageView) findViewById(R.id.img_request_thumbnail);
        mTxtCreatedComment = (TextView) findViewById(R.id.txt_created_comment);
        mTxtCreatedBy = (TextView) findViewById(R.id.txt_created_by);
        mTxtCreatedAt = (TextView) findViewById(R.id.txt_created_at);
        mTxtCreatedReputation = (TextView) findViewById(R.id.txt_votes);

        mIsClosed = false;
        mIsPickedUp = false;
    }


    @Override
    public Handler getInboxHandler() {

        // Since most of the work done in MVC Views consist of manipulations on underlying
        // Android Views, it will be convenient (and less error prone) if MVC View's inbox Handler
        // will be running on UI thread.
        if (mInboxHandler == null) {
            mInboxHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    RequestThumbnailViewMVC.this.handleMessage(msg);
                }
            };
        }
        return mInboxHandler;
    }


    @Override
    public Bundle getViewState() {
        return null;
    }


    private void handleMessage(Message msg) {
        // TODO: write request/user update logic
        switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {

            default:
                break;
        }
    }

    /**
     * Update this thumbnail with the details of a particular request
     * @param requestItem the request which should be displayed
     */
    public void showRequestThumbnail(RequestItem requestItem) {

        // Update ContentObserver
        if (mContentObserver != null && requestItem.getId() != mRequestItem.getId()) {
            mContext.getContentResolver().unregisterContentObserver(mContentObserver);
            mContentObserver = new RequestThumbnailContentObserver(new Handler());
            Uri uri = ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI, requestItem.getId());
            mContext.getContentResolver().registerContentObserver(uri, false, mContentObserver);
        }

        mRequestItem = requestItem;

        // Update the UI
        setStatus();
        setColors();
        setTexts();
        setPictures();

        // Update user's details
        updateUsers();
    }



    private void setStatus() {
        if (mRequestItem.getClosedBy() != 0) {
            mIsClosed = true;
        }
        else if (mRequestItem.getPickedUpBy() != 0) {
            mIsClosed = false;
            mIsPickedUp = true;
        }
        else {
            mIsClosed = false;
            mIsPickedUp = false;
        }
    }

    private void setColors() {
        int statusColor;

        if (mIsClosed)
            statusColor = getResources().getColor(R.color.closed_request_color);
        else if (mIsPickedUp)
            statusColor = getResources().getColor(R.color.picked_up_request_color);
        else
            statusColor = getResources().getColor(R.color.new_request_color);

        mTxtRequestStatus.setBackgroundColor(statusColor);
        mTxtRequestLocation.setBackgroundColor(statusColor);
    }

    private void setTexts() {
        if (mIsClosed)
            mTxtRequestStatus.setText(getResources().getString(R.string.txt_closed_request_title));
        else if (mIsPickedUp)
            mTxtRequestStatus.setText(getResources().getString(R.string.txt_picked_up_request_title));
        else
            mTxtRequestStatus.setText(getResources().getString(R.string.txt_new_request_title));

        // TODO: need to set city name
        mTxtRequestLocation.setText("TODO City Name");

        mTxtCreatedComment.setText(mRequestItem.getCreatedComment());
        mTxtCreatedAt.setText(mRequestItem.getCreatedAt());

        mTxtCreatedReputation.setText(String.valueOf(mRequestItem.getCreatedReputation()));
    }


    private void setPictures() {

        mImgRequestThumbnail.setVisibility(View.VISIBLE);

        String[] createdPictures = mRequestItem.getCreatedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        if (createdPictures.length > 0 ) {
            if (!createdPictures[0].equals(mCurrentPictureUrl)) {

                mImgRequestThumbnail.setImageDrawable(null);

                ImageLoader.getInstance().displayImage(
                        createdPictures[0],
                        mImgRequestThumbnail,
                        Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);

                mCurrentPictureUrl = createdPictures[0];
            }

        } else {
            mImgRequestThumbnail.setImageResource(R.drawable.ic_background_grass);
        }
    }

    private void updateUsers() {
        long createdBy = mRequestItem.getCreatedBy();
        // TODO: replace this code to obtain user's name from users' cache
        mTxtCreatedBy.setText(Long.toString(createdBy));
    }



    /**
     * ContentObserver that handles changes of request shown in this thumbnail
     */
    private class RequestThumbnailContentObserver extends ContentObserver {

        private View mView;
        private long mRequestId;
        private Context mContext;

        public RequestThumbnailContentObserver(Handler handler) {
            super(handler);
            mView = RequestThumbnailViewMVC.this;
            mRequestId = RequestThumbnailViewMVC.this.mRequestItem.getId();
            mContext = RequestThumbnailViewMVC.this.mContext;
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

            if (uri == null) {
                uri = ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI, mRequestId);
            }

            // This statement prevents the adapter from recycling this view. If not set, then when the
            // view is scrolled off the screen while this method is executed, it might be the case
            // the adapter will bind another request to this view, but execution of this method will
            // re-bind the old request, thus creating erroneous list.
            ViewCompat.setHasTransientState(mView, true);

            new AsyncTask<Uri,Void,Cursor>() {

                @Override
                protected Cursor doInBackground(Uri... uris) {
                    Cursor cursor = mContext.getContentResolver().query(
                            uris[0],
                            RequestItem.MANDATORY_REQUEST_FIELDS,
                            null,
                            null,
                            null);
                    return cursor;
                }

                @Override
                protected void onPostExecute(Cursor cursor) {
                    RequestItem request;

                    try {
                        request = RequestItem.createRequestItem(cursor);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        // TODO: consider more sophisticated error handling (maybe destroy the view?)
                        return;
                    }

                    ((RequestThumbnailViewMVC) mView).showRequestThumbnail(request);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);


            // Once all the changes were applied - this view might be recycled by the adapter
            ViewCompat.setHasTransientState(mView, false);
        }
    }



}
