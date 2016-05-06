package il.co.idocare.mvcviews.home;

import android.widget.ListAdapter;

import il.co.idocare.mvcviews.ObservableViewMVC;
import il.co.idocare.mvcviews.ViewMVC;

/**
 * This MVC view represents "home" screen which displays a list of requests
 */
public interface HomeViewMvc
        extends ObservableViewMVC<HomeViewMvc.HomeViewMvcListener> {

    /**
     * This interface should be implemented by classes which instantiate HomeViewMvc in
     * order to get notifications about input events
     */
    interface HomeViewMvcListener {
        void onListItemClick(int position, long id);

        void onCreateNewRequestClick();
    }

    void setListAdapter(ListAdapter adapter);
}
