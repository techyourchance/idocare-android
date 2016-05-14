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
import android.widget.ImageView;
import android.widget.TextView;

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
    private TextView mTxtLocationTitle;
    private TextView mTxtFineLocation;
    private TextView mTxtTopUserTitle;
    private ImageView mImgTopPictures;
    private TextView mTxtBottomUserTitle;
    private ImageView mImgBottomPictures;


    private MapView mMapPreview;

    private Button mBtnPickUpRequest;
    private Button mBtnCloseRequest;


    public RequestDetailsViewMvcImpl(@NonNull LayoutInflater inflater,
                                     @Nullable ViewGroup container,
                                     @NonNull ImageViewPictureLoader imageViewPictureLoader) {
        setRootView(inflater.inflate(R.layout.layout_request_details, container, false));
        mImageViewPictureLoader = imageViewPictureLoader;
        mContext = inflater.getContext();
        mResources = mContext.getResources();


        // "TOP" MVC sub-view
        mUserInfoTopViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                null,
                mImageViewPictureLoader);

        mUserInfoTopViewMvc.registerListener(new RequestRelatedUserInfoViewMvc.RequestRelatedUserInfoViewMvcListener() {
            @Override
            public void onVoteUpClicked() {
                mPresentationStrategy.onTopUserVoteUpClicked();
            }

            @Override
            public void onVoteDownClicked() {
                mPresentationStrategy.onTopUserVoteDownClicked();
            }
        });


        // "BOTTOM" MVC sub-view
        mUserInfoBottomViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                null,
                mImageViewPictureLoader);
        
        mUserInfoBottomViewMvc.registerListener(new RequestRelatedUserInfoViewMvc.RequestRelatedUserInfoViewMvcListener() {
            @Override
            public void onVoteUpClicked() {
                mPresentationStrategy.onBottomUserVoteUpClicked();
            }

            @Override
            public void onVoteDownClicked() {
                mPresentationStrategy.onBottomUserVoteDownClicked();
            }
        });

        initialize();
    }

    @SuppressLint("CutPasteId")
    private void initialize() {

        mFrameUserInfoTop = (FrameLayout) getRootView().findViewById(R.id.frame_user_info_top); 
        mFrameUserInfoTop.addView(mUserInfoTopViewMvc.getRootView());

        mFrameUserInfoBottom = (FrameLayout) getRootView().findViewById(R.id.frame_user_info_bottom);
        mFrameUserInfoBottom.addView(mUserInfoBottomViewMvc.getRootView());
        
        mTxtStatus = (TextView) getRootView().findViewById(R.id.txt_request_status);

        mTxtTopUserTitle = (TextView) getRootView().findViewById(R.id.txt_top_user_title);
        mTxtBottomUserTitle = (TextView) getRootView().findViewById(R.id.txt_bottom_user_title);

        mFrameUserInfoTop = (FrameLayout) getRootView().findViewById(R.id.frame_user_info_top);
        mFrameUserInfoBottom = (FrameLayout) getRootView().findViewById(R.id.frame_user_info_bottom);


        // "Top pictures" views
        mImgTopPictures = (ImageView) getRootView().findViewById(R.id.imgTopPictures);

        // Fine location view
        mTxtFineLocation = (TextView) getRootView().findViewById(R.id.txt_request_fine_location);
        mTxtLocationTitle = (TextView) getRootView().findViewById(R.id.txt_location_title);

        
        // "Closed pictures" views
        mImgBottomPictures = (ImageView) getRootView().findViewById(R.id.imgBottomPictures);

        // The map
        mMapPreview = (MapView) getRootView().findViewById(R.id.map_preview);

        mBtnPickUpRequest = (Button) getRootView().findViewById(R.id.btn_pickup_request);
        mBtnPickUpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onPickupRequestClicked();
                }
            }
        });
        mBtnCloseRequest = (Button) getRootView().findViewById(R.id.btn_close_request);
        mBtnCloseRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onCloseRequestClicked();
                }
            }
        });

    }


    @Override
    public Bundle getViewState() {
        return null;
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


    /**
     * This is a base class for objects that will encapsulate the differentiation logic
     * between requests of different status
     */
    private abstract class PresentationStrategy {

        private RequestItem mRequestItemCopy;

        protected RequestItem getRequestItem() {
            return mRequestItemCopy;
        }

        private void bindRequestItem(RequestItem request) {
            beforeBindRequestItem();

            mRequestItemCopy = RequestItem.create(request); // copy in order to prevent accidental changes

            mTxtStatus.setBackgroundColor(mResources.getColor(getStatusColorResId()));
            mTxtStatus.setText(getStatusString());

            if (TextUtils.isEmpty(getTopUserTitle())) {
                mTxtTopUserTitle.setVisibility(View.GONE);
            } else {
                mTxtTopUserTitle.setVisibility(View.VISIBLE);
                mTxtTopUserTitle.setText(getTopUserTitle());
            }

            mUserInfoTopViewMvc.bindDate(getTopUserDate());
            
            if (getTopUserVotes() == null || getTopUserVotes().length() == 0) {
                mUserInfoTopViewMvc.setVotesVisible(false);
            } else {
                mUserInfoTopViewMvc.setVotesVisible(true);
                mUserInfoTopViewMvc.bindVotes(getTopUserVotes());
            }
            
            if (TextUtils.isEmpty(getTopUserComment())) {
                mUserInfoTopViewMvc.setCommentVisible(false);
            } else {
                mUserInfoTopViewMvc.setCommentVisible(true);
                mUserInfoTopViewMvc.bindComment(getTopUserComment());
            }

            String[] mTopPictures = getTopPictures();
            if (mTopPictures == null || mTopPictures.length == 0) {
                mImgTopPictures.setVisibility(View.GONE);
            } else {
                mImageViewPictureLoader.loadFromWebOrFile(mImgTopPictures, mTopPictures[0],
                        R.drawable.ic_default_user_picture);
            }

            if (showCloseRequestButton()) {
                mBtnCloseRequest.setVisibility(View.VISIBLE);
            } else {
                mBtnCloseRequest.setVisibility(View.GONE);
            }


            mTxtBottomUserTitle.setText(getBottomUserTitle());

            mUserInfoBottomViewMvc.bindDate(getBottomUserDate());
            if (getBottomUserVotes() == null || getBottomUserVotes().length() == 0) {
                mUserInfoBottomViewMvc.setVotesVisible(false);
            } else {
                mUserInfoBottomViewMvc.setVotesVisible(true);
                mUserInfoBottomViewMvc.bindVotes(getBottomUserVotes());
            }
            
            if (TextUtils.isEmpty(getBottomUserComment())) {
                mUserInfoBottomViewMvc.setCommentVisible(false);
            } else {
                mUserInfoBottomViewMvc.setCommentVisible(true);
                mUserInfoBottomViewMvc.bindComment(getBottomUserComment());
            }

            String[] mBottomPictures = getBottomPictures();
            if (mBottomPictures == null || mBottomPictures.length == 0) {
                mImgBottomPictures.setVisibility(View.GONE);
            } else {
                mImageViewPictureLoader.loadFromWebOrFile(mImgBottomPictures, mBottomPictures[0],
                        R.drawable.ic_default_user_picture);
            }

            if (mRequestItem.getLocation() == null || mRequestItem.getLocation().length() == 0) {
                mTxtLocationTitle.setVisibility(View.GONE);
                mTxtFineLocation.setVisibility(View.GONE);
            } else {
                mTxtLocationTitle.setVisibility(View.VISIBLE);
                mTxtFineLocation.setVisibility(View.VISIBLE);
                mTxtFineLocation.setText(mRequestItem.getLocation());
            }

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

            afterBindRequestItem();
        }


        abstract void bindCreatedByUser(UserItem user);
        abstract void bindPickedUpByUser(UserItem user);
        abstract void bindClosedByUser(UserItem user);


        protected abstract void beforeBindRequestItem();
        protected abstract void afterBindRequestItem();

        protected abstract @ColorRes int getStatusColorResId();
        protected abstract String getStatusString();

        protected abstract String getTopUserTitle();
        protected abstract String[] getTopPictures();
        protected abstract String getTopUserComment();
        protected abstract String getTopUserVotes();
        protected abstract String getTopUserDate();

        protected abstract String getBottomUserDate();
        protected abstract String getBottomUserVotes();
        protected abstract String getBottomUserComment();
        protected abstract String[] getBottomPictures();
        protected abstract String getBottomUserTitle();

        protected abstract boolean showPickUpRequestButton();
        protected abstract boolean showCloseRequestButton();

        protected abstract void onTopUserVoteUpClicked();
        protected abstract void onTopUserVoteDownClicked();

        protected abstract void onBottomUserVoteUpClicked();
        protected abstract void onBottomUserVoteDownClicked();
    }

    private class NewPresentationStrategy extends PresentationStrategy {

        @Override
        void bindCreatedByUser(UserItem user) {
            mUserInfoTopViewMvc.bindUser(user);
        }

        @Override
        void bindPickedUpByUser(UserItem user) {
            throw new UnsupportedOperationException("new requests can't have 'picked up by' user");
        }

        @Override
        void bindClosedByUser(UserItem user) {
            throw new UnsupportedOperationException("new requests can't have 'closed by' user");
        }

        @Override
        protected void beforeBindRequestItem() {
            mFrameUserInfoTop.setVisibility(View.VISIBLE);
            mFrameUserInfoBottom.setVisibility(View.GONE);
        }

        @Override
        protected void afterBindRequestItem() {
        }

        @Override
        protected int getStatusColorResId() {
            return R.color.new_request_color;
        }

        @Override
        protected String getStatusString() {
            return mContext.getString(R.string.txt_new_request_title);
        }

        @Override
        protected String getTopUserTitle() {
            return null;
        }

        @Override
        protected String[] getTopPictures() {
            return getRequestItem().getCreatedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        }

        @Override
        protected String getTopUserComment() {
            return getRequestItem().getCreatedComment();
        }

        @Override
        protected String getTopUserVotes() {
            return String.valueOf(getRequestItem().getCreatedVotes());
        }

        @Override
        protected String getTopUserDate() {
            return getRequestItem().getCreatedAt();
        }

        @Override
        protected String getBottomUserDate() {
            return null;
        }

        @Override
        protected String getBottomUserVotes() {
            return null;
        }

        @Override
        protected String getBottomUserComment() {
            return null;
        }

        @Override
        protected String[] getBottomPictures() {
            return new String[0];
        }

        @Override
        protected String getBottomUserTitle() {
            return null;
        }

        @Override
        protected boolean showPickUpRequestButton() {
            return true;
        }

        @Override
        protected boolean showCloseRequestButton() {
            return false;
        }

        @Override
        protected void onTopUserVoteUpClicked() {
            for (RequestDetailsViewMvcListener listener : getListeners()) {
                listener.onCreatedVoteUpClicked();
            }
        }

        @Override
        protected void onTopUserVoteDownClicked() {
            for (RequestDetailsViewMvcListener listener : getListeners()) {
                listener.onCreatedVoteDownClicked();
            }
        }

        @Override
        protected void onBottomUserVoteUpClicked() {
           throw new RuntimeException("should not show bottom votes in new request");
        }

        @Override
        protected void onBottomUserVoteDownClicked() {
            throw new RuntimeException("should not show bottom votes in new request");
        }
    }

    private class PickedUpPresentationStrategy extends PresentationStrategy {

        @Override
        void bindCreatedByUser(UserItem user) {
            mUserInfoBottomViewMvc.bindUser(user);
        }

        @Override
        void bindPickedUpByUser(UserItem user) {
            mUserInfoTopViewMvc.bindUser(user);
        }

        @Override
        void bindClosedByUser(UserItem user) {
            throw new UnsupportedOperationException("picked up requests can't have 'closed by' user");
        }

        @Override
        protected void beforeBindRequestItem() {
            mFrameUserInfoTop.setVisibility(View.VISIBLE);
            mFrameUserInfoBottom.setVisibility(View.VISIBLE);
        }

        @Override
        protected void afterBindRequestItem() {

        }

        @Override
        protected int getStatusColorResId() {
            return R.color.picked_up_request_color;
        }

        @Override
        protected String getStatusString() {
            return mContext.getString(R.string.txt_picked_up_request_title);
        }

        @Override
        protected String getTopUserTitle() {
            return mContext.getString(R.string.txt_picked_up_by_title);
        }

        @Override
        protected String[] getTopPictures() {
            return getRequestItem().getCreatedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        }

        @Override
        protected String getTopUserComment() {
            return null;
        }

        @Override
        protected String getTopUserVotes() {
            return null;
        }

        @Override
        protected String getTopUserDate() {
            return mRequestItem.getPickedUpAt();
        }

        @Override
        protected String getBottomUserDate() {
            return mRequestItem.getCreatedAt();
        }

        @Override
        protected String getBottomUserVotes() {
            return String.valueOf(getRequestItem().getCreatedVotes());
        }

        @Override
        protected String getBottomUserComment() {
            return getRequestItem().getCreatedComment();
        }

        @Override
        protected String[] getBottomPictures() {
            return new String[0];
        }

        @Override
        protected String getBottomUserTitle() {
            return mContext.getString(R.string.txt_created_by_title);
        }

        @Override
        protected boolean showPickUpRequestButton() {
            return false;
        }

        @Override
        protected boolean showCloseRequestButton() {
            return getRequestItem().getStatus() == RequestItem.RequestStatus.PICKED_UP_BY_ME;
        }

        @Override
        protected void onTopUserVoteUpClicked() {
            throw new RuntimeException("should not show top votes in picked up request");
        }

        @Override
        protected void onTopUserVoteDownClicked() {
            throw new RuntimeException("should not show top votes in picked up request");
        }

        @Override
        protected void onBottomUserVoteUpClicked() {
            for (RequestDetailsViewMvcListener listener : getListeners()) {
                listener.onCreatedVoteUpClicked();
            }
        }

        @Override
        protected void onBottomUserVoteDownClicked() {
            for (RequestDetailsViewMvcListener listener : getListeners()) {
                listener.onCreatedVoteDownClicked();
            }
        }

    }

    private class ClosedPresentationStrategy extends PresentationStrategy {

        @Override
        void bindCreatedByUser(UserItem user) {
            mUserInfoBottomViewMvc.bindUser(user);
        }

        @Override
        void bindPickedUpByUser(UserItem user) {
            // no-op - this user should be identical to "closed by"
        }

        @Override
        void bindClosedByUser(UserItem user) {
            mUserInfoTopViewMvc.bindUser(user);
        }

        @Override
        protected void beforeBindRequestItem() {
            mFrameUserInfoTop.setVisibility(View.VISIBLE);
            mFrameUserInfoBottom.setVisibility(View.VISIBLE);
        }

        @Override
        protected void afterBindRequestItem() {

        }

        @Override
        protected int getStatusColorResId() {
            return R.color.closed_request_color;
        }

        @Override
        protected String getStatusString() {
            return mContext.getString(R.string.txt_closed_request_title);
        }

        @Override
        protected String getTopUserTitle() {
            return mContext.getString(R.string.txt_closed_by_title);
        }

        @Override
        protected String[] getTopPictures() {
            return getRequestItem().getClosedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        }

        @Override
        protected String getTopUserComment() {
            return getRequestItem().getClosedComment();
        }

        @Override
        protected String getTopUserVotes() {
            return String.valueOf(getRequestItem().getClosedVotes());
        }

        @Override
        protected String getTopUserDate() {
            return getRequestItem().getClosedAt();
        }

        @Override
        protected String getBottomUserDate() {
            return getRequestItem().getCreatedAt();
        }

        @Override
        protected String getBottomUserVotes() {
            return String.valueOf(getRequestItem().getCreatedVotes());
        }

        @Override
        protected String getBottomUserComment() {
            return getRequestItem().getCreatedComment();
        }

        @Override
        protected String[] getBottomPictures() {
            return getRequestItem().getClosedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        }

        @Override
        protected String getBottomUserTitle() {
            return mContext.getString(R.string.txt_created_by_title);
        }

        @Override
        protected boolean showPickUpRequestButton() {
            return false;
        }

        @Override
        protected boolean showCloseRequestButton() {
            return false;
        }

        @Override
        protected void onTopUserVoteUpClicked() {
            for (RequestDetailsViewMvcListener listener : getListeners()) {
                listener.onClosedVoteUpClicked();
            }
        }

        @Override
        protected void onTopUserVoteDownClicked() {
            for (RequestDetailsViewMvcListener listener : getListeners()) {
                listener.onClosedVoteDownClicked();
            }
        }

        @Override
        protected void onBottomUserVoteUpClicked() {
            for (RequestDetailsViewMvcListener listener : getListeners()) {
                listener.onCreatedVoteUpClicked();
            }
        }

        @Override
        protected void onBottomUserVoteDownClicked() {
            for (RequestDetailsViewMvcListener listener : getListeners()) {
                listener.onCreatedVoteDownClicked();
            }
        }
    }

}
