package il.co.idocare.mvcviews.navdrawerentry;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import il.co.idocare.R;
import il.co.idocare.datamodels.functional.NavigationDrawerEntry;
import il.co.idocare.mvcviews.ViewMVC;

/**
 * Created by Vasiliy on 3/23/2015.
 */
public class NavigationDrawerEntryViewMVC extends RelativeLayout implements ViewMVC {

    private static final String LOG_TAG = "NavigationDrawerEntryViewMVC";

    private Context mContext;
    private ImageView mImgIcon;
    private TextView mTxtTitle;


    public NavigationDrawerEntryViewMVC(Context context) {
        super(context);

        init(context);
    }


    /**
     * Initialize this MVC view. Must be called from constructor
     */
    private void init(Context context) {

        // Inflate the underlying layout
        LayoutInflater.from(context).inflate(R.layout.element_nav_drawer_entry, this, true);

//        // This padding is required in order not to hide the border when colorizing inner views
//        int padding = (int) getResources().getDimension(R.dimen.border_background_width);
//        getRootView().setPadding(padding, padding, padding, padding);
//
//        // Set background color and border for the whole item
//        getRootView().setBackgroundColor(getResources().getColor(android.R.color.white));
//        getRootView().setBackgroundResource(R.drawable.border_background);

        mImgIcon = (ImageView) getRootView().findViewById(R.id.img_icon);
        mTxtTitle = (TextView) getRootView().findViewById(R.id.txt_title);


        mContext = context;

    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    public void showEntry(NavigationDrawerEntry entry) {
        if (entry.getIconResId() != 0) {

//            if (icons[i] != 0) {
//                // Change of API for SDK 21 and further
//                icon = Build.VERSION.SDK_INT >= 21 ? getResources().getDrawable(icons[i], getTheme()) :
//                        getResources().getDrawable(icons[i]);
//            } else {
//                icon = null;
//            }

            mImgIcon.setVisibility(View.VISIBLE);
            mImgIcon.setImageResource(entry.getIconResId());
        } else {
            mImgIcon.setVisibility(View.INVISIBLE);
        }
        mTxtTitle.setText(entry.getTitleResId());
    }

}
