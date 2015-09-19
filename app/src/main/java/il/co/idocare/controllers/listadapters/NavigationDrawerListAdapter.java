package il.co.idocare.controllers.listadapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import il.co.idocare.datamodels.functional.NavigationDrawerEntry;
import il.co.idocare.views.NavigationDrawerEntryViewMVC;

/**
 * Created by Vasiliy on 3/23/2015.
 */
public class NavigationDrawerListAdapter extends ArrayAdapter<NavigationDrawerEntry> {

    private Context mContext;

    public NavigationDrawerListAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        NavigationDrawerEntryViewMVC view;

        if (convertView == null) {
            view = new NavigationDrawerEntryViewMVC(mContext);
        } else {
            view = (NavigationDrawerEntryViewMVC) convertView;
        }

        view.showEntry(getItem(position));

        return view;
    }

}
