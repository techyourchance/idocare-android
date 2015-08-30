package il.co.idocare.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.MalformedURLException;
import java.net.URL;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.pojos.UserItem;
import il.co.idocare.utils.UtilMethods;

/**
 * This is the top level View which should be used as a "thumbnail" for requests
 * when they are displayed in a list.
 */
public class RequestThumbnailViewMVC extends RelativeLayout implements ViewMVC{

    private static final String LOG_TAG = RequestThumbnailViewMVC.class.getSimpleName();

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

        mCurrentPictureUrl = "";


        // Inflate the underlying layout
        LayoutInflater.from(context).inflate(R.layout.layout_request_thumbnail, this, true);

        initialize();
    }



    /**
     * Initialize this MVC view. Must be called from constructor
     */
    private void initialize() {

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
    public Bundle getViewState() {
        return null;
    }


    /**
     * Update this thumbnail with the details of a particular request
     * @param request the request which should be displayed
     */
    public void bindRequestItem(RequestItem request) {

        mRequestItem = request;

        // Update the UI
        setStatus();
        setColors();
        setTexts();
        setPictures();
    }


    /**
     * Update this thumbnail's "created by" views with details of a particular user
     * @param user the user who's data should be displayed
     */
    public void bindCreatedByUser(UserItem user) {
        mTxtCreatedBy.setText(user.getNickname());
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

        if (TextUtils.isEmpty(mRequestItem.getCreatedPictures())) {
            mCurrentPictureUrl = "";
            mImgRequestThumbnail.setImageResource(R.drawable.ic_background_grass);
            return;
        }

        String[] createdPictures = mRequestItem.getCreatedPictures().split(Constants.PICTURES_LIST_SEPARATOR);

        if (!createdPictures[0].equals(mCurrentPictureUrl)) {

            mImgRequestThumbnail.setImageDrawable(null);

            String universalImageLoaderUri = createdPictures[0];
            try {
                new URL(universalImageLoaderUri);
            } catch (MalformedURLException e) {
                // The exception means that the current Uri is not a valid URL - it is local
                // uri and we need to adjust it to the scheme recognized by UIL
                universalImageLoaderUri = "file://" + universalImageLoaderUri;
            }

            ImageLoader.getInstance().displayImage(
                    universalImageLoaderUri,
                    mImgRequestThumbnail,
                    Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);

            mCurrentPictureUrl = createdPictures[0];
        }
    }




}
