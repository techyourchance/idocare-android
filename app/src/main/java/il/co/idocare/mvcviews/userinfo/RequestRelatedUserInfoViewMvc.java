package il.co.idocare.mvcviews.userinfo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import il.co.idocare.R;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.AbstractViewMVC;
import il.co.idocare.mvcviews.ViewMVC;
import il.co.idocare.pictures.ImageViewPictureLoader;

/**
 * Created by Vasiliy on 5/6/2016.
 */
public class RequestRelatedUserInfoViewMvc
        extends AbstractViewMVC<RequestRelatedUserInfoViewMvc.RequestRelatedUserInfoViewMvcListener>
        implements ViewMVC {

    @NonNull
    private final ImageViewPictureLoader mImageViewPictureLoader;

    interface RequestRelatedUserInfoViewMvcListener {

    }

    private ImageView mImgUserPicture;
    private TextView mTxtUserNickname;
    private TextView mTxtCustomInfo;
    private TextView mTxtUserReputation;


    public RequestRelatedUserInfoViewMvc(@NonNull LayoutInflater inflater,
                                         @Nullable ViewGroup container,
                                         @NonNull ImageViewPictureLoader imageViewPictureLoader) {
        mImageViewPictureLoader = imageViewPictureLoader;
        setRootView(inflater.inflate(R.layout.element_request_related_user_info, container, true));
        initialize();
    }

    private void initialize() {
        mImgUserPicture = (ImageView) getRootView().findViewById(R.id.img_user_picture);
        mTxtUserNickname = (TextView) getRootView().findViewById(R.id.txt_user_nickname);
        mTxtCustomInfo = (TextView) getRootView().findViewById(R.id.txt_custom_info);
        mTxtUserReputation = (TextView) getRootView().findViewById(R.id.txt_user_reputation);
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    public void bindUser(UserItem userItem) {

        mTxtUserNickname.setText(userItem.getNickname());

        mImageViewPictureLoader.loadFromWebOrFile(mImgUserPicture, userItem.getPictureUrl(),
                R.drawable.ic_default_user_picture);

        mTxtUserReputation.setText(String.valueOf(userItem.getReputation()));
    }

    public void bindCustomInfo(String customInfo) {
        mTxtCustomInfo.setText(customInfo);
    }
}
