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
import il.co.idocare.mvcviews.AbstractViewMVC;

/**
 * .
 */
public class NewRequestViewMvcImpl
        extends AbstractViewMVC<NewRequestViewMvc.NewRequestViewMvcListener>
        implements NewRequestViewMvc {

    private final static String TAG = NewRequestViewMvcImpl.class.getSimpleName();

    private EditText mEdtCreatedComment;
    private ImageView[] mImgPictures = new ImageView[3];

    public NewRequestViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_new_request, container, false));

        mEdtCreatedComment = (EditText) getRootView().findViewById(R.id.edt_created_comment);
        mImgPictures[0] = (ImageView) getRootView().findViewById(R.id.img_picture0);
        mImgPictures[1] = (ImageView) getRootView().findViewById(R.id.img_picture1);
        mImgPictures[2] = (ImageView) getRootView().findViewById(R.id.img_picture2);

        View viewTakePicture = getRootView().findViewById(R.id.view_take_picture);
        viewTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (NewRequestViewMvcListener listener : getListeners()) {
                    listener.takePictureClicked();
                }
            }
        });

        Button btnCreateNewRequest = (Button) getRootView().findViewById(R.id.btn_create_new_request);
        btnCreateNewRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (NewRequestViewMvcListener listener : getListeners()) {
                    listener.createRequestClicked();
                }
            }
        });
    }

    @Override
    public void showPicture(int position, String cameraPicturePath) {
        if (position >= 3) {
            Log.e(TAG, "maximal number of pictures exceeded!");
            return;
        }

        ImageLoader.getInstance().displayImage(
                Constants.UIL_LOCAL_FILE_PREFIX + cameraPicturePath,
                mImgPictures[position],
                Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
    }

    @Override
    public Bundle getViewState() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FIELD_NAME_CREATED_COMMENT,
                mEdtCreatedComment.getText().toString());
        return bundle;
    }

}
