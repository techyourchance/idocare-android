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

/**
 * MVC View for New Request screen.
 */
public class RequestDetailsViewMVC extends AbstractViewMVC {

    private final static String LOG_TAG = "RequestDetailsViewMVC";


    Context mContext;
    View mRootView;
    RequestItem mRequestItem;
    RequestStatus mRequestStatus;


    TextView mTxtStatus;
    TextView mTxtCoarseLocation;
    TextView mTxtFineLocation;
    
    TextView mTxtCreatedByTitle;
    ImageView mImgCreatedByPicture;
    TextView mTxtCreatedByNickname;
    TextView mTxtCreatedAt;
    TextView mTxtCreatedByReputation;
    TextView mTxtCreatedVotes;
    TextView mTxtCreatedComment;
    ImageView[] mImgCreatedPictures;

    TextView mTxtClosedByTitle;
    ImageView mImgClosedByPicture;
    TextView mTxtClosedByNickname;
    TextView mTxtClosedAt;
    TextView mTxtClosedByReputation;
    TextView mTxtClosedVotes;
    TextView mTxtClosedComment;
    ImageView[] mImgClosedPictures;

    Button mBtnPickUpRequest;
    Button mBtnCloseRequest;


    public RequestDetailsViewMVC(Context context, ViewGroup container, Bundle savedInstanceState) {
        mContext = context;
        mRootView = LayoutInflater.from(mContext)
                .inflate(R.layout.fragment_request_details, container, false);

        findAllViews();

    }


    @Override
    protected void handleMessage(Message msg) {
        // TODO: complete this method
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
        mTxtCreatedByTitle = (TextView) includedView.findViewById(R.id.txt_title);
        mImgCreatedByPicture = (ImageView) includedView.findViewById(R.id.img_user_picture);
        mTxtCreatedByNickname = (TextView) includedView.findViewById(R.id.txt_user_nickname);
        mTxtCreatedAt = (TextView) includedView.findViewById(R.id.txt_date);
        mTxtCreatedByReputation = (TextView) includedView.findViewById(R.id.txt_user_reputation);
        mTxtCreatedVotes = (TextView) includedView.findViewById(R.id.txt_votes);
        mTxtCreatedComment = (TextView) includedView.findViewById(R.id.txt_comment);

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
        mTxtClosedAt = (TextView) includedView.findViewById(R.id.txt_date);
        mTxtClosedByReputation = (TextView) includedView.findViewById(R.id.txt_user_reputation);
        mTxtClosedVotes = (TextView) includedView.findViewById(R.id.txt_votes);
        mTxtClosedComment = (TextView) includedView.findViewById(R.id.txt_comment);

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
     * Decide which of the Android Views in this MVC View should be visible and populate them with
     * data from the RequestItem object.
     * @param requestItem request item to take teh data from
     */
    public void populateChildViewsFromRequestItem(RequestItem requestItem) {

        mRequestItem = requestItem;
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



    /**
     * Handle the status bar text and color
     */
    private void populateStatusBarFromRequestItem() {

        int statusColor;
        String statusText;

        switch (mRequestStatus) {
            case NEW_BY_OTHER:
                statusColor = mContext.getResources().getColor(R.color.new_request_status);
                statusText = mContext.getResources().getString(R.string.txt_new_request_title);
                break;
            case NEW_BY_ME:
                statusColor = mContext.getResources().getColor(R.color.new_request_status);
                statusText = mContext.getResources().getString(R.string.txt_new_request_title);
                break;
            case PICKED_UP_BY_OTHER:
                statusColor = mContext.getResources().getColor(R.color.picked_up_request_status);
                statusText = mContext.getResources().getString(R.string.txt_picked_up_request_title);
                break;
            case PICKED_UP_BY_ME:
                statusColor = mContext.getResources().getColor(R.color.picked_up_request_status);
                statusText = mContext.getResources().getString(R.string.txt_picked_up_request_title);
                break;
            case CLOSED_BY_OTHER:
                statusColor = mContext.getResources().getColor(R.color.closed_request_status);
                statusText = mContext.getResources().getString(R.string.txt_closed_request_title);
                break;
            case CLOSED_BY_ME:
                statusColor = mContext.getResources().getColor(R.color.closed_request_status);
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
        mTxtCreatedByTitle.setText(R.string.txt_created_by_title);
        mImgCreatedByPicture.setImageResource(R.drawable.ic_launcher);
        mTxtCreatedByNickname.setText(mRequestItem.getCreatedBy().getNickname());
        mTxtCreatedAt.setText(mRequestItem.getCreatedAt());
        mTxtCreatedByReputation.setText("REPUTATION");
        mTxtCreatedVotes.setText("VOTE");

        if (mRequestItem.getCreatedComment() == null ||
                mRequestItem.getCreatedComment().isEmpty()) {
            mTxtCreatedComment.setVisibility(View.GONE);
        } else {
            mTxtCreatedComment.setVisibility(View.VISIBLE);
            mTxtCreatedComment.setText(mRequestItem.getCreatedComment());
        }

        for (int i=0; i<3; i++) {
            if (mRequestItem.getCreatedPictures() != null &&
                    mRequestItem.getCreatedPictures().length > i) {
                ImageLoader.getInstance().displayImage(
                        mRequestItem.getCreatedPictures()[i],
                        mImgCreatedPictures[i],
                        Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
            } else {
                mImgCreatedPictures[i].setImageResource(R.drawable.ic_picture_placeholder_small);

            }
        }

        mTxtFineLocation.setText("TODO fine loc");

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
        mImgClosedByPicture.setImageResource(R.drawable.ic_launcher);
        mTxtClosedByNickname.setText(mRequestItem.getClosedBy().getNickname());
        mTxtClosedAt.setText(mRequestItem.getClosedAt());
        mTxtClosedByReputation.setText("REPUTATION");
        mTxtClosedVotes.setText("VOTE");

        if (mRequestItem.getClosedComment() == null ||
                mRequestItem.getClosedComment().isEmpty()) {
            mTxtClosedComment.setVisibility(View.GONE);
        } else {
            mTxtClosedComment.setVisibility(View.VISIBLE);
            mTxtClosedComment.setText(mRequestItem.getClosedComment());
        }
        
        
        for (int i=0; i<3; i++) {
            if (mRequestItem.getClosedPictures() != null &&
                    mRequestItem.getClosedPictures().length > i) {
                ImageLoader.getInstance().displayImage(
                        mRequestItem.getClosedPictures()[i],
                        mImgClosedPictures[i],
                        Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
            } else {
                mImgClosedPictures[i].setImageResource(R.drawable.ic_picture_placeholder_small);

            }
        }

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
            mBtnPickUpRequest.setText("Assigned to " + mRequestItem.getPickedUpBy().getNickname());
        }
        else {
            mBtnPickUpRequest.setVisibility(View.GONE);
        }

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

        if (mRequestItem.getClosedBy() != null) {
            if (userLoggedIn && (userId == mRequestItem.getClosedBy().getId()))
                mRequestStatus = RequestStatus.CLOSED_BY_ME;
            else
                mRequestStatus = RequestStatus.CLOSED_BY_OTHER;
        }
        else if (mRequestItem.getPickedUpBy() != null) {
            if (userLoggedIn && (userId == mRequestItem.getPickedUpBy().getId()))
                mRequestStatus = RequestStatus.PICKED_UP_BY_ME;
            else
                mRequestStatus = RequestStatus.PICKED_UP_BY_OTHER;
        }
        else if (mRequestItem.getCreatedPictures() != null) {
            if (userLoggedIn && (userId == mRequestItem.getCreatedBy().getId()))
                mRequestStatus = RequestStatus.NEW_BY_ME;
            else
                mRequestStatus = RequestStatus.NEW_BY_OTHER;
        } else {
            // TODO: some error
        }
    }


}
