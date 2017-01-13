package il.co.idocare.screens.requests.mvcviews;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.screens.requests.listadapters.RequestsListAdapter;

/**
 * This MVC view shows a list of requests
 */
public class RequestsMyViewMvcImpl
        extends AbstractViewMVC<RequestsMyViewMvcImpl.RequestsMyViewMvcListener> {


    public interface RequestsMyViewMvcListener {
        public void onRequestClicked(RequestEntity request);
        public void onCreateNewRequestClicked();
    }

    private ListView mLstMyRequests;
    private RequestsListAdapter mRequestsListAdapter;
    private FloatingActionButton mFloatingActionButton;

    public RequestsMyViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_requests_my, container, false));

        initList();
        initFloatingActionButton();

    }

    private void initList() {
        mLstMyRequests = findViewById(R.id.lst_my_requests);

        mRequestsListAdapter = new RequestsListAdapter(getContext(), 0);
        mLstMyRequests.setAdapter(mRequestsListAdapter);

        mLstMyRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (RequestsMyViewMvcListener listener : getListeners()) {
                    listener.onRequestClicked(mRequestsListAdapter.getItem(position));
                }
            }
        });
    }

    private void initFloatingActionButton() {

        mFloatingActionButton = findViewById(R.id.btn_create_new_request);


        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RequestsMyViewMvcListener listener : getListeners()) {
                    listener.onCreateNewRequestClicked();
                }
            }
        });
    }

    public void bindRequests(List<RequestEntity> requests) {
        mRequestsListAdapter.bindRequests(requests);
    }

    @Override
    public Bundle getViewState() {
        return null;
    }


}
