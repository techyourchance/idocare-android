package il.co.idocarerequests.screens.requests.mvcviews;

import android.widget.ListAdapter;

import il.co.idocarecore.screens.common.mvcviews.ObservableViewMvc;


/**
 * This MVC view represents "home" screen which displays a list of requests
 */
public interface RequestsAllViewMvc
        extends ObservableViewMvc<RequestsAllViewMvc.RequestsAllViewMvcListener> {

    /**
     * This interface should be implemented by classes which instantiate RequestsAllViewMvc in
     * order to get notifications about input events
     */
    interface RequestsAllViewMvcListener {
        void onListItemClick(int position, long id);

        void onCreateNewRequestClick();
    }

    void setListAdapter(ListAdapter adapter);
}
