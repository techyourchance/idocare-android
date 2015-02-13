package il.co.idocare.views;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import il.co.idocare.Constants;
import il.co.idocare.R;

/**
 * MVC View for Close Request screen.
 */
public class CloseRequestViewMVC extends AbstractViewMVC {

    private final static String LOG_TAG = "CloseRequestViewMVC";


    View mRootView;

    EditText mEdtNoteAfter;

    public CloseRequestViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.fragment_close_request, container, false);

        mEdtNoteAfter = (EditText) mRootView.findViewById(R.id.edt_note_after);

        Button btnTakePicture = (Button) mRootView.findViewById(R.id.btn_take_picture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
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
        bundle.putString("noteAfter", mEdtNoteAfter.getText().toString());
        return bundle;
    }
}
