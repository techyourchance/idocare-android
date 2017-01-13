package il.co.idocare.screens.requests.listadapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.requests.RequestEntity;
import il.co.idocare.screens.requests.mvcviews.RequestPreviewViewMvcImpl;

/**
 * This adapter should be used in order to populate ListView with {@link RequestEntity} objects
 */
public class RequestsListAdapter extends ArrayAdapter<RequestEntity> {


    private Context mContext;

    public RequestsListAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
    }

    public void bindRequests(List<RequestEntity> requests) {
        clear();
        addAll(requests);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RequestPreviewViewMvcImpl requestPreviewViewMvc;
        if (convertView == null) {
            requestPreviewViewMvc =
                    new RequestPreviewViewMvcImpl(LayoutInflater.from(mContext), parent);
            convertView = requestPreviewViewMvc.getRootView();
            convertView.setTag(requestPreviewViewMvc);
        } else {
            requestPreviewViewMvc = (RequestPreviewViewMvcImpl) convertView.getTag();
        }

        requestPreviewViewMvc.bindRequest(getItem(position));

        return convertView;
    }

}
