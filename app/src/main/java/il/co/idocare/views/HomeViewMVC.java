package il.co.idocare.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import de.greenrobot.event.EventBus;
import il.co.idocare.R;
import il.co.idocare.controllers.listadapters.HomeListAdapter;

/**
 * MVC View of the Home screen.
 */
public class HomeViewMVC implements ViewMVC {

    private View mRootView;

    private ListView mListView;

    private HomeListAdapter mAdapter;

    public HomeViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.layout_home, container, false);

        mListView = (ListView) mRootView.findViewById(R.id.list_requests_thumbnails);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                EventBus.getDefault().post(new ListItemClickEvent(position, id));
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

    public void setListAdapter(ListAdapter adapter) {
        mListView.setAdapter(adapter);
    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    /**
     * This nested static class represents an item click event that will be posted on EventBus.
     */
    public static class ListItemClickEvent {
        public int mPosition;
        public long mId;

        public ListItemClickEvent(int position, long id) {
            mPosition = position;
            mId = id;
        }
    }

    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------


}
