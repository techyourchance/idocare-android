package il.co.idocare.mvcviews.requestthumbnail;

import il.co.idocare.datamodels.functional.RequestItem;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.ViewMVC;

/**
 * This MVC view represents a "thumbnail" of request which can be shown as a list element
 */
public interface RequestThumbnailViewMvc extends ViewMVC {

    /**
     * This interface should be implemented by classes which instantiate RequestThumbnailViewMvc in
     * order to get notifications about input events
     */
    public interface RequestThumbnailViewMvcListener {

    }

    void bindRequestItem(RequestItem request);

    void bindCreatedByUser(UserItem user);

    void bindPickedUpByUser(UserItem user);

}
