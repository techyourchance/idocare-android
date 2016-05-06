package il.co.idocare.mvcviews.requestdetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;

import java.net.MalformedURLException;
import java.net.URL;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.datamodels.functional.RequestItem;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.AbstractViewMVC;
import il.co.idocare.mvcviews.userinfo.RequestRelatedUserInfoViewMvc;
import il.co.idocare.pictures.ImageViewPictureLoader;

/**
 * MVC View for New Request screen.
 */
public class RequestDetailsViewMvcImpl
        extends AbstractViewMVC<RequestDetailsViewMvc.RequestDetailsViewMvcListener>
        implements RequestDetailsViewMvc {

    private final ImageViewPictureLoader mImageViewPictureLoader;
    private Context mContext;

    private RequestItem mRequestItem;

    private TextView mTxtStatus;
    private TextView mTxtCoarseLocation;
    private TextView mTxtFineLocation;
    private TextView mTxtCreatedTitle;
    private RequestRelatedUserInfoViewMvc mCreatedByUserInfoViewMvc;
    private TextView mTxtCreatedReputation;
    private TextView mTxtCreatedComment;
    private ImageView[] mImgCreatedPictures;
    private ImageView mImgCreatedVoteUp;
    private ImageView mImgCreatedVoteDown;
    private TextView mTxtClosedByTitle;
    private RequestRelatedUserInfoViewMvc mClosedByUserInfoViewMvc;
    private TextView mTxtClosedReputation;
    private TextView mTxtClosedComment;
    private ImageView[] mImgClosedPictures;
    private ImageView mImgClosedVoteUp;
    private ImageView mImgClosedVoteDown;

    private MapView mMapPreview;

    private Button mBtnPickUpRequest;
    private Button mBtnCloseRequest;


    public RequestDetailsViewMvcImpl(@NonNull LayoutInflater inflater,
                                     @Nullable ViewGroup container,
                                     @NonNull ImageViewPictureLoader imageViewPictureLoader) {
        setRootView(inflater.inflate(R.layout.layout_request_details, container, false));
        mImageViewPictureLoader = imageViewPictureLoader;
        mContext = getRootView().getContext();
        initialize();
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    @SuppressLint("CutPasteId")
    private void initialize() {
        View includedView; // Used to reference compound sub-views

        // Status bar views
        mTxtStatus = (TextView) getRootView().findViewById(R.id.txt_request_status);
        mTxtCoarseLocation = (TextView) getRootView().findViewById(R.id.txt_request_coarse_location);


        mTxtCreatedTitle = (TextView) getRootView().findViewById(R.id.txt_created_by_title);
        mTxtClosedByTitle = (TextView) getRootView().findViewById(R.id.txt_closed_by_title);

        // "Created by" views
        mCreatedByUserInfoViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                (FrameLayout) getRootView().findViewById(R.id.frame_created_by_info),
                mImageViewPictureLoader);

        View createdVotePanel = getRootView().findViewById(R.id.element_created_vote_panel);
        mTxtCreatedReputation = (TextView) createdVotePanel.findViewById(R.id.txt_votes);
        mImgCreatedVoteUp = (ImageView) createdVotePanel.findViewById(R.id.img_vote_up);
        mImgCreatedVoteDown = (ImageView) createdVotePanel.findViewById(R.id.img_vote_down);

        mTxtCreatedComment = (TextView) getRootView().findViewById(R.id.txt_created_comment);

        // "Created pictures" views
        includedView = getRootView().findViewById(R.id.element_created_pictures);
        mImgCreatedPictures = new ImageView[3];
        mImgCreatedPictures[0] = (ImageView) includedView.findViewById(R.id.img_picture0);
        mImgCreatedPictures[1] = (ImageView) includedView.findViewById(R.id.img_picture1);
        mImgCreatedPictures[2] = (ImageView) includedView.findViewById(R.id.img_picture2);

        // Fine location view
        mTxtFineLocation = (TextView) getRootView().findViewById(R.id.txt_request_fine_location);

        // "Closed by" views
        mClosedByUserInfoViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                (FrameLayout) getRootView().findViewById(R.id.frame_closed_by_info),
                mImageViewPictureLoader);

        View closedVotePanel = getRootView().findViewById(R.id.element_closed_vote_panel);
        mTxtClosedReputation = (TextView) closedVotePanel.findViewById(R.id.txt_votes);
        mImgClosedVoteUp = (ImageView) closedVotePanel.findViewById(R.id.img_vote_up);
        mImgClosedVoteDown = (ImageView) closedVotePanel.findViewById(R.id.img_vote_down);

        mTxtClosedComment = (TextView) getRootView().findViewById(R.id.txt_closed_comment);
        
        // "Closed pictures" views
        includedView = getRootView().findViewById(R.id.element_closed_pictures);
        mImgClosedPictures = new ImageView[3];
        mImgClosedPictures[0] = (ImageView) includedView.findViewById(R.id.img_picture0);
        mImgClosedPictures[1] = (ImageView) includedView.findViewById(R.id.img_picture1);
        mImgClosedPictures[2] = (ImageView) includedView.findViewById(R.id.img_picture2);

        // The map
        mMapPreview = (MapView) getRootView().findViewById(R.id.map_preview);

        // "Close" button
        mBtnPickUpRequest = (Button) getRootView().findViewById(R.id.btn_pickup_request);
        mBtnCloseRequest = (Button) getRootView().findViewById(R.id.btn_close_request);

    }


    /**
     *
     * Show the details of the request
     * @param requestItem the request that should be shown
     */
    @Override
    public void bindRequestItem(RequestItem requestItem) {

        mRequestItem = requestItem;

        // Handle the status bar
        configureStatusBar();
        // Handle the views related to initial request
        configureCreatedViews();
        // Handle the views related to pickup info
        configureClosedViews();
        // Configure location
        configureLocationViews();
        // Handle the pickup button functionality
        configurePickupButton();
        // Handle the close button functionality
        configureClosedButton();
    }


    @Override
    public void bindCreatedByUser(UserItem user) {
        mCreatedByUserInfoViewMvc.bindUser(user);
    }


    @Override
    public void bindClosedByUser(UserItem user) {
        mClosedByUserInfoViewMvc.bindUser(user);
    }


    @Override
    public void bindPickedUpByUser(UserItem user) {
        if (mRequestItem.isPickedUp()) {
            if (mRequestItem.getStatus() == RequestItem.RequestStatus.PICKED_UP_BY_OTHER) {
                mTxtStatus.setText(
                        mContext.getResources().getString(R.string.txt_picked_up_request_title)
                                + " " +
                                mContext.getResources().getString(R.string.txt_by) + " " + user.getNickname());
            } else {
                mTxtStatus.setText(
                        mContext.getResources().getString(R.string.txt_picked_up_request_title)
                                + " " +
                                mContext.getResources().getString(R.string.txt_by_me));
            }
        }
    }



    /**
     * Handle the status bar text and color
     */
    private void configureStatusBar() {

        int statusColor;
        String statusText;

        if (mRequestItem.isClosed()) {
            statusColor = mContext.getResources().getColor(R.color.closed_request_color);
            statusText = mContext.getResources().getString(R.string.txt_closed_request_title);
        } else if (mRequestItem.isPickedUp()) {
            statusColor = mContext.getResources().getColor(R.color.picked_up_request_color);
            statusText = mContext.getResources().getString(R.string.txt_picked_up_request_title);
        } else {
            statusColor = mContext.getResources().getColor(R.color.new_request_color);
            statusText = mContext.getResources().getString(R.string.txt_new_request_title);
        }

        mTxtStatus.setBackgroundColor(statusColor);
        mTxtStatus.setText(statusText);

        mTxtCoarseLocation.setBackgroundColor(statusColor);
        mTxtCoarseLocation.setText("");


    }

    /**
     * Handle the views describing the initial request
     */
    private void configureCreatedViews() {

        mTxtCreatedTitle.setText(R.string.txt_created_by_title);

        mCreatedByUserInfoViewMvc.bindCustomInfo(mRequestItem.getCreatedAt());
        mTxtCreatedReputation.setText(String.valueOf(mRequestItem.getCreatedReputation()));

        if (TextUtils.isEmpty(mRequestItem.getCreatedComment())) {
            mTxtCreatedComment.setVisibility(View.GONE);
        } else {
            mTxtCreatedComment.setVisibility(View.VISIBLE);
            mTxtCreatedComment.setText(mRequestItem.getCreatedComment());
        }

        String[] createdPictures = mRequestItem.getCreatedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        for (int i = 0; i < 3; i++) {
            if (createdPictures.length > i && !TextUtils.isEmpty(createdPictures[i])) {

                String universalImageLoaderUri = createdPictures[i];
                try {
                    new URL(universalImageLoaderUri);
                } catch (MalformedURLException e) {
                    // The exception means that the current Uri is not a valid URL - it is local
                    // uri and we need to adjust it to the scheme recognized by UIL
                    universalImageLoaderUri = "file://" + universalImageLoaderUri;
                }

                ImageLoader.getInstance().displayImage(
                        universalImageLoaderUri,
                        mImgCreatedPictures[i],
                        Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
            } else {
                mImgCreatedPictures[i].setVisibility(View.GONE);

            }
        }


        mTxtFineLocation.setText("");

        mImgCreatedVoteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onCreatedVoteUpClicked();
                }
            }
        });

        mImgCreatedVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onCreatedVoteDownClicked();
                }
            }
        });

    }


    /**
     * Handle the views describing the info about request's closure
     */
    private void configureClosedViews() {
        if (mRequestItem.getStatus() != RequestItem.RequestStatus.CLOSED_BY_OTHER &&
                mRequestItem.getStatus() != RequestItem.RequestStatus.CLOSED_BY_ME) {
            // Hide all "closed" views if the request is not closed
            getRootView().findViewById(R.id.element_closed_pictures).setVisibility(View.GONE);
            getRootView().findViewById(R.id.btn_close_request).setVisibility(View.GONE);
            return;
        }

        getRootView().findViewById(R.id.element_closed_pictures).setVisibility(View.VISIBLE);
        getRootView().findViewById(R.id.btn_close_request).setVisibility(View.VISIBLE);

        mTxtClosedByTitle.setText(R.string.txt_closed_by_title); // This should be complemented by "closed by" user's nickname
        mClosedByUserInfoViewMvc.bindCustomInfo(mRequestItem.getClosedAt());
        mTxtClosedReputation.setText(String.valueOf(mRequestItem.getClosedReputation()));

        if (TextUtils.isEmpty(mRequestItem.getClosedComment())) {
            mTxtClosedComment.setVisibility(View.GONE);
        } else {
            mTxtClosedComment.setVisibility(View.VISIBLE);
            mTxtClosedComment.setText(mRequestItem.getClosedComment());
        }
        
        String[] closedPictures = mRequestItem.getClosedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        for (int i=0; i<3; i++) {
            if (closedPictures.length > i && !TextUtils.isEmpty(closedPictures[i])) {


                String universalImageLoaderUri = closedPictures[i];
                try {
                    new URL(universalImageLoaderUri);
                } catch (MalformedURLException e) {
                    // The exception means that the current Uri is not a valid URL - it is local
                    // uri and we need to adjust it to the scheme recognized by UIL
                    universalImageLoaderUri = "file://" + universalImageLoaderUri;
                }

                ImageLoader.getInstance().displayImage(
                        universalImageLoaderUri,
                        mImgClosedPictures[i],
                        Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
            } else {
                mImgClosedPictures[i].setVisibility(View.GONE);

            }
        }


        mImgClosedVoteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onClosedVoteUpClicked();
                }
            }
        });

        mImgClosedVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onClosedVoteDownClicked();
                }
            }
        });

    }

    private void configureLocationViews() {

        mTxtFineLocation.setText(mRequestItem.getLocation());

        if (mRequestItem.isClosed()) {
            // Don't show the map for closed requests
            mMapPreview.setVisibility(View.GONE);
            return;
        }

        GoogleMap map = mMapPreview.getMap();

        MapsInitializer.initialize(getRootView().getContext());
        map.setMyLocationEnabled(false); // Don't show my location
        map.setBuildingsEnabled(false); // Don't show 3D buildings
        map.getUiSettings().setMapToolbarEnabled(false); // No toolbar needed in a lite preview

        LatLng location = new LatLng(mRequestItem.getLatitude(), mRequestItem.getLongitude());
        // Center the camera at request location
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
        // Put a marker
        map.addMarker(new MarkerOptions().position(location));

    }

    /**
     * Handle the view of the pickup button
     */
    private void configurePickupButton() {

        if (mRequestItem.getStatus() == RequestItem.RequestStatus.NEW_BY_ME ||
                mRequestItem.getStatus() == RequestItem.RequestStatus.NEW_BY_OTHER) {
            mBtnPickUpRequest.setVisibility(View.VISIBLE);
            mBtnPickUpRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (RequestDetailsViewMvcListener listener : getListeners()) {
                        listener.onPickupRequestClicked();
                    }
                }
            });
        }
        else {
            mBtnPickUpRequest.setVisibility(View.GONE);
        }

    }



    /**
     * Handle the view of the close button
     */
    private void configureClosedButton() {

        if (mRequestItem.getStatus() == RequestItem.RequestStatus.PICKED_UP_BY_ME) {
            mBtnCloseRequest.setVisibility(View.VISIBLE);
            mBtnCloseRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (RequestDetailsViewMvcListener listener : getListeners()) {
                        listener.onCloseRequestClicked();
                    }
                }
            });
        } else {
            mBtnCloseRequest.setVisibility(View.GONE);
        }

    }


}
