package il.co.idocarerequests.screens.requestdetails.mvcviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import il.co.idocarecore.screens.common.mvcviews.AbstractViewMvc;
import il.co.idocarerequests.R;

/**
 * Implementation of CloseRequestViewMvc interface
 */
public class CloseRequestViewMvcImpl
        extends AbstractViewMvc<CloseRequestViewMvc.CloseRequestViewMvcListener>
        implements CloseRequestViewMvc {

    private final static String TAG = CloseRequestViewMvcImpl.class.getSimpleName();

    private EditText mEdtClosedComment;

    private CameraControlViewMvc mCameraControlViewMvc;


    public CloseRequestViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_close_request, container, false));

        mCameraControlViewMvc = new CameraControlViewMvcImpl(inflater, null);
        mCameraControlViewMvc.registerListener(new CameraControlViewMvc.CameraControlViewMvcListener() {
            @Override
            public void onTakePictureClicked() {
                for (CloseRequestViewMvcListener listener : getListeners()) {
                    listener.onTakePictureClicked();
                }
            }
        });

        FrameLayout frameCameraControl = (FrameLayout) getRootView().findViewById(R.id.frame_camera_control);
        frameCameraControl.addView(mCameraControlViewMvc.getRootView());

        mEdtClosedComment = (EditText) getRootView().findViewById(R.id.edt_closed_comment);

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
        mCameraControlViewMvc.showPicture(position, cameraPicturePath);
    }

    @Override
    public Bundle getViewState() {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_CLOSED_COMMENT, mEdtClosedComment.getText().toString());
        return bundle;
    }
}
