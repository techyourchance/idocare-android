package il.co.idocare.mvcviews.requestthumbnail;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.MalformedURLException;
import java.net.URL;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.datamodels.functional.RequestItem;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.AbstractViewMVC;


/**
 * This is the top level View which should be used as a "thumbnail" for requests
 * when they are displayed in a list.
 */
public class RequestThumbnailViewMvcImpl
        extends AbstractViewMVC<RequestThumbnailViewMvc.RequestThumbnailViewMvcListener>
        implements RequestThumbnailViewMvc {

    private RequestItem mRequestItem;

    private TextView mTxtRequestStatus;
    private TextView mTxtRequestLocation;
    private ImageView mImgRequestThumbnail;
    private TextView mTxtCreatedComment;
    private TextView mTxtCreatedBy;
    private TextView mTxtCreatedAt;
    private TextView mTxtCreatedReputation;

    private String mCurrentPictureUrl = "";

    public RequestThumbnailViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_request_thumbnail, container, false));


        initialize();
    }



    /**
     * Initialize this MVC view. Must be called from constructor
     */
    private void initialize() {

        mTxtRequestStatus = (TextView) getRootView().findViewById(R.id.txt_request_status);
        mTxtRequestLocation = (TextView) getRootView().findViewById(R.id.txt_request_fine_location);
        mImgRequestThumbnail = (ImageView) getRootView().findViewById(R.id.img_request_thumbnail);
        mTxtCreatedComment = (TextView) getRootView().findViewById(R.id.txt_created_comment);
        mTxtCreatedBy = (TextView) getRootView().findViewById(R.id.txt_created_by);
        mTxtCreatedAt = (TextView) getRootView().findViewById(R.id.txt_created_at);
        mTxtCreatedReputation = (TextView) getRootView().findViewById(R.id.txt_votes);

    }


    @Override
    public Bundle getViewState() {
        return null;
    }


    /**
     * Update this thumbnail with the details of a particular request
     * @param request the request which should be displayed
     */
    @Override
    public void bindRequestItem(RequestItem request) {

        mRequestItem = request;

        // Update the UI
        setColors();
        setTexts();
        setPictures();
    }


    /**
     * Update this thumbnail's "created by" views with details of a particular user
     * @param user the user who's data should be displayed
     */
    @Override
    public void bindCreatedByUser(UserItem user) {
        mTxtCreatedBy.setText(user.getNickname());
    }

    /**
     * Update this thumbnail's "picked up by" views with details of a particular user
     * @param user the user who's data should be displayed
     */
    @Override
    public void bindPickedUpByUser(UserItem user) {
        if (mRequestItem.isPickedUp() ) {
            if (mRequestItem.getStatus() == RequestItem.RequestStatus.PICKED_UP_BY_OTHER) {
                mTxtRequestStatus.setText(
                        getRootView().getResources().getString(R.string.txt_picked_up_request_title) + " " +
                                getRootView().getResources().getString(R.string.txt_by) + " " + user.getNickname());
            } else {
                mTxtRequestStatus.setText(
                        getRootView().getResources().getString(R.string.txt_picked_up_request_title) + " " +
                                getRootView().getResources().getString(R.string.txt_by_me));
            }
        }
    }
    private void setColors() {
        int statusColor;

        if (mRequestItem.isClosed())
            statusColor = getRootView().getResources().getColor(R.color.closed_request_color);
        else if (mRequestItem.isPickedUp())
            statusColor = getRootView().getResources().getColor(R.color.picked_up_request_color);
        else
            statusColor = getRootView().getResources().getColor(R.color.new_request_color);

        mTxtRequestStatus.setBackgroundColor(statusColor);
        mTxtRequestLocation.setBackgroundColor(statusColor);
    }

    private void setTexts() {
        if (mRequestItem.isClosed())
            mTxtRequestStatus.setText(getRootView().getResources().getString(R.string.txt_closed_request_title));
        else if (mRequestItem.isPickedUp())
            mTxtRequestStatus.setText(getRootView().getResources().getString(R.string.txt_picked_up_request_title));
        else
            mTxtRequestStatus.setText(getRootView().getResources().getString(R.string.txt_new_request_title));

        // TODO: need to set city name
        mTxtRequestLocation.setText("");

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
