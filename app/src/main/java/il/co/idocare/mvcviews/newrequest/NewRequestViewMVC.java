package il.co.idocare.mvcviews.newrequest;

import il.co.idocare.Constants;
import il.co.idocare.mvcviews.ObservableViewMVC;
import il.co.idocare.mvcviews.ViewMVC;

/**
 * MVC View for New Request screen
 */
public interface NewRequestViewMvc
        extends ObservableViewMVC<NewRequestViewMvc.NewRequestViewMvcListener> {

    interface NewRequestViewMvcListener {
        /**
         * Will be invoked when the user clicks on "create request" button
         */
        void onCreateRequestClicked();

        /**
         * Will be invoked when the user clicks on "take picture" button
         */
        void onTakePictureClicked();
    }

    String KEY_CREATED_COMMENT = Constants.FIELD_NAME_CREATED_COMMENT;

    /**
     * Show picture at position
     * @param position the position (i.e. index) of the picture
     * @param cameraPicturePath picture's URI
     */
    void showPicture(int position, String cameraPicturePath);
}
