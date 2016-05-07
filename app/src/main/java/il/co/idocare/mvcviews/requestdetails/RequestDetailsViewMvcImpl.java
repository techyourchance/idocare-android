package il.co.idocare.mvcviews.requestdetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorRes;
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
    private Resources mResources;

    private PresentationStrategy mPresentationStrategy;

    private FrameLayout mFrameUserInfoTop;
    private FrameLayout mFrameUserInfoBottom;
    private RequestRelatedUserInfoViewMvc mUserInfoTopViewMvc;
    private RequestRelatedUserInfoViewMvc mUserInfoBottomViewMvc;

    private RequestItem mRequestItem;

    private TextView mTxtStatus;
    private TextView mTxtFineLocation;
    private TextView mTxtTopUserTitle;
    private GestureImageView mGestureImgCreatedPictures;
    private TextView mTxtBottomUserTitle;
    private GestureImageView mGestureImgClosedPictures;


    private MapView mMapPreview;

    private Button mBtnPickUpRequest;
    private Button mBtnCloseRequest;
    private String[] mTopPictures;
    private String[] mBottomPictures;


    public RequestDetailsViewMvcImpl(@NonNull LayoutInflater inflater,
                                     @Nullable ViewGroup container,
                                     @NonNull ImageViewPictureLoader imageViewPictureLoader) {
        setRootView(inflater.inflate(R.layout.layout_request_details, container, false));
        mImageViewPictureLoader = imageViewPictureLoader;
        mContext = inflater.getContext();
        mResources = mContext.getResources();


        // "Created by" MVC sub-view
        mUserInfoTopViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                null,
                mImageViewPictureLoader);
        mUserInfoTopViewMvc.registerListener(new RequestRelatedUserInfoViewMvc.RequestRelatedUserInfoViewMvcListener() {
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
        mUserInfoBottomViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                null,
                mImageViewPictureLoader);
        mUserInfoBottomViewMvc.registerListener(new RequestRelatedUserInfoViewMvc.RequestRelatedUserInfoViewMvcListener() {
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

        mTxtTopUserTitle = (TextView) getRootView().findViewById(R.id.txt_created_by_title);
        mTxtBottomUserTitle = (TextView) getRootView().findViewById(R.id.txt_closed_by_title);

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

        if (mRequestItem.isClosed()) {
            mPresentationStrategy = new ClosedPresentationStrategy();
        } else if (mRequestItem.isPickedUp()) {
            mPresentationStrategy = new PickedUpPresentationStrategy();
        } else {
            mPresentationStrategy = new NewPresentationStrategy();
        }

        mPresentationStrategy.bindRequestItem(requestItem);

    }


    @Override
    public void bindCreatedByUser(UserItem user) {
        mPresentationStrategy.bindCreatedByUser(user);
    }


    @Override
    public void bindClosedByUser(UserItem user) {
        mPresentationStrategy.bindClosedByUser(user);
    }


    @Override
    public void bindPickedUpByUser(UserItem user) {
        mPresentationStrategy.bindPickedUpByUser(user);
    }





    private abstract class PresentationStrategy {

        protected void bindRequestItem(RequestItem request) {
            mTxtStatus.setBackgroundColor(mResources.getColor(getStatusColorResId()));
            mTxtStatus.setText(getStatusString());

            if (TextUtils.isEmpty(getTopUserTitle())) {
                mTxtTopUserTitle.setVisibility(View.GONE);
            } else {
                mTxtTopUserTitle.setVisibility(View.VISIBLE);
                mTxtTopUserTitle.setText(getTopUserTitle());
            }

            mUserInfoTopViewMvc.bindDate(getTopUserDate());
            mUserInfoTopViewMvc.bindVotes(getTopUserVotes());

            if (TextUtils.isEmpty(getTopUserComment())) {
                mUserInfoTopViewMvc.setCommentVisible(false);
            } else {
                mUserInfoTopViewMvc.setCommentVisible(true);
                mUserInfoTopViewMvc.bindComment(getTopUserComment());
            }

            mTopPictures = getTopPictures();

            mImageViewPictureLoader.loadFromWebOrFile(mGestureImgCreatedPictures, mTopPictures[0],
                    R.drawable.ic_default_user_picture);

            if (showCloseRequestButton()) {
                mBtnCloseRequest.setVisibility(View.VISIBLE);
            } else {
                mBtnCloseRequest.setVisibility(View.GONE);
            }


            mTxtBottomUserTitle.setText(getBottomUserTitle());
            mUserInfoBottomViewMvc.bindDate(getBottomUserDate());
            mUserInfoBottomViewMvc.bindVotes(getBottomUserVotes());

            if (TextUtils.isEmpty(getBottomUserComment())) {
                mUserInfoBottomViewMvc.setCommentVisible(false);
            } else {
                mUserInfoBottomViewMvc.setCommentVisible(true);
                mUserInfoBottomViewMvc.bindComment(getBottomUserComment());
            }

            mBottomPictures = getBottomPictures();

            mImageViewPictureLoader.loadFromWebOrFile(mGestureImgClosedPictures, mBottomPictures[0],
                    R.drawable.ic_default_user_picture);


            mTxtFineLocation.setText(mRequestItem.getLocation());

            if (mRequestItem.isClosed()) {
                // Don't show the map for closed requests
                mMapPreview.setVisibility(View.GONE);
                return;
            }

            GoogleMap map = mMapPreview.getMap();

            MapsInitializer.initialize(mContext);
            map.setMyLocationEnabled(false); // Don't show my location
            map.setBuildingsEnabled(false); // Don't show 3D buildings
            map.getUiSettings().setMapToolbarEnabled(false); // No toolbar needed in a lite preview

            LatLng location = new LatLng(mRequestItem.getLatitude(), mRequestItem.getLongitude());
            // Center the camera at request location
            map.moveCamera(CameraUpdateFactory.newLatLng(location));
            // Put a marker
            map.addMarker(new MarkerOptions().position(location));

            if (showPickUpRequestButton()) {
                mBtnPickUpRequest.setVisibility(View.VISIBLE);
            } else {
                mBtnPickUpRequest.setVisibility(View.GONE);
            }
        }

        protected abstract String getBottomUserDate();
        protected abstract String getBottomUserVotes();
        protected abstract String getBottomUserComment();
        protected abstract String[] getBottomPictures();
        protected abstract boolean showPickUpRequestButton();
        protected abstract String getBottomUserTitle();
        protected abstract boolean showCloseRequestButton();


        abstract void bindCreatedByUser(UserItem user);
        abstract void bindPickedUpByUser(UserItem user);
        abstract void bindClosedByUser(UserItem user);

        protected abstract @ColorRes int getStatusColorResId();
        protected abstract String getStatusString();
        protected abstract String getTopUserTitle();
        protected abstract String[] getTopPictures();
        protected abstract String getTopUserComment();
        protected abstract String getTopUserVotes();
        protected abstract String getTopUserDate();
    }

    private class NewPresentationStrategy extends PresentationStrategy {


        @Override
        public void bindCreatedByUser(UserItem user) {

        }

        @Override
        public void bindPickedUpByUser(UserItem user) {

        }

        @Override
        public void bindClosedByUser(UserItem user) {

        }
    }

    private class PickedUpPresentationStrategy implements PresentationStrategy {

        @Override
        public void bindRequestItem(RequestItem request) {
            mTxtStatus.setBackgroundColor(mResources.getColor(R.color.picked_up_request_color);
            mTxtStatus.setText(mResources.getString(R.string.txt_picked_up_request_title));

        }

        @Override
        public void bindCreatedByUser(UserItem user) {

        }

        @Override
        public void bindPickedUpByUser(UserItem user) {

        }

        @Override
        public void bindClosedByUser(UserItem user) {

        }
    }

    private class ClosedPresentationStrategy implements PresentationStrategy {

        @Override
        public void bindRequestItem(RequestItem request) {
            mTxtStatus.setBackgroundColor(mResources.getColor(R.color.closed_request_color));
            mTxtStatus.setText(mResources.getString(R.string.txt_closed_request_title));
        }

        @Override
        public void bindCreatedByUser(UserItem user) {

        }

        @Override
        public void bindPickedUpByUser(UserItem user) {

        }

        @Override
        public void bindClosedByUser(UserItem user) {

        }
    }
}
