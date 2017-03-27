package il.co.idocare.mvcviews.requestdetails;

import il.co.idocare.mvcviews.ObservableViewMvc;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.users.UserEntity;

/**
 * This MVC view represents a screen showing request's details
 */
public interface RequestDetailsViewMvc
        extends ObservableViewMvc<RequestDetailsViewMvc.RequestDetailsViewMvcListener> {

    interface RequestDetailsViewMvcListener {
        void onCloseRequestClicked();
        void onPickupRequestClicked();
        void onClosedVoteUpClicked();
        void onClosedVoteDownClicked();
        void onCreatedVoteUpClicked();
        void onCreatedVoteDownClicked();
    }


    void bindRequest(RequestEntity request);

    void bindCurrentUserId(String currentUserId);

    void bindCreatedByUser(UserEntity user);

    void bindClosedByUser(UserEntity user);

    void bindPickedUpByUser(UserEntity user);
}
