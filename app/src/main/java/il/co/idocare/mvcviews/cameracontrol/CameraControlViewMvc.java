package il.co.idocare.mvcviews.cameracontrol;

import il.co.idocare.mvcviews.ObservableViewMvc;

/**
 * This MVC view represents "camera control" element
 */
public interface CameraControlViewMvc
        extends ObservableViewMvc<CameraControlViewMvc.CameraControlViewMvcListener> {


    interface CameraControlViewMvcListener {
        /**
         * Will be invoked when the user clicks on "take picture" button
         */
        void onTakePictureClicked();
    }


    public int MAX_PICTURES = 3;

    /**
     * Show picture at position
     * @param position the position (i.e. index) of the picture
     * @param cameraPicturePath picture's URI
     */
    void showPicture(int position, String cameraPicturePath);
}
