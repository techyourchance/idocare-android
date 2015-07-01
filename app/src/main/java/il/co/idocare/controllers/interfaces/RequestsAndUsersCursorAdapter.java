package il.co.idocare.controllers.interfaces;

import android.database.Cursor;
import android.widget.ListAdapter;

import il.co.idocare.pojos.RequestItem;

/**
 * An interface for an adapter which can take in two cursors (one containing data about requests and
 * the other one containing data about users) and adapt these two sets of data to a single list.
 */
public interface RequestsAndUsersCursorAdapter extends ListAdapter {

    public Cursor swapRequestsCursor(Cursor requestsCursor);

    public Cursor swapUsersCursor(Cursor usersCursor);

    public RequestItem getRequestAtPosition(int position);
}
