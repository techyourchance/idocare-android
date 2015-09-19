package il.co.idocare.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.R;

/**
 * MVC View for Close Request screen.
 */
public class CloseRequestViewMVC implements ViewMVC {

    public static final String KEY_CLOSED_COMMENT = Constants.FIELD_NAME_CLOSED_COMMENT;

    private final static String LOG_TAG = CloseRequestViewMVC.class.getSimpleName();


    private View mRootView;

    private EditText mEdtClosedComment;
    private ImageView[] mImgPictures = new ImageView[3];


    public CloseRequestViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.layout_close_request, container, false);

        mEdtClosedComment = (EditText) mRootView.findViewById(R.id.edt_closed_comment);
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

        Button btnCloseRequest = (Button) mRootView.findViewById(R.id.btn_close_request);
        btnCloseRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CloseRequestButtonClickEvent());
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
        bundle.putString(KEY_CLOSED_COMMENT, mEdtClosedComment.getText().toString());
        return bundle;
    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    public static class CloseRequestButtonClickEvent {}

    public static class TakePictureButtonClickEvent {}


    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------

}
