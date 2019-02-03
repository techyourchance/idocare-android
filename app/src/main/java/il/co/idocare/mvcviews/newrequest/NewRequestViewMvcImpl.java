package il.co.idocare.mvcviews.newrequest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import il.co.idocarecore.Constants;
import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMvc;
import il.co.idocare.mvcviews.cameracontrol.CameraControlViewMvc;
import il.co.idocare.mvcviews.cameracontrol.CameraControlViewMvcImpl;

/**
 * .
 */
public class NewRequestViewMvcImpl
        extends AbstractViewMvc<NewRequestViewMvc.NewRequestViewMvcListener>
        implements NewRequestViewMvc {

    private final static String TAG = NewRequestViewMvcImpl.class.getSimpleName();

    private EditText mEdtCreatedComment;

    private CameraControlViewMvc mCameraControlViewMvc;

    public NewRequestViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_new_request, container, false));

        mCameraControlViewMvc = new CameraControlViewMvcImpl(inflater, null);
        mCameraControlViewMvc.registerListener(new CameraControlViewMvc.CameraControlViewMvcListener() {
            @Override
            public void onTakePictureClicked() {
                for (NewRequestViewMvcListener listener : getListeners()) {
                    listener.onTakePictureClicked();
                }
            }
        });

        FrameLayout frameCameraControl = (FrameLayout) getRootView().findViewById(R.id.frame_camera_control);
        frameCameraControl.addView(mCameraControlViewMvc.getRootView());

        mEdtCreatedComment = (EditText) getRootView().findViewById(R.id.edt_created_comment);

        Button btnCreateNewRequest = (Button) getRootView().findViewById(R.id.btn_create_new_request);
        btnCreateNewRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (NewRequestViewMvcListener listener : getListeners()) {
                    listener.onCreateRequestClicked();
                }
            }
        });
    }

    @Override
    public void showPicture(int position, String cameraPicturePath) {
        mCameraControlViewMvc.showPicture(position, cameraPicturePath);
    }

    @Override
    public Bundle getViewState() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FIELD_NAME_CREATED_COMMENT,
                mEdtCreatedComment.getText().toString());
        return bundle;
    }

}
