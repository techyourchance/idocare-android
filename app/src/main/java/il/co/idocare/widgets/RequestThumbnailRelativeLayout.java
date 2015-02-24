package il.co.idocare.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;

/**
 * This is the top level View which should be used as a "thumbnail" for requests
 * when they are displayed in a list.
 */
public class RequestThumbnailRelativeLayout extends RelativeLayout {


    private TextView mTxtRequestStatus;
    private TextView mTxtRequestLocation;
    private ImageView mImgRequestThumbnail;
    private TextView mTxtNoRequestThumbnailPicture;
    private TextView mTxtCreatedComment;
    private TextView mTxtCreatedBy;
    private TextView mTxtCreatedAt;
    private TextView mTxtCreatedVotes;

    private boolean mIsClosed;
    private boolean mIsPickedUp;



    public RequestThumbnailRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public RequestThumbnailRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RequestThumbnailRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * All three constructors invoke this method.
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_request_thumbnail, this, true);

        // This padding is required in order not to hide the border when colorizing inner views
        int padding = (int) getResources().getDimension(R.dimen.border_background_width);
        getRootView().setPadding(padding, padding, padding, padding);

        // Set background color and border for the whole item
        getRootView().setBackgroundColor(getResources().getColor(android.R.color.white));
        getRootView().setBackgroundResource(R.drawable.border_background);


        mTxtRequestStatus = (TextView) findViewById(R.id.txt_request_status);
        mTxtRequestLocation = (TextView) findViewById(R.id.txt_request_location);
        mImgRequestThumbnail = (ImageView) findViewById(R.id.img_request_thumbnail);
        mTxtNoRequestThumbnailPicture =
                (TextView) findViewById(R.id.txt_no_request_thumbnail_picture);
        mTxtCreatedComment = (TextView) findViewById(R.id.txt_created_comment);
        mTxtCreatedBy = (TextView) findViewById(R.id.txt_created_by);
        mTxtCreatedAt = (TextView) findViewById(R.id.txt_created_at);
        mTxtCreatedVotes = (TextView) findViewById(R.id.txt_created_votes);

        mIsClosed = false;
        mIsPickedUp = false;
    }

    /**
     * Show "thumbnail" details of the request
     * @param request the request item to be shown
     */
    public void showRequestThumbnail(RequestItem request) {
        setStatus(request);
        setColors();
        setTexts(request);
        setPictures(request);
    }


    private void setStatus(RequestItem request) {
        if (request.getClosedBy() != null) {
            mIsClosed = true;
        }
        else if (request.getPickedUpBy() != null) {
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
            statusColor = getResources().getColor(R.color.closed_request_status);
        else if (mIsPickedUp)
            statusColor = getResources().getColor(R.color.picked_up_request_status);
        else
            statusColor = getResources().getColor(R.color.new_request_status);

        mTxtRequestStatus.setBackgroundColor(statusColor);
        mTxtRequestLocation.setBackgroundColor(statusColor);
    }

    private void setTexts(RequestItem request) {
        if (mIsClosed)
            mTxtRequestStatus.setText(getResources().getString(R.string.txt_closed_request_title));
        else if (mIsPickedUp)
            mTxtRequestStatus.setText(getResources().getString(R.string.txt_picked_up_request_title));
        else
            mTxtRequestStatus.setText(getResources().getString(R.string.txt_new_request_title));

        // TODO: need to set city name
        mTxtRequestLocation.setText("TODO City Name");

        mTxtCreatedComment.setText(request.getCreatedComment());
        mTxtCreatedBy.setText(request.getCreatedBy().getNickname());
        mTxtCreatedAt.setText(request.getCreatedAt());

        // TODO: set actual votes
        mTxtCreatedVotes.setText("TODO\nVotes");
    }


    private void setPictures(RequestItem request) {

        mImgRequestThumbnail.setVisibility(View.GONE);
        mTxtNoRequestThumbnailPicture.setVisibility(View.VISIBLE);

        if (request.mCreatedPictures != null && request.mCreatedPictures.length > 0) {

            ImageLoader.getInstance().displayImage(
                    request.mCreatedPictures[0],
                    mImgRequestThumbnail,
                    Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS,
                    new RequestThumbnailLoadingListener(mTxtNoRequestThumbnailPicture,
                            getResources().getString(R.string.text_request_thumbnail_loading),
                            getResources().getString(R.string.text_request_thumbnail_failed),
                            getResources().getString(R.string.text_request_thumbnail_cancelled)),
                    new RequestThumbnailLoadingProgressListener(mTxtNoRequestThumbnailPicture,
                            getResources().getString(R.string.text_request_thumbnail_loading)));

        } else {
            mTxtNoRequestThumbnailPicture.
                    setText(getResources().getString(R.string.text_request_thumbnail_no_picture));
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
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(500);
            anim.setRepeatCount(0);
            view.startAnimation(anim);
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
