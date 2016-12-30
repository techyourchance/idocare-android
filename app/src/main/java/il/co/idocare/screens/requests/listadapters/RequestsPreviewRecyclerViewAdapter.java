package il.co.idocare.screens.requests.listadapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.requests.RequestEntity;
import il.co.idocare.screens.requests.mvcviews.RequestPreviewViewMvcImpl;

/**
 * This adapter should be used in order to populate RecyclerView with previews of requests
 */
public class RequestsPreviewRecyclerViewAdapter extends
        RecyclerView.Adapter<RequestsPreviewRecyclerViewAdapter.RequestsPreviewViewHolder>
        implements RequestPreviewViewMvcImpl.RequestPreviewViewMvcListener {

    public interface  OnRequestClickListener {
        public void onRequestClicked(RequestEntity request);
    }

    public static class RequestsPreviewViewHolder extends RecyclerView.ViewHolder {

        private final RequestPreviewViewMvcImpl mViewMvc;

        public RequestsPreviewViewHolder(RequestPreviewViewMvcImpl viewMvc,
                                         RequestPreviewViewMvcImpl.RequestPreviewViewMvcListener listener) {
            super(viewMvc.getRootView());
            mViewMvc = viewMvc;

            mViewMvc.registerListener(listener);
        }

    }

    private Context mContext;

    private final List<RequestEntity> mRequests = new ArrayList<>(0);

    private final OnRequestClickListener mOnRequestClickListener;

    public RequestsPreviewRecyclerViewAdapter(Context context, OnRequestClickListener listener) {
        mContext = context;
        mOnRequestClickListener = listener;
    }

    public void bindRequests(List<RequestEntity> requests) {
        mRequests.clear();
        mRequests.addAll(requests);
        notifyDataSetChanged();
    }

    @Override
    public void onRequestClicked(RequestEntity request) {
        mOnRequestClickListener.onRequestClicked(request);
    }

    @Override
    public RequestsPreviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RequestPreviewViewMvcImpl viewMvc =
                new RequestPreviewViewMvcImpl(LayoutInflater.from(mContext), parent);
        return new RequestsPreviewViewHolder(viewMvc, this);
    }

    @Override
    public void onBindViewHolder(RequestsPreviewViewHolder holder, int position) {
        holder.mViewMvc.bindRequest(mRequests.get(position));
    }

    @Override
    public int getItemCount() {
        return mRequests.size();
    }
}
