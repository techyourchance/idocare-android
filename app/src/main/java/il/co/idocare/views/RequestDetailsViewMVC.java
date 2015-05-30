package il.co.idocare.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.pojos.RequestItem.RequestStatus;
import il.co.idocare.pojos.UserItem;

/**
 * MVC View for New Request screen.
 */
public class RequestDetailsViewMVC extends AbstractViewMVC {

    private final static String LOG_TAG = "RequestDetailsViewMVC";

    private final Object LOCK = new Object();

    private Context mContext;

    private RequestItem mRequestItem;
    private RequestStatus mRequestStatus;


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

    private Button mBtnPickUpRequest;
    private Button mBtnCloseRequest;


    public RequestDetailsViewMVC(Context context, ViewGroup container, Bundle savedInstanceState) {
        mContext = context;

        mRootView = LayoutInflater.from(mContext)
                .inflate(R.layout.fragment_request_details, container, false);

        findAllViews();

    }


    @Override
    protected void handleMessage(Message msg) {
        switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {
            case M_USER_DATA_UPDATE:
                // The assumption here is that update to the data of the user can't change the
                // status of the request, but just the details of this particular user (e.g. reputation)
                long userId = ((Long)msg.obj);
                synchronized (LOCK) {
                    if (mRequestItem.getCreatedBy() != 0 && mRequestItem.getCreatedBy() == userId) {
                        updateCreatedByUser();
                    }
                    else if (mRequestItem.getPickedUpBy() != 0 && mRequestItem.getPickedUpBy() == userId) {
                        updatePickedUpByUser();
                    }
                    else if (mRequestItem.getClosedBy() != 0 && mRequestItem.getClosedBy() == userId) {
                        updateClosedByUser();
                    }
                }
                break;


            default:
                break;
        }
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
    private void findAllViews() {
        View includedView;

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

        // "Close" button
        mBtnPickUpRequest = (Button) mRootView.findViewById(R.id.btn_pickup_request);
        mBtnCloseRequest = (Button) mRootView.findViewById(R.id.btn_close_request);

    }


    /**
     *
     * Show the details of the request
     * @param requestId ID of the request that should be shown
     */
    public void showRequest(long requestId) {
        // This sync prevents concurrent change of mRequestItem
        synchronized (LOCK) {

            setRequestStatus();

            // Handle the status bar
            populateStatusBarFromRequestItem();
            // Handle the views related to initial request
            populateCreatedViewsFromRequestItem();
            // Handle the views related to pickup info
            populateClosedViewsFromRequestItem();
            // Handle the pickup button functionality
            populatePickupButtonFromRequestItem();
            // Handle the close button functionality
            populateCloseButtonFromRequestItem();
        }
    }



    /**
     * Handle the status bar text and color
     */
    private void populateStatusBarFromRequestItem() {

        int statusColor;
        String statusText;

        switch (mRequestStatus) {
            case NEW_BY_OTHER:
                statusColor = mContext.getResources().getColor(R.color.new_request_color);
                statusText = mContext.getResources().getString(R.string.txt_new_request_title);
                break;
            case NEW_BY_ME:
                statusColor = mContext.getResources().getColor(R.color.new_request_color);
                statusText = mContext.getResources().getString(R.string.txt_new_request_title);
                break;
            case PICKED_UP_BY_OTHER:
                statusColor = mContext.getResources().getColor(R.color.picked_up_request_color);
                statusText = mContext.getResources().getString(R.string.txt_picked_up_request_title);
                break;
            case PICKED_UP_BY_ME:
                statusColor = mContext.getResources().getColor(R.color.picked_up_request_color);
                statusText = mContext.getResources().getString(R.string.txt_picked_up_request_title);
                break;
            case CLOSED_BY_OTHER:
                statusColor = mContext.getResources().getColor(R.color.closed_request_color);
                statusText = mContext.getResources().getString(R.string.txt_closed_request_title);
                break;
            case CLOSED_BY_ME:
                statusColor = mContext.getResources().getColor(R.color.closed_request_color);
                statusText = mContext.getResources().getString(R.string.txt_closed_request_title);
                break;
            default:
                statusColor = mContext.getResources().getColor(android.R.color.white);
                statusText = "Error: request status is not set!";
        }

        mTxtStatus.setBackgroundColor(statusColor);
        mTxtStatus.setText(statusText);

        mTxtCoarseLocation.setBackgroundColor(statusColor);
        mTxtCoarseLocation.setText("TODO coarse loc");


    }

    /**
     * Handle the views describing the initial request
     */
    private void populateCreatedViewsFromRequestItem() {


        mTxtCreatedTitle.setText(R.string.txt_created_title);
        mTxtCreatedAt.setText(mRequestItem.getCreatedAt());
        mTxtCreatedReputation.setText(String.valueOf(mRequestItem.getCreatedReputation()));

        if (mRequestItem.getCreatedComment() == null ||
                mRequestItem.getCreatedComment().isEmpty()) {
            mTxtCreatedComment.setVisibility(View.GONE);
        } else {
            mTxtCreatedComment.setVisibility(View.VISIBLE);
            mTxtCreatedComment.setText(mRequestItem.getCreatedComment());
        }

        String[] createdPictures = mRequestItem.getCreatedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        for (int i=0; i<3; i++) {
            if (createdPictures.length > i) {
                ImageLoader.getInstance().displayImage(
                        createdPictures[i],
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
                notifyOutboxHandlers(Constants.MessageType.V_CREATED_VOTE_UP_BUTTON_CLICKED.ordinal(),
                        0, 0, null);
            }
        });

        mImgCreatedVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyOutboxHandlers(Constants.MessageType.V_CREATED_VOTE_DOWN_BUTTON_CLICKED.ordinal(),
                        0, 0, null);
            }
        });

        updateCreatedByUser();

    }

    private void updateCreatedByUser() {
        // TODO: obtain users' data from cache
        UserItem createdByUserItem = UserItem.createUserItem(0);

        mTxtCreatedByNickname.setText(createdByUserItem.getNickname());

        if (createdByUserItem.getPictureUrl() != null) {
            ImageLoader.getInstance().displayImage(
                    createdByUserItem.getPictureUrl(),
                    mImgCreatedByPicture,
                    Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
        } else {
            mImgCreatedByPicture.setImageResource(R.drawable.default_user_picture);
        }

        mTxtCreatedByReputation.setText(String.valueOf(createdByUserItem.getReputation()));
    }

    /**
     * Handle the views describing the info about request's closure
     */
    private void populateClosedViewsFromRequestItem() {
        if (mRequestStatus != RequestStatus.CLOSED_BY_OTHER &&
                mRequestStatus != RequestStatus.CLOSED_BY_ME) {
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

        mTxtClosedByTitle.setText(R.string.txt_closed_by_title);
        mTxtClosedAt.setText(mRequestItem.getClosedAt());
        mTxtClosedReputation.setText(String.valueOf(mRequestItem.getClosedReputation()));

        if (mRequestItem.getClosedComment() == null ||
                mRequestItem.getClosedComment().isEmpty()) {
            mTxtClosedComment.setVisibility(View.GONE);
        } else {
            mTxtClosedComment.setVisibility(View.VISIBLE);
            mTxtClosedComment.setText(mRequestItem.getClosedComment());
        }
        
        String[] closedPictures = mRequestItem.getClosedPictures().split(Constants.PICTURES_LIST_SEPARATOR);
        for (int i=0; i<3; i++) {
            if (closedPictures.length > i) {
                ImageLoader.getInstance().displayImage(
                        closedPictures[i],
                        mImgClosedPictures[i],
                        Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
            } else {
                mImgClosedPictures[i].setVisibility(View.GONE);

            }
        }


        mImgClosedVoteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyOutboxHandlers(Constants.MessageType.V_CLOSED_VOTE_UP_BUTTON_CLICKED.ordinal(),
                        0, 0, null);
            }
        });

        mImgClosedVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyOutboxHandlers(Constants.MessageType.V_CLOSED_VOTE_DOWN_BUTTON_CLICKED.ordinal(),
                        0, 0, null);
            }
        });

        updateClosedByUser();

    }

    private void updateClosedByUser() {
        // TODO: obtain user's data from cache
        UserItem closedByUserItem = UserItem.createUserItem(0);

        mTxtClosedByNickname.setText(closedByUserItem.getNickname());

        if (closedByUserItem.getPictureUrl() != null) {
            ImageLoader.getInstance().displayImage(
                    closedByUserItem.getPictureUrl(),
                    mImgClosedByPicture,
                    Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
        } else {
            mImgClosedByPicture.setImageResource(R.drawable.default_user_picture);
        }

        mTxtClosedByReputation.setText(String.valueOf(closedByUserItem.getReputation()));
    }

    /**
     * Handle the view of the pickup button
     */
    private void populatePickupButtonFromRequestItem() {


        if (mRequestStatus == RequestStatus.NEW_BY_ME ||
                mRequestStatus == RequestStatus.NEW_BY_OTHER) {
            mBtnPickUpRequest.setVisibility(View.VISIBLE);
            mBtnPickUpRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifyOutboxHandlers(Constants.MessageType.V_PICKUP_REQUEST_BUTTON_CLICKED.ordinal(),
                            0, 0, null);
                }
            });
        }
        else if (mRequestStatus == RequestStatus.PICKED_UP_BY_OTHER) {
            mBtnPickUpRequest.setClickable(false);
            updatePickedUpByUser();
        }
        else {
            mBtnPickUpRequest.setVisibility(View.GONE);
        }

    }

    private void updatePickedUpByUser() {
        long pickedUpBy = mRequestItem.getPickedUpBy();
        // TODO: obtain user's data from local cache
        mBtnPickUpRequest.setText("Assigned to " + "SOMEUSER");
    }


    /**
     * Handle the view of the close button
     */
    private void populateCloseButtonFromRequestItem() {

        if (mRequestStatus == RequestStatus.PICKED_UP_BY_ME) {
            mBtnCloseRequest.setVisibility(View.VISIBLE);
            mBtnCloseRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifyOutboxHandlers(Constants.MessageType.V_CLOSE_REQUEST_BUTTON_CLICKED.ordinal(),
                            0, 0, null);
                }
            });
        } else {
            mBtnCloseRequest.setVisibility(View.GONE);
        }

    }

    /**
     * Determine the status of the request based on its contents
     */
    private void setRequestStatus() {

        SharedPreferences prefs =
                mContext.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        boolean userLoggedIn = prefs.contains(Constants.FieldName.USER_ID.getValue());
        long userId = prefs.getLong(Constants.FieldName.USER_ID.getValue(), 0);

        if (mRequestItem.getClosedBy() != 0) {
            if (userLoggedIn && (userId == mRequestItem.getClosedBy()))
                mRequestStatus = RequestStatus.CLOSED_BY_ME;
            else
                mRequestStatus = RequestStatus.CLOSED_BY_OTHER;
        } else if (mRequestItem.getPickedUpBy() != 0) {
            if (userLoggedIn && (userId == mRequestItem.getPickedUpBy()))
                mRequestStatus = RequestStatus.PICKED_UP_BY_ME;
            else
                mRequestStatus = RequestStatus.PICKED_UP_BY_OTHER;
        } else if (mRequestItem.getCreatedBy() != 0) {
            if (userLoggedIn && (userId == mRequestItem.getCreatedBy()))
                mRequestStatus = RequestStatus.NEW_BY_ME;
            else
                mRequestStatus = RequestStatus.NEW_BY_OTHER;
        } else {
            // TODO: some error
        }
    }


}
