package il.co.idocare.pojos;

import android.graphics.drawable.Drawable;

/**
 * Created by Vasiliy on 3/23/2015.
 */
public class NavigationDrawerEntry {


    private int mIcon;
    private String mTitle;

    public NavigationDrawerEntry(String title, int icon) {
        mIcon = icon;
        mTitle = title;
    }

    public int getIcon() {
        return mIcon;
    }

    public String getTitle() {
        return mTitle;
    }
}
