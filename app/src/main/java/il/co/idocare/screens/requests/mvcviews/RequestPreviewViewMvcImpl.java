package il.co.idocare.screens.requests.mvcviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;
import il.co.idocare.requests.RequestEntity;


/**
 * This is the top level View which should be used as a "thumbnail" for requests
 * when they are displayed in a list.
 */
public class RequestPreviewViewMvcImpl
        extends AbstractViewMVC<RequestPreviewViewMvcImpl.RequestPreviewViewMvcListener> {

    public interface RequestPreviewViewMvcListener {
        public void onRequestClicked(RequestEntity request);
    }

    private RequestEntity mRequest;

    private ImageView mImgRequestThumbnail;
    private TextView mTxtRequestStatus;
    private TextView mTxtRequestLocation;
    private TextView mTxtCreatedBy;
    private TextView mTxtCreatedAt;
    private TextView mTxtCreatedVotes;

    private String mCurrentPictureUri = "";

    public RequestPreviewViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_request_thumbnail, container, false));

        mTxtRequestStatus = findViewById(R.id.txt_request_status);
        mTxtRequestLocation = findViewById(R.id.txt_request_coarse_location);
        mImgRequestThumbnail = findViewById(R.id.img_request_thumbnail);
        mTxtCreatedBy = findViewById(R.id.txt_created_by);
        mTxtCreatedAt = findViewById(R.id.txt_created_at);
        mTxtCreatedVotes = findViewById(R.id.txt_created_votes);

    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    public void bindRequest(RequestEntity request) {

        mRequest = request;

        setColors();
        setTexts();
        setPreviewPicture();

    }


    private void setColors() {
        int statusColor;

        if (mRequest.isClosed())
            statusColor = getRootView().getResources().getColor(R.color.closed_request_color);
        else if (mRequest.isPickedUp())
            statusColor = getRootView().getResources().getColor(R.color.picked_up_request_color);
        else
            statusColor = getRootView().getResources().getColor(R.color.new_request_color);

        mTxtRequestStatus.setBackgroundColor(statusColor);
    }

    private void setTexts() {
        if (mRequest.isClosed())
            mTxtRequestStatus.setText(getRootView().getResources().getString(R.string.txt_closed_request_title));
        else if (mRequest.isPickedUp())
            mTxtRequestStatus.setText(getRootView().getResources().getString(R.string.txt_picked_up_request_title));
        else
            mTxtRequestStatus.setText(getRootView().getResources().getString(R.string.txt_new_request_title));

        mTxtCreatedAt.setText(mRequest.getCreatedAt());

        mTxtCreatedVotes.setText(String.valueOf(mRequest.getCreatedVotes()));
    }


    private void setPreviewPicture() {

        mImgRequestThumbnail.setVisibility(View.VISIBLE);

        List<String> createdPictures = mRequest.getCreatedPictures();

        if (createdPictures.isEmpty()) {
            setDefaultPreviewPicture();
        } else {
            setPreviewPictureFromUri(createdPictures.get(0));
        }
    }

    private void setPreviewPictureFromUri(String mainPictureUri) {

        if (mainPictureUri.equals(mCurrentPictureUri)) {
            return;
        }

        mImgRequestThumbnail.setImageDrawable(null);

        String universalImageLoaderUri = mainPictureUri;

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

        mCurrentPictureUri = mainPictureUri;
    }

    private void setDefaultPreviewPicture() {
        mCurrentPictureUri = "";
        mImgRequestThumbnail.setImageResource(R.drawable.ic_background_grass);
    }


}
