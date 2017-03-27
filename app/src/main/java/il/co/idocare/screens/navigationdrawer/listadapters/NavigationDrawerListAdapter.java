package il.co.idocare.screens.navigationdrawer.listadapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import il.co.idocare.datamodels.functional.NavigationDrawerEntry;
import il.co.idocare.mvcviews.navdrawerentry.NavigationDrawerEntryViewMvc;

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

        NavigationDrawerEntryViewMvc view;

        if (convertView == null) {
            view = new NavigationDrawerEntryViewMvc(mContext);
        } else {
            view = (NavigationDrawerEntryViewMvc) convertView;
        }

        view.showEntry(getItem(position));

        return view;
    }

}
