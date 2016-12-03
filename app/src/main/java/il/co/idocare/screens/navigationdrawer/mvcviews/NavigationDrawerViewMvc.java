package il.co.idocare.screens.navigationdrawer.mvcviews;

import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.ObservableViewMVC;

/**
 * This mvc view represents navigation drawer's UI
 */
public interface NavigationDrawerViewMvc extends
        ObservableViewMVC<NavigationDrawerViewMvc.NavigationDrawerViewMvcListener> {

    public interface NavigationDrawerViewMvcListener {
        void onRequestsListClicked();
        void onMyRequestsClicked();
        void onNewRequestClicked();
        void onLogInClicked();
        void onLogOutClicked();
        void onShowMapClicked();
    }

    void bindUserData(UserItem user);

    void refreshDrawer(boolean isUserLoggedIn);

}
