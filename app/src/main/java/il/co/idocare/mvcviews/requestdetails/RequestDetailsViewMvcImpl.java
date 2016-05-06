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
import android.widget.TextView;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
    private TextView mTxtFineLocation;
    private TextView mTxtCreatedByTitle;
    private RequestRelatedUserInfoViewMvc mCreatedByUserInfoViewMvc;
    private GestureImageView mGestureImgCreatedPictures;
    private TextView mTxtClosedByTitle;
    private RequestRelatedUserInfoViewMvc mClosedByUserInfoViewMvc;
    private GestureImageView mGestureImgClosedPictures;

    private FrameLayout mFrameUserInfoTop;
    private FrameLayout mFrameUserInfoBottom;

    private MapView mMapPreview;

    private Button mBtnPickUpRequest;
    private Button mBtnCloseRequest;
    private String[] mCreatedPictures;
    private String[] mClosedPictures;


    public RequestDetailsViewMvcImpl(@NonNull LayoutInflater inflater,
                                     @Nullable ViewGroup container,
                                     @NonNull ImageViewPictureLoader imageViewPictureLoader) {
        setRootView(inflater.inflate(R.layout.layout_request_details, container, false));
        mImageViewPictureLoader = imageViewPictureLoader;
        mContext = getRootView().getContext();


        // "Created by" MVC sub-view
        mCreatedByUserInfoViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                null,
                mImageViewPictureLoader);
        mCreatedByUserInfoViewMvc.registerListener(new RequestRelatedUserInfoViewMvc.RequestRelatedUserInfoViewMvcListener() {
            @Override
            public void onVoteUpClicked() {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onCreatedVoteUpClicked();
                }
            }

            @Override
            public void onVoteDownClicked() {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onCreatedVoteDownClicked();
                }
            }
        });


        // "Closed by" MVC sub-view
        mClosedByUserInfoViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                null,
                mImageViewPictureLoader);
        mClosedByUserInfoViewMvc.registerListener(new RequestRelatedUserInfoViewMvc.RequestRelatedUserInfoViewMvcListener() {
            @Override
            public void onVoteUpClicked() {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onClosedVoteUpClicked();
                }
            }

            @Override
            public void onVoteDownClicked() {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onClosedVoteDownClicked();
                }
            }
        });

        initialize();
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    @SuppressLint("CutPasteId")
    private void initialize() {

        mTxtStatus = (TextView) getRootView().findViewById(R.id.txt_request_status);

        mTxtCreatedByTitle = (TextView) getRootView().findViewById(R.id.txt_created_by_title);
        mTxtClosedByTitle = (TextView) getRootView().findViewById(R.id.txt_closed_by_title);

        mFrameUserInfoTop = (FrameLayout) getRootView().findViewById(R.id.frame_user_info_top);
        mFrameUserInfoBottom = (FrameLayout) getRootView().findViewById(R.id.frame_user_info_bottom);


        // "Created pictures" views
        mGestureImgCreatedPictures =
                (GestureImageView) getRootView().findViewById(R.id.gestureImgCreatedPictures);
        initGestureImageView(mGestureImgCreatedPictures);

        // Fine location view
        mTxtFineLocation = (TextView) getRootView().findViewById(R.id.txt_request_fine_location);

        
        // "Closed pictures" views
        mGestureImgClosedPictures =
                (GestureImageView) getRootView().findViewById(R.id.gestureImgClosedPictures);
        initGestureImageView(mGestureImgClosedPictures);

        // The map
        mMapPreview = (MapView) getRootView().findViewById(R.id.map_preview);

        mBtnPickUpRequest = (Button) getRootView().findViewById(R.id.btn_pickup_request);
        mBtnCloseRequest = (Button) getRootView().findViewById(R.id.btn_close_request);

    }

    private void initGestureImageView(GestureImageView gestureImageView) {
        Settings settings = gestureImageView.getController().getSettings();
        settings.setFitMethod(Settings.Fit.OUTSIDE);
        settings.setPanEnabled(false);
    }


    /**
     *
     * Show the details of the request
     * @param requestItem the request that should be shown
     */
    @Override
    public void bindRequestItem(RequestItem requestItem) {

        mRequestItem = requestItem;


        if (mRequestItem.isClosed() || mRequestItem.isPickedUp()) {
            mFrameUserInfoTop.removeAllViews();
            mFrameUserInfoTop.addView(mClosedByUserInfoViewMvc.getRootView());
            mFrameUserInfoBottom.removeAllViews();
            mFrameUserInfoBottom.addView(mCreatedByUserInfoViewMvc.getRootView());
        } else {
            mFrameUserInfoTop.removeAllViews();
            mFrameUserInfoTop.addView(mCreatedByUserInfoViewMvc.getRootView());
            mFrameUserInfoBottom.removeAllViews();
            mFrameUserInfoBottom.setVisibility(View.GONE);
        }

        updateStatus();
        // Handle the views related to initial request
        updateCreatedViews();
        // Handle the views related to pickup info
        updateClosedViews();
        // Configure location
        updateLocationViews();
        // Handle the pickup button functionality
        updatePickupButton();
        // Handle the close button functionality
        updateClosedButton();
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

    private void updateStatus() {
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
    }

    /**
     * Handle the views describing the initial request
     */
    private void updateCreatedViews() {

        mTxtCreatedByTitle.setText(R.string.txt_created_by_title);

        mCreatedByUserInfoViewMvc.bindDate(mRequestItem.getCreatedAt());
        mCreatedByUserInfoViewMvc.bindVotes(String.valueOf(mRequestItem.getCreatedReputation()));

        if (TextUtils.isEmpty(mRequestItem.getCreatedComment())) {
            mCreatedByUserInfoViewMvc.setCommentVisible(false);
        } else {
            mCreatedByUserInfoViewMvc.setCommentVisible(true);
            mCreatedByUserInfoViewMvc.bindComment(mRequestItem.getCreatedComment());
        }

        mCreatedPictures = mRequestItem.getCreatedPictures().split(Constants.PICTURES_LIST_SEPARATOR);

        mImageViewPictureLoader.loadFromWebOrFile(mGestureImgCreatedPictures, mCreatedPictures[0],
                R.drawable.ic_default_user_picture);

        mTxtFineLocation.setText("");
    }


    /**
     * Handle the views describing the info about request's closure
     */
    private void updateClosedViews() {
        if (mRequestItem.getStatus() != RequestItem.RequestStatus.CLOSED_BY_OTHER &&
                mRequestItem.getStatus() != RequestItem.RequestStatus.CLOSED_BY_ME) {
            // Hide all "closed" views if the request is not closed
            getRootView().findViewById(R.id.btn_close_request).setVisibility(View.GONE);
            return;
        }

        getRootView().findViewById(R.id.btn_close_request).setVisibility(View.VISIBLE);

        mTxtClosedByTitle.setText(R.string.txt_closed_by_title); // This should be complemented by "closed by" user's nickname
        mClosedByUserInfoViewMvc.bindDate(mRequestItem.getClosedAt());
        mClosedByUserInfoViewMvc.bindVotes(String.valueOf(mRequestItem.getClosedReputation()));

        if (TextUtils.isEmpty(mRequestItem.getClosedComment())) {
            mClosedByUserInfoViewMvc.setCommentVisible(false);
        } else {
            mClosedByUserInfoViewMvc.setCommentVisible(true);
            mClosedByUserInfoViewMvc.bindComment(mRequestItem.getClosedComment());
        }

        mClosedPictures = mRequestItem.getClosedPictures().split(Constants.PICTURES_LIST_SEPARATOR);

        mImageViewPictureLoader.loadFromWebOrFile(mGestureImgClosedPictures, mClosedPictures[0],
                R.drawable.ic_default_user_picture);
    }

    private void updateLocationViews() {

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
    private void updatePickupButton() {

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
    private void updateClosedButton() {

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
