package il.co.idocare.screens.requests.mvcviews;

import il.co.idocarecore.datamodels.functional.RequestItem;
import il.co.idocarecore.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.ViewMvc;

/**
 * This MVC view represents a "thumbnail" of request which can be shown as a list element
 */
public interface RequestThumbnailViewMvc extends ViewMvc {

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
