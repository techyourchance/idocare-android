package il.co.idocare.screens.common;

import android.support.annotation.NonNull;

/**
 * This interface can be implemented by components that contain FrameLayout in order to allow
 * other components to manage the content of this frame.
 */

public interface MainFrameContainer {

    /**
     * @return FrameHelper object that can be used in order to manager the contents of the
     *         FrameLayout which is contained within the implementation of {@link MainFrameContainer}
     */
    public @NonNull FrameHelper getFrameHelper();
}
