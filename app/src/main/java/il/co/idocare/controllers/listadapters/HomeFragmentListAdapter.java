package il.co.idocare.controllers.listadapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.interfaces.RequestsAndUsersCursorAdapter;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.pojos.UserItem;
import il.co.idocare.views.RequestThumbnailViewMVC;

/**
 * Customized CursorAdapter that is used for displaying the list of requests on HomeFragment.
 */
public class HomeFragmentListAdapter extends CursorAdapter implements
        RequestsAndUsersCursorAdapter {

    private final static String LOG_TAG = HomeFragmentListAdapter.class.getSimpleName();

    private Cursor mUsersCursor;
    private ConcurrentMap<Long, UserItem> mUsersCache;

    public HomeFragmentListAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mUsersCache = new ConcurrentHashMap<>(5);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return new RequestThumbnailViewMVC(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RequestItem request;

        try {
             request = RequestItem.createRequestItem(cursor);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            // TODO: consider more sophisticated error handling (maybe destroy the view?)
            return;
        }

        ((RequestThumbnailViewMVC) view).bindRequestItem(request);

        UserItem createdByUser = getUser(request.getCreatedBy());
        if (createdByUser != null)
            ((RequestThumbnailViewMVC) view).bindCreatedByUser(createdByUser);
        else
            ((RequestThumbnailViewMVC) view).bindCreatedByUser(UserItem.createAnonymousUser());
    }

    private UserItem getUser(long id) {
        // If user's info has been cached - use it
        if (mUsersCache.containsKey(id)) {
            return mUsersCache.get(id);
        }

        if (mUsersCursor != null && mUsersCursor.moveToFirst()) {
            do {
                if (mUsersCursor.getLong(
                        mUsersCursor.getColumnIndex(IDoCareContract.Users.COL_USER_ID)) == id) {
                    UserItem user = UserItem.create(mUsersCursor);
                    mUsersCache.put(user.getId(), user);
                    return user;
                }
            } while (mUsersCursor.moveToNext());
        }

        return null;
    }


    @Override
    public Cursor swapRequestsCursor(Cursor requestsCursor) {
        return this.swapCursor(requestsCursor);
    }

    @Override
    public Cursor swapUsersCursor(Cursor usersCursor) {
        // Clear users cache
        mUsersCache.clear();

        Cursor oldUsersCursor = mUsersCursor;
        mUsersCursor = usersCursor;
        notifyDataSetChanged();
        return oldUsersCursor;
    }

    @Override
    public RequestItem getRequestAtPosition(int position) {
        return RequestItem.createRequestItem((Cursor) this.getItem(position));
    }
}
