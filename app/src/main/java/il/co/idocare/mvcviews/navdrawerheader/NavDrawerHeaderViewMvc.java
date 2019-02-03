package il.co.idocare.mvcviews.navdrawerheader;

import androidx.annotation.Nullable;

import il.co.idocarecore.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.ObservableViewMvc;

/**
 * This MVC view should be used as nav drawer's header (the topmost element in nav drawer)
 */
public interface NavDrawerHeaderViewMvc
        extends ObservableViewMvc<NavDrawerHeaderViewMvc.NavDrawerHeaderViewMvcListener> {

    interface NavDrawerHeaderViewMvcListener {

    }

    /**
     * Show user's data in navigation drawer's header
     * @param user data of the user; pass null in order to clear user's data
     */
    void bindUserData(@Nullable UserItem user);
}
