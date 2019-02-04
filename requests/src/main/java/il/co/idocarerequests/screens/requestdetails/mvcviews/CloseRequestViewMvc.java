package il.co.idocarerequests.screens.requestdetails.mvcviews;

import il.co.idocarecore.Constants;
import il.co.idocarecore.screens.common.mvcviews.ObservableViewMvc;

/**
 * This MVC view represents "close request" screen
 */
public interface CloseRequestViewMvc
        extends ObservableViewMvc<CloseRequestViewMvc.CloseRequestViewMvcListener> {



    interface CloseRequestViewMvcListener {
        /**
         * Will be invoked when the user clicks on "create request" button
         */
        void onCloseRequestClicked();

        /**
         * Will be invoked when the user clicks on "take picture" button
         */
        void onTakePictureClicked();
    }

    String KEY_CLOSED_COMMENT = Constants.FIELD_NAME_CLOSED_COMMENT;

    int MAX_PICTURES = 3;

    /**
     * Show picture at position
     * @param position the position (i.e. index) of the picture
     * @param cameraPicturePath picture's URI
     */
    void showPicture(int position, String cameraPicturePath);
}
