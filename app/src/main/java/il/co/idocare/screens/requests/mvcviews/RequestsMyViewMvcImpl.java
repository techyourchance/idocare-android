package il.co.idocare.screens.requests.mvcviews;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import il.co.idocare.R;
import il.co.idocare.mvcviews.AbstractViewMVC;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.screens.requests.listadapters.RequestsPreviewRecyclerViewAdapter;

/**
 * This MVC view shows a list of requests
 */
public class RequestsMyViewMvcImpl
        extends AbstractViewMVC<RequestsMyViewMvcImpl.RequestsMyViewMvcListener>
        implements RequestsPreviewRecyclerViewAdapter.OnRequestClickListener {


    public interface RequestsMyViewMvcListener {
        public void onRequestClicked(RequestEntity request);
        public void onCreateNewRequestClicked();
    }

    private RecyclerView mRecyclerView;
    private RequestsPreviewRecyclerViewAdapter mRequestsPreviewRecyclerViewAdapter;
    private FloatingActionButton mFloatingActionButton;

    public RequestsMyViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_requests_my, container, false));

        mRecyclerView = findViewById(R.id.recycler_requests);
        mFloatingActionButton = findViewById(R.id.btn_create_new_request);

        mRequestsPreviewRecyclerViewAdapter =
                new RequestsPreviewRecyclerViewAdapter(getContext(), this);
        mRecyclerView.setAdapter(mRequestsPreviewRecyclerViewAdapter);

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
        mRequestsPreviewRecyclerViewAdapter.bindRequests(requests);
    }

    @Override
    public void onRequestClicked(RequestEntity request) {
        for (RequestsMyViewMvcListener listener : getListeners()) {
            listener.onRequestClicked(request);
        }
    }

    @Override
    public Bundle getViewState() {
        return null;
    }


}
