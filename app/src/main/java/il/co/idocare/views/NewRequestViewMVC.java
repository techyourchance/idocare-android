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
 * MVC View for New Request screen.
 */
public class NewRequestViewMVC extends AbstractViewMVC {

    private final static String LOG_TAG = "NewRequestViewMVC";


    View mRootView;

    EditText mEdtNoteBefore;
    RatingBar mRatingbarPollutionLevel;

    public NewRequestViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.fragment_new_request, container, false);

        mEdtNoteBefore = (EditText) mRootView.findViewById(R.id.edt_note_before);
        mRatingbarPollutionLevel = (RatingBar) mRootView.findViewById(R.id.ratingbar_pollution_level);

        Button btnTakePicture = (Button) mRootView.findViewById(R.id.view_take_picture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyOutboxHandlers(Constants.MessageType.V_TAKE_PICTURE_BUTTON_CLICKED.ordinal(),
                        0, 0, null);
            }
        });

        Button btnAddNewRequest = (Button) mRootView.findViewById(R.id.btn_add_new_request);
        btnAddNewRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyOutboxHandlers(Constants.MessageType.V_ADD_NEW_REQUEST_BUTTON_CLICKED.ordinal(),
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
        bundle.putString(Constants.FieldName.CREATED_COMMENT.getValue(),
                mEdtNoteBefore.getText().toString());
        bundle.putString(Constants.FieldName.CREATED_POLLUTION_LEVEL.getValue(),
                String.valueOf(mRatingbarPollutionLevel.getRating()));
        return bundle;
    }
}
