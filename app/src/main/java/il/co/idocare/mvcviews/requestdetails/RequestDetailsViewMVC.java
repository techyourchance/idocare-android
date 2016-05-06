package il.co.idocare.mvcviews.requestdetails;

import il.co.idocare.datamodels.functional.RequestItem;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.mvcviews.ObservableViewMVC;
import il.co.idocare.mvcviews.ViewMVC;

/**
 * This MVC view represents a screen showing request's details
 */
public interface RequestDetailsViewMvc
        extends ObservableViewMVC<RequestDetailsViewMvc.RequestDetailsViewMvcListener> {

    interface RequestDetailsViewMvcListener {
        void onCloseRequestClicked();
        void onPickupRequestClicked();
        void onClosedVoteUpClicked();
        void onClosedVoteDownClicked();
        void onCreatedVoteUpClicked();
        void onCreatedVoteDownClicked();
    }


    void bindRequestItem(RequestItem requestItem);

    void bindCreatedByUser(UserItem user);

    void bindClosedByUser(UserItem user);

    void bindPickedUpByUser(UserItem user);
}
