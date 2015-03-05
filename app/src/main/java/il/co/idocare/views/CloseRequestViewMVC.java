package il.co.idocare.views;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import il.co.idocare.Constants;
import il.co.idocare.R;

/**
 * MVC View for Close Request screen.
 */
public class CloseRequestViewMVC extends AbstractViewMVC {

    private final static String LOG_TAG = "CloseRequestViewMVC";


    private View mRootView;

    private EditText mEdtClosedComment;
    private ImageView[] mImgPictures = new ImageView[3];


    public CloseRequestViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.fragment_close_request, container, false);

        mEdtClosedComment = (EditText) mRootView.findViewById(R.id.edt_closed_comment);
        mImgPictures[0] = (ImageView) mRootView.findViewById(R.id.img_picture0);
        mImgPictures[1] = (ImageView) mRootView.findViewById(R.id.img_picture1);
        mImgPictures[2] = (ImageView) mRootView.findViewById(R.id.img_picture2);

        View viewTakePicture = mRootView.findViewById(R.id.view_take_picture);
        viewTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyOutboxHandlers(Constants.MessageType.V_TAKE_PICTURE_BUTTON_CLICKED.ordinal(),
                        0, 0, null);
            }
        });

        Button btnCloseRequest = (Button) mRootView.findViewById(R.id.btn_close_request);
        btnCloseRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyOutboxHandlers(Constants.MessageType.V_CLOSE_REQUEST_BUTTON_CLICKED.ordinal(),
                        0, 0, null);
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
    protected void handleMessage(Message msg) {
        // TODO: complete this method
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FieldName.CLOSED_COMMENT.getValue(),
                mEdtClosedComment.getText().toString());
        return bundle;
    }
}
