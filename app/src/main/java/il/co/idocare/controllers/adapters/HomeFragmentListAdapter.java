package il.co.idocare.controllers.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import il.co.idocare.pojos.RequestItem;
import il.co.idocare.views.RequestThumbnailViewMVC;

/**
 * Customized CursorAdapter that is used for displaying the list of requests on HomeFragment.
 */
public class HomeFragmentListAdapter extends CursorAdapter {

    private final static String LOG_TAG = HomeFragmentListAdapter.class.getSimpleName();

    private Context mContext;

    public HomeFragmentListAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mContext = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return new RequestThumbnailViewMVC(mContext);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RequestItem request = RequestItem.createRequestItem(cursor);

        try {
            ((RequestThumbnailViewMVC) view).showRequestThumbnail(request);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }
}
