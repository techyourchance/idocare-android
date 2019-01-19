package il.co.idocare.mvcviews.userinfo;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMvc;
import il.co.idocare.mvcviews.ViewMvc;
import il.co.idocare.pictures.ImageViewPictureLoader;
import il.co.idocare.users.UserEntity;

public class RequestRelatedUserInfoViewMvc
        extends AbstractViewMvc<RequestRelatedUserInfoViewMvc.RequestRelatedUserInfoViewMvcListener>
        implements ViewMvc {

    @NonNull
    private final ImageViewPictureLoader mImageViewPictureLoader;



    public interface RequestRelatedUserInfoViewMvcListener {
        void onVoteUpClicked();
        void onVoteDownClicked();
    }

    private ImageView mImgUserPicture;
    private TextView mTxtUserNickname;
    private TextView mTxtDate;
    private TextView mTxtUserReputation;
    private TextView mTxtComment;
    private TextView mTxtVotes;
    private View mViewVoteUp;
    private View mViewVoteDown;
    private View mViewVotePanel;


    public RequestRelatedUserInfoViewMvc(@NonNull LayoutInflater inflater,
                                         @Nullable ViewGroup container,
                                         @NonNull ImageViewPictureLoader imageViewPictureLoader) {
        mImageViewPictureLoader = imageViewPictureLoader;
        setRootView(inflater.inflate(R.layout.element_request_related_user_info, container, false));
        initialize();
    }

    private void initialize() {
        mImgUserPicture = (ImageView) getRootView().findViewById(R.id.img_user_picture);
        mTxtUserNickname = (TextView) getRootView().findViewById(R.id.txt_user_nickname);
        mTxtDate = (TextView) getRootView().findViewById(R.id.txt_custom_info);
        mTxtUserReputation = (TextView) getRootView().findViewById(R.id.txt_user_reputation);
        mTxtComment = (TextView) getRootView().findViewById(R.id.txt_comment);
        mTxtVotes = (TextView) getRootView().findViewById(R.id.txt_votes);
        mViewVoteUp = findViewById(R.id.view_vote_up);
        mViewVoteDown = findViewById(R.id.view_vote_down);
        mViewVotePanel = getRootView().findViewById(R.id.element_votes);

        mViewVoteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestRelatedUserInfoViewMvcListener listener : getListeners()) {
                    listener.onVoteUpClicked();
                }
            }
        });

        mViewVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestRelatedUserInfoViewMvcListener listener : getListeners()) {
                    listener.onVoteDownClicked();
                }
            }
        });
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    public void bindUser(UserEntity userItem) {

        mTxtUserNickname.setText(userItem.getNickname());

        mImageViewPictureLoader.loadFromWebOrFile(mImgUserPicture, userItem.getPictureUrl(),
                R.drawable.ic_default_user_picture);

        mTxtUserReputation.setText(String.valueOf(userItem.getReputation()));
    }

    public void bindDate(String customInfo) {
        mTxtDate.setText(customInfo);
    }


    public void setCommentVisible(boolean visible) {
        if (visible)
            mTxtComment.setVisibility(View.VISIBLE);
        else
            mTxtComment.setVisibility(View.GONE);
    }


    public void setVotesVisible(boolean visible) {
        if (visible) {
            mViewVotePanel.setVisibility(View.VISIBLE);
        } else {
            mViewVotePanel.setVisibility(View.GONE);
        }
    }

    public void bindComment(String comment) {
        mTxtComment.setText(comment);
    }

    public void bindVotes(String votes) {
        mTxtVotes.setText(votes);
    }
}
