package il.co.idocare.mvcviews.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;

/**
 * Implementation of HomeViewMvc
 */
public class HomeViewMvcImpl
        extends AbstractViewMVC<HomeViewMvc.HomeViewMvcListener>
        implements HomeViewMvc {

    private View mRootView;

    private ListView mListView;

    public HomeViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.layout_home, container, false);

        mListView = (ListView) mRootView.findViewById(R.id.list_requests_thumbnails);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                for (HomeViewMvcListener listener : getListeners()) {
                    listener.onListItemClick(position, id);
                }
            }
        });

    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        mListView.setAdapter(adapter);
    }

}
