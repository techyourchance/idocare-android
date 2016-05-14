package il.co.idocare.mvcviews.navdrawerheader;

import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.ObservableViewMVC;

/**
 * This MVC view should be used as nav drawer's header (the topmost element in nav drawer)
 */
public interface NavDrawerHeaderViewMvc
        extends ObservableViewMVC<NavDrawerHeaderViewMvc.NavDrawerHeaderViewMvcListener> {

    interface NavDrawerHeaderViewMvcListener {

    }

    /**
     * Show user's data in navigarion drawer's header
     * @param user data of the user
     */
    void bindUserData(UserItem user);
}
