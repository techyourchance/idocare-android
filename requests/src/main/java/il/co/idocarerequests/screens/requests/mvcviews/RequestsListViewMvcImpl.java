package il.co.idocarerequests.screens.requests.mvcviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.screens.common.widgets.IdcViewUtils;
import il.co.idocarecore.screens.common.mvcviews.AbstractViewMvc;
import il.co.idocarecore.users.UsersManager;
import il.co.idocarerequests.R;
import il.co.idocarerequests.screens.requests.listadapters.RequestsListAdapter;

/**
 * This MVC view shows a list of requests
 */
public class RequestsListViewMvcImpl
        extends AbstractViewMvc<RequestsListViewMvcImpl.RequestsListViewMvcListener> {



    public interface RequestsListViewMvcListener {
        public void onRequestClicked(RequestEntity request);
        public void onCreateNewRequestClicked();
    }

    private ListView mLstMyRequests;
    private RequestsListAdapter mRequestsListAdapter;
    private FloatingActionButton mFloatingActionButton;
    private final UsersManager mUsersManager;

    private final View mProgressView;

    public RequestsListViewMvcImpl(LayoutInflater inflater, ViewGroup container, UsersManager usersManager) {
        mUsersManager = usersManager;
        setRootView(inflater.inflate(R.layout.layout_requests_my, container, false));

        initList();
        initFloatingActionButton();

        mProgressView = findViewById(R.id.element_progress_overlay);
        IdcViewUtils.showProgressOverlay(mProgressView);

    }

    private void initList() {
        mLstMyRequests = findViewById(R.id.lst_my_requests);

        mRequestsListAdapter = new RequestsListAdapter(getContext(), 0, mUsersManager);
        mLstMyRequests.setAdapter(mRequestsListAdapter);

        mLstMyRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (RequestsListViewMvcListener listener : getListeners()) {
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
                for (RequestsListViewMvcListener listener : getListeners()) {
                    listener.onCreateNewRequestClicked();
                }
            }
        });
    }

    public void bindRequests(List<RequestEntity> requests) {
        mRequestsListAdapter.bindRequests(requests);
        IdcViewUtils.hideProgressOverlay(mProgressView);
    }

    @Override
    public Bundle getViewState() {
        return null;
    }


}
