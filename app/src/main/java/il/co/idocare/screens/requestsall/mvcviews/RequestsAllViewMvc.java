package il.co.idocare.screens.requestsall.mvcviews;

import android.widget.ListAdapter;

import il.co.idocare.mvcviews.ObservableViewMVC;

/**
 * This MVC view represents "home" screen which displays a list of requests
 */
public interface RequestsAllViewMvc
        extends ObservableViewMVC<RequestsAllViewMvc.RequestsAllViewMvcListener> {

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
