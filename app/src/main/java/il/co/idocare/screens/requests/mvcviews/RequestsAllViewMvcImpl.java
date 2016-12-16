package il.co.idocare.screens.requests.mvcviews;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;

/**
 * Implementation of RequestsAllViewMvc
 */
public class RequestsAllViewMvcImpl
        extends AbstractViewMVC<RequestsAllViewMvc.RequestsAllViewMvcListener>
        implements RequestsAllViewMvc {

    private ListView mListView;
    private FloatingActionButton mBtnCreateRequest;

    public RequestsAllViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_home, container, false));

        mListView = (ListView) getRootView().findViewById(R.id.list_requests_thumbnails);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                for (RequestsAllViewMvcListener listener : getListeners()) {
                    listener.onListItemClick(position, id);
                }
            }
        });

        mBtnCreateRequest = (FloatingActionButton) getRootView().findViewById(R.id.btn_create_new_request);
        mBtnCreateRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestsAllViewMvcListener listener : getListeners()) {
                    listener.onCreateNewRequestClick();
                }
            }
        });

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
