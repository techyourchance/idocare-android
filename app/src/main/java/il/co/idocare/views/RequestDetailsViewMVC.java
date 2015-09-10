package il.co.idocare.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.MalformedURLException;
import java.net.URL;

import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.pojos.RequestItem.RequestStatus;
import il.co.idocare.pojos.UserItem;
import il.co.idocare.utils.UtilMethods;

/**
 * MVC View for New Request screen.
 */
public class RequestDetailsViewMVC implements ViewMVC {

    private final static String LOG_TAG = RequestDetailsViewMVC.class.getSimpleName();

    private Context mContext;

    private RequestItem mRequestItem;


    private View mRootView;
    private TextView mTxtStatus;
    private TextView mTxtCoarseLocation;
    private TextView mTxtFineLocation;
    private TextView mTxtCreatedTitle;
    private ImageView mImgCreatedByPicture;
    private TextView mTxtCreatedByNickname;
    private TextView mTxtCreatedAt;
    private TextView mTxtCreatedByReputation;
    private TextView mTxtCreatedReputation;
    private TextView mTxtCreatedComment;
    private ImageView[] mImgCreatedPictures;
    private ImageView mImgCreatedVoteUp;
    private ImageView mImgCreatedVoteDown;
    private TextView mTxtClosedByTitle;
    private ImageView mImgClosedByPicture;
    private TextView mTxtClosedByNickname;
    private TextView mTxtClosedAt;
    private TextView mTxtClosedByReputation;
    private TextView mTxtClosedReputation;
    private TextView mTxtClosedComment;
    private ImageView[] mImgClosedPictures;
    private ImageView mImgClosedVoteUp;
    private ImageView mImgClosedVoteDown;

    private MapView mMapPreview;

    private Button mBtnPickUpRequest;
    private Button mBtnCloseRequest;


    public RequestDetailsViewMVC(Context context, ViewGroup container, Bundle savedInstanceState) {
        mContext = context;

        mRootView = LayoutInflater.from(mContext)
                .inflate(R.layout.fragment_request_details, container, false);

        initialize();

    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    @SuppressLint("CutPasteId")
    private void initialize() {
        View includedView; // Used to reference compound sub-views

        // Status bar views
        mTxtStatus = (TextView) mRootView.findViewById(R.id.txt_request_status);
        mTxtCoarseLocation = (TextView) mRootView.findViewById(R.id.txt_request_coarse_location);

        // "Created by" views
        includedView = mRootView.findViewById(R.id.element_created_by);
        mTxtCreatedTitle = (TextView) includedView.findViewById(R.id.txt_title);
        mImgCreatedByPicture = (ImageView) includedView.findViewById(R.id.img_user_picture);
        mTxtCreatedByNickname = (TextView) includedView.findViewById(R.id.txt_user_nickname);
        mTxtCreatedAt = (TextView) includedView.findViewById(R.id.txt_created_at);
        mTxtCreatedByReputation = (TextView) includedView.findViewById(R.id.txt_user_reputation);
        mTxtCreatedReputation = (TextView) includedView.findViewById(R.id.txt_votes);
        mTxtCreatedComment = (TextView) includedView.findViewById(R.id.txt_comment);
        mImgCreatedVoteUp = (ImageView) includedView.findViewById(R.id.img_vote_up);
        mImgCreatedVoteDown = (ImageView) includedView.findViewById(R.id.img_vote_down);

        // "Created pictures" views
        includedView = mRootView.findViewById(R.id.element_created_pictures);
        mImgCreatedPictures = new ImageView[3];
        mImgCreatedPictures[0] = (ImageView) includedView.findViewById(R.id.img_picture0);
        mImgCreatedPictures[1] = (ImageView) includedView.findViewById(R.id.img_picture1);
        mImgCreatedPictures[2] = (ImageView) includedView.findViewById(R.id.img_picture2);

        // Fine location view
        mTxtFineLocation = (TextView) mRootView.findViewById(R.id.txt_request_fine_location);

        // "Closed by" views
        includedView = mRootView.findViewById(R.id.element_closed_by);
        mTxtClosedByTitle = (TextView) includedView.findViewById(R.id.txt_title);
        mImgClosedByPicture = (ImageView) includedView.findViewById(R.id.img_user_picture);
        mTxtClosedByNickname = (TextView) includedView.findViewById(R.id.txt_user_nickname);
        mTxtClosedAt = (TextView) includedView.findViewById(R.id.txt_created_at);
        mTxtClosedByReputation = (TextView) includedView.findViewById(R.id.txt_user_reputation);
        mTxtClosedReputation = (TextView) includedView.findViewById(R.id.txt_votes);
        mTxtClosedComment = (TextView) includedView.findViewById(R.id.txt_comment);
        mImgClosedVoteUp = (ImageView) includedView.findViewById(R.id.img_vote_up);
        mImgClosedVoteDown = (ImageView) includedView.findViewById(R.id.img_vote_down);

        // "Closed pictures" views
        includedView = mRootView.findViewById(R.id.element_closed_pictures);
        mImgClosedPictures = new ImageView[3];
        mImgClosedPictures[0] = (ImageView) includedView.findViewById(R.id.img_picture0);
        mImgClosedPictures[1] = (ImageView) includedView.findViewById(R.id.img_picture1);
        mImgClosedPictures[2] = (ImageView) includedView.findViewById(R.id.img_picture2);

        // The map
        mMapPreview = (MapView) mRootView.findViewById(R.id.map_preview);

        // "Close" button
        mBtnPickUpRequest = (Button) mRootView.findViewById(R.id.btn_pickup_request);
        mBtnCloseRequest = (Button) mRootView.findViewById(R.id.btn_close_request);

    }


    /**
     *
     * Show the details of the request
     * @param requestItem the request that should be shown
     */
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


    public void bindCreatedByUser(UserItem user) {

        mTxtCreatedByNickname.setText(user.getNickname());

        if (!TextUtils.isEmpty(user.getPictureUrl())) {
            String universalImageLoaderUri = user.getPictureUrl();
            try {
                new URL(universalImageLoaderUri);
            } catch (MalformedURLException e) {
                // The exception means that the current Uri is not a valid URL - it is local
                // uri and we need to adjust it to the scheme recognized by UIL
                universalImageLoaderUri = "file://" + universalImageLoaderUri;
            }

            ImageLoader.getInstance().displayImage(
                    universalImageLoaderUri,
                    mImgCreatedByPicture,
                    Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
        } else {
            mImgCreatedByPicture.setImageResource(R.drawable.default_user_picture);
        }

        mTxtCreatedByReputation.setText(String.valueOf(user.getReputation()));
    }


    public void bindClosedByUser(UserItem user) {

        mTxtClosedByNickname.setText(user.getNickname());

        if (!TextUtils.isEmpty(user.getPictureUrl())) {
            String universalImageLoaderUri = user.getPictureUrl();
            try {
                new URL(universalImageLoaderUri);
            } catch (MalformedURLException e) {
                // The exception means that the current Uri is not a valid URL - it is local
                // uri and we need to adjust it to the scheme recognized by UIL
                universalImageLoaderUri = "file://" + universalImageLoaderUri;
            }

            ImageLoader.getInstance().displayImage(
                    universalImageLoaderUri,
                    mImgClosedByPicture,
                    Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
        } else {
            mImgClosedByPicture.setImageResource(R.drawable.default_user_picture);
        }

        mTxtClosedByReputation.setText(String.valueOf(user.getReputation()));
    }


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

        mTxtCreatedTitle.setText(R.string.txt_created_title); // This should be complemented by "created by" user's nickname

        mTxtCreatedAt.setText(mRequestItem.getCreatedAt());
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

        mTxtFineLocation.setText("TODO fine loc");

        mImgCreatedVoteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CreatedVoteUpButtonClickEvent());
            }
        });

        mImgCreatedVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CreatedVoteDownButtonClickEvent());
            }
        });

    }


    /**
     * Handle the views describing the info about request's closure
     */
    private void configureClosedViews() {
        if (mRequestItem.getStatus() != RequestStatus.CLOSED_BY_OTHER &&
                mRequestItem.getStatus() != RequestStatus.CLOSED_BY_ME) {
            // Hide all "closed" views if the request is not closed
            mRootView.findViewById(R.id.element_closed_by).setVisibility(View.GONE);
            mRootView.findViewById(R.id.element_closed_pictures).setVisibility(View.GONE);
            mRootView.findViewById(R.id.btn_close_request).setVisibility(View.GONE);
            mRootView.findViewById(R.id.line_users_separator).setVisibility(View.GONE);
            return;
        }

        mRootView.findViewById(R.id.element_closed_by).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.element_closed_pictures).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.btn_close_request).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.line_users_separator).setVisibility(View.VISIBLE);

        mTxtClosedByTitle.setText(R.string.txt_closed_by_title); // This should be complemented by "closed by" user's nickname
        mTxtClosedAt.setText(mRequestItem.getClosedAt());
        mTxtClosedReputation.setText(String.valueOf(mRequestItem.getClosedReputation()));

        if (TextUtils.isEmpty(mRequestItem.getClosedComment())) {
            mTxtClosedComment.setVisibility(View.GONE);
        } else {
            mTxtClosedComment.setVisibility(View.VISIBLE);
            mTxtClosedComment.setText(mRequestItem.getClosedComment());
        }
        
        String[] closedPictures = mRequestItem.getClosedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        for (int i=0; i<3; i++) {
            if (closedPictures.length > i&& !TextUtils.isEmpty(closedPictures[i])) {

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
                EventBus.getDefault().post(new ClosedVoteUpButtonClickEvent());
            }
        });

        mImgClosedVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ClosedVoteDownButtonClickEvent());
            }
        });

    }

    private void configureLocationViews() {

        mTxtFineLocation.setText(mRequestItem.getLocation());

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

        if (mRequestItem.getStatus() == RequestStatus.NEW_BY_ME ||
                mRequestItem.getStatus() == RequestStatus.NEW_BY_OTHER) {
            mBtnPickUpRequest.setVisibility(View.VISIBLE);
            mBtnPickUpRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new PickupRequestButtonClickEvent());
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

        if (mRequestItem.getStatus() == RequestStatus.PICKED_UP_BY_ME) {
            mBtnCloseRequest.setVisibility(View.VISIBLE);
            mBtnCloseRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new CloseRequestButtonClickEvent());
                }
            });
        } else {
            mBtnCloseRequest.setVisibility(View.GONE);
        }

    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    public static class CloseRequestButtonClickEvent {}

    public static class PickupRequestButtonClickEvent {}

    public static class ClosedVoteUpButtonClickEvent {}

    public static class ClosedVoteDownButtonClickEvent {}

    public static class CreatedVoteUpButtonClickEvent {}

    public static class CreatedVoteDownButtonClickEvent {}



    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------


}
