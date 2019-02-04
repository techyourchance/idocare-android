package il.co.idocarerequests.screens.requestdetails.mvcviews;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import il.co.idocarecore.pictures.ImageViewPictureLoader;
import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.screens.common.mvcviews.AbstractViewMvc;
import il.co.idocarecore.screens.common.widgets.SwipeImageGalleryView;
import il.co.idocarecore.users.UserEntity;
import il.co.idocarerequests.R;

/**
 * MVC View for New Request screen.
 */
public class RequestDetailsViewMvcImpl
        extends AbstractViewMvc<RequestDetailsViewMvc.RequestDetailsViewMvcListener>
        implements RequestDetailsViewMvc {

    private final ImageViewPictureLoader mImageViewPictureLoader;
    private Context mContext;
    private Resources mResources;

    private PresentationStrategy mPresentationStrategy;

    private FrameLayout mFrameUserInfoTop;
    private FrameLayout mFrameUserInfoBottom;
    private FrameLayout mFrameLocationInfo;

    private RequestRelatedUserInfoViewMvc mUserInfoTopViewMvc;
    private RequestRelatedUserInfoViewMvc mUserInfoBottomViewMvc;

    private LocationInfoViewMvc mLocationInfoViewMvc;

    private RequestEntity mRequest;

    private TextView mTxtStatus;
    private TextView mTxtTopUserTitle;
    private SwipeImageGalleryView mSwipeImageGalleryTop;
    private TextView mTxtBottomUserTitle;
    private SwipeImageGalleryView mSwipeImageGalleryBottom;

    private Button mBtnPickUpRequest;
    private Button mBtnCloseRequest;
    private String mCurrentUserId;



    public RequestDetailsViewMvcImpl(@NonNull LayoutInflater inflater,
                                     @Nullable ViewGroup container,
                                     @NonNull ImageViewPictureLoader imageViewPictureLoader) {
        setRootView(inflater.inflate(R.layout.layout_request_details, container, false));
        mImageViewPictureLoader = imageViewPictureLoader;
        mContext = inflater.getContext();
        mResources = mContext.getResources();

        initialize();
        registerListeners();
    }

    private void initialize() {

        // "Location info" MVC sub-view
        mLocationInfoViewMvc = new LocationInfoViewMvcImpl(LayoutInflater.from(mContext), null);

        // "TOP" MVC sub-view
        mUserInfoTopViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                null,
                mImageViewPictureLoader);

        // "BOTTOM" MVC sub-view
        mUserInfoBottomViewMvc = new RequestRelatedUserInfoViewMvc(
                LayoutInflater.from(mContext),
                null,
                mImageViewPictureLoader);

        mFrameLocationInfo = (FrameLayout) getRootView().findViewById(R.id.frame_location_info);
        mFrameLocationInfo.addView(mLocationInfoViewMvc.getRootView());

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
        mSwipeImageGalleryTop = (SwipeImageGalleryView) getRootView().findViewById(R.id.swipeImageGalleryTop);

        // "Closed pictures" views
        mSwipeImageGalleryBottom = (SwipeImageGalleryView) getRootView().findViewById(R.id.swipeImageGalleryBottom);

        mBtnPickUpRequest = (Button) getRootView().findViewById(R.id.btn_pickup_request);

        mBtnCloseRequest = (Button) getRootView().findViewById(R.id.btn_close_request);
    }


    private void registerListeners() {

        mLocationInfoViewMvc.registerListener(new LocationInfoViewMvc.LocationInfoViewMvcListener() {
            @Override
            public void onMapClicked() {
                // TODO: show full screen map
            }
        });

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

        mBtnPickUpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestDetailsViewMvcListener listener : getListeners()) {
                    listener.onPickupRequestClicked();
                }
            }
        });

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

    @Override
    public void bindRequest(RequestEntity request) {

        mRequest = request;

        if (mRequest.isClosed()) {
            mPresentationStrategy = new ClosedPresentationStrategy();
        } else if (mRequest.isPickedUp()) {
            mPresentationStrategy = new PickedUpPresentationStrategy();
        } else {
            mPresentationStrategy = new NewPresentationStrategy();
        }

        mPresentationStrategy.bindRequest(request);

    }

    @Override
    public void bindCurrentUserId(String currentUserId) {
        mCurrentUserId = currentUserId;
    }

    @Override
    public void bindCreatedByUser(UserEntity user) {
        mPresentationStrategy.bindCreatedByUser(user);
    }


    @Override
    public void bindClosedByUser(UserEntity user) {
        mPresentationStrategy.bindClosedByUser(user);
    }


    @Override
    public void bindPickedUpByUser(UserEntity user) {
        mPresentationStrategy.bindPickedUpByUser(user);
    }


    /**
     * This is a base class for objects that will encapsulate the differentiation logic
     * between requests of different status
     */
    private abstract class PresentationStrategy {

        protected RequestEntity getRequestItem() {
            return mRequest;
        }

        private void bindRequest(RequestEntity request) {
            beforeBindRequestItem();

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

            mSwipeImageGalleryTop.clear();
            List<String> mTopPictures = getTopPictures();
            if (mTopPictures == null || mTopPictures.isEmpty()) {
                mSwipeImageGalleryTop.setVisibility(View.GONE);
            } else {
                mSwipeImageGalleryTop.setVisibility(View.VISIBLE);
                mSwipeImageGalleryTop.addPictures(mTopPictures);
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

            mSwipeImageGalleryBottom.clear();
            List<String> mBottomPictures = getBottomPictures();
            if (mBottomPictures == null || mBottomPictures.isEmpty()) {
                mSwipeImageGalleryBottom.setVisibility(View.GONE);
            } else {
                mSwipeImageGalleryBottom.setVisibility(View.VISIBLE);
                mSwipeImageGalleryBottom.addPictures(mBottomPictures);
            }

            bindLocationFields(request);

            if (showPickUpRequestButton()) {
                mBtnPickUpRequest.setVisibility(View.VISIBLE);
            } else {
                mBtnPickUpRequest.setVisibility(View.GONE);
            }

            afterBindRequestItem();
        }

        private void bindLocationFields(RequestEntity request) {
            mLocationInfoViewMvc.setLocationString(request.getLocation());

            mLocationInfoViewMvc.setLocation(
                    mRequest.getLatitude(), mRequest.getLongitude());
        }


        abstract void bindCreatedByUser(UserEntity user);
        abstract void bindPickedUpByUser(UserEntity user);
        abstract void bindClosedByUser(UserEntity user);


        protected abstract void beforeBindRequestItem();
        protected abstract void afterBindRequestItem();

        protected abstract @ColorRes int getStatusColorResId();
        protected abstract String getStatusString();

        protected abstract String getTopUserTitle();
        protected abstract List<String> getTopPictures();
        protected abstract String getTopUserComment();
        protected abstract String getTopUserVotes();
        protected abstract String getTopUserDate();

        protected abstract String getBottomUserDate();
        protected abstract String getBottomUserVotes();
        protected abstract String getBottomUserComment();
        protected abstract List<String> getBottomPictures();
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
        void bindCreatedByUser(UserEntity user) {
            mUserInfoTopViewMvc.bindUser(user);
        }

        @Override
        void bindPickedUpByUser(UserEntity user) {
            throw new UnsupportedOperationException("new requests can't have 'picked up by' user");
        }

        @Override
        void bindClosedByUser(UserEntity user) {
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
        protected List<String> getTopPictures() {
            return getRequestItem().getCreatedPictures();
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
        protected List<String> getBottomPictures() {
            return new ArrayList<>(0);
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
        void bindCreatedByUser(UserEntity user) {
            mUserInfoBottomViewMvc.bindUser(user);
        }

        @Override
        void bindPickedUpByUser(UserEntity user) {
            mUserInfoTopViewMvc.bindUser(user);
        }

        @Override
        void bindClosedByUser(UserEntity user) {
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
        protected List<String> getTopPictures() {
            return getRequestItem().getCreatedPictures();
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
            return mRequest.getPickedUpAt();
        }

        @Override
        protected String getBottomUserDate() {
            return mRequest.getCreatedAt();
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
        protected List<String> getBottomPictures() {
            return new ArrayList<>(0);
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
            String pickedUpBy = getRequestItem().getPickedUpBy();
            return pickedUpBy != null && pickedUpBy.equals(mCurrentUserId);
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
        void bindCreatedByUser(UserEntity user) {
            mUserInfoBottomViewMvc.bindUser(user);
        }

        @Override
        void bindPickedUpByUser(UserEntity user) {
            // no-op - this user should be identical to "closed by"
        }

        @Override
        void bindClosedByUser(UserEntity user) {
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
        protected List<String> getTopPictures() {
            return getRequestItem().getClosedPictures();
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
        protected List<String> getBottomPictures() {
            return getRequestItem().getCreatedPictures();
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
