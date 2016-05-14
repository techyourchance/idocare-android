package il.co.idocare.mvcviews.closerequest;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;

/**
 * Implementation of CloseRequestViewMvc interface
 */
public class CloseRequestViewMvcImpl
        extends AbstractViewMVC<CloseRequestViewMvc.CloseRequestViewMvcListener>
        implements CloseRequestViewMvc {

    private final static String TAG = CloseRequestViewMvcImpl.class.getSimpleName();

    private EditText mEdtClosedComment;
    private ImageView[] mImgPictures = new ImageView[3];


    public CloseRequestViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_close_request, container, false));

        mEdtClosedComment = (EditText) getRootView().findViewById(R.id.edt_closed_comment);
        mImgPictures[0] = (ImageView) getRootView().findViewById(R.id.img_picture0);
        mImgPictures[1] = (ImageView) getRootView().findViewById(R.id.img_picture1);
        mImgPictures[2] = (ImageView) getRootView().findViewById(R.id.img_picture2);

        View viewTakePicture = getRootView().findViewById(R.id.view_take_picture);
        viewTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (CloseRequestViewMvcListener listener : getListeners()) {
                    listener.onTakePictureClicked();
                }
            }
        });

        Button btnCloseRequest = (Button) getRootView().findViewById(R.id.btn_close_request);
        btnCloseRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (CloseRequestViewMvcListener listener : getListeners()) {
                    listener.onCloseRequestClicked();
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
        bundle.putString(KEY_CLOSED_COMMENT, mEdtClosedComment.getText().toString());
        return bundle;
    }

}
