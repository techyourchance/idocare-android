package il.co.idocare.mvcviews.navdrawerheader;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.MalformedURLException;
import java.net.URL;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.AbstractViewMVC;

/**
 * Created by Vasiliy on 5/14/2016.
 */
public class NavDrawerHeaderViewMvcImpl
        extends AbstractViewMVC<NavDrawerHeaderViewMvc.NavDrawerHeaderViewMvcListener>
        implements NavDrawerHeaderViewMvc {

    private ImageView mImgUserPicture;
    private ImageView mImgReputationStar;
    private TextView mTxtUserReputation;
    private TextView mTxtUserNickname;

    public NavDrawerHeaderViewMvcImpl(@NonNull LayoutInflater inflater,
                                      @Nullable ViewGroup container) {
        setRootView(inflater.inflate(R.layout.element_nav_drawer_header, container));

        mImgUserPicture = (ImageView) getRootView().findViewById(R.id.img_user_picture);
        mImgReputationStar = (ImageView) getRootView().findViewById(R.id.img_reputation_star);
        mTxtUserReputation = (TextView) getRootView().findViewById(R.id.txt_user_reputation);
        mTxtUserNickname = (TextView) getRootView().findViewById(R.id.txt_user_nickname);
    }


    @Override
    public Bundle getViewState() {
        return null;
    }


    @Override
    public void bindUserData(UserItem user) {

        if (user != null && user.getId() > 0) {
            mImgUserPicture.setVisibility(View.VISIBLE);
            mImgReputationStar.setVisibility(View.VISIBLE);
            mTxtUserReputation.setVisibility(View.VISIBLE);
            mTxtUserNickname.setVisibility(View.VISIBLE);

            mTxtUserNickname.setText(user.getNickname());
            mTxtUserReputation.setText(String.valueOf(user.getReputation()));

            if (user.getPictureUrl() != null && user.getPictureUrl().length() > 0) {
                showUserPicture(user.getPictureUrl());
            } else {
                mImgUserPicture.setImageResource(R.drawable.ic_default_user_picture);
            }
        } else {
            mImgUserPicture.setVisibility(View.GONE);
            mImgReputationStar.setVisibility(View.GONE);
            mTxtUserReputation.setVisibility(View.GONE);
            mTxtUserNickname.setVisibility(View.GONE);
        }
    }

    private void showUserPicture(String pictureUrl) {
        String universalImageLoaderUri = pictureUrl;
        try {
            new URL(universalImageLoaderUri);
        } catch (MalformedURLException e) {
            // The exception means that the current Uri is not a valid URL - it is local
            // uri and we need to adjust it to the scheme recognized by UIL
            universalImageLoaderUri = "file://" + universalImageLoaderUri;
        }

        ImageLoader.getInstance().displayImage(
                universalImageLoaderUri,
                mImgUserPicture,
                Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);

    }
}
