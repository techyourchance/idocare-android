package il.co.idocare.mvcviews.newrequest;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.mvcviews.ViewMVC;

/**
 * MVC View for New Request screen.
 */
public class NewRequestViewMVC implements ViewMVC {

    public final static String KEY_CREATED_COMMENT = Constants.FIELD_NAME_CREATED_COMMENT;
    public final static String KEY_CREATED_POLLUTION_LEVEL = Constants.FIELD_NAME_CREATED_POLLUTION_LEVEL;

    private final static String LOG_TAG = NewRequestViewMVC.class.getSimpleName();


    private View mRootView;

    private EditText mEdtCreatedComment;
    private RatingBar mRatingbarPollutionLevel;
    private ImageView[] mImgPictures = new ImageView[3];

    public NewRequestViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.layout_new_request, container, false);

        mEdtCreatedComment = (EditText) mRootView.findViewById(R.id.edt_created_comment);
        mRatingbarPollutionLevel = (RatingBar) mRootView.findViewById(R.id.ratingbar_pollution_level);
        mImgPictures[0] = (ImageView) mRootView.findViewById(R.id.img_picture0);
        mImgPictures[1] = (ImageView) mRootView.findViewById(R.id.img_picture1);
        mImgPictures[2] = (ImageView) mRootView.findViewById(R.id.img_picture2);

        View viewTakePicture = mRootView.findViewById(R.id.view_take_picture);
        viewTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new TakePictureButtonClickEvent());
            }
        });

        Button btnCreateNewRequest = (Button) mRootView.findViewById(R.id.btn_create_new_request);
        btnCreateNewRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CreateNewRequestButtonClickEvent());
            }
        });
    }

    public void showPicture(int position, String cameraPicturePath) {
        if (position >= 3) {
            Log.e(LOG_TAG, "maximal number of pictures exceeded!");
            return;
        }

        ImageLoader.getInstance().displayImage(
                Constants.UIL_LOCAL_FILE_PREFIX + cameraPicturePath,
                mImgPictures[position],
                Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
    }


    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FIELD_NAME_CREATED_COMMENT,
                mEdtCreatedComment.getText().toString());
        bundle.putString(Constants.FIELD_NAME_CREATED_POLLUTION_LEVEL,
                String.valueOf(mRatingbarPollutionLevel.getRating()));
        return bundle;
    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    public static class CreateNewRequestButtonClickEvent {}

    public static class TakePictureButtonClickEvent {}


    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------

}
