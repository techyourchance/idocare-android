package il.co.idocare.controllers.interfaces;

import android.database.Cursor;
import android.widget.ListAdapter;

import il.co.idocare.datamodels.pojos.RequestItemPojo;

/**
 * An interface for an adapter which can take in two cursors (one containing data about requests and
 * the other one containing data about users) and adapt these two sets of data to a single list.<br>
 * An additional Cursor containing data about the actions the user performed locally can be supplied
 * (actions like voting, picking requests up, etc), in which case this data will be
 * superimposed on the requests/users data obtained from the respective cursors.
 */
public interface RequestsCombinedCursorAdapter extends ListAdapter {

    public Cursor swapRequestsCursor(Cursor requestsCursor);

    public Cursor swapUsersCursor(Cursor usersCursor);

    public Cursor swapUserActionsCursor(Cursor userActionsCursor);

    public RequestItemPojo getRequestAtPosition(int position);
}
