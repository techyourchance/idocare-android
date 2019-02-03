package il.co.idocare.screens.navigationdrawer.mvcviews;

import il.co.idocare.mvcviews.ObservableViewMvc;
import il.co.idocarecore.users.UserEntity;

/**
 * This mvc view represents navigation drawer's UI
 */
public interface NavigationDrawerViewMvc extends
        ObservableViewMvc<NavigationDrawerViewMvc.NavigationDrawerViewMvcListener> {

    public interface NavigationDrawerViewMvcListener {
        void onRequestsListClicked();
        void onMyRequestsClicked();
        void onNewRequestClicked();
        void onLogInClicked();
        void onLogOutClicked();
        void onShowMapClicked();
    }

    void bindUserData(UserEntity user);

}
