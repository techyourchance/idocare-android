package il.co.idocare.datamodels.functional;

/**
 * Created by Vasiliy on 3/23/2015.
 */
public class NavigationDrawerEntry {


    private final int mTitleStringId;
    private final int mIconDrawableId;

    public NavigationDrawerEntry(int titleStringId, int iconDrawableId) {
        mTitleStringId = titleStringId;
        mIconDrawableId = iconDrawableId;
    }

    public int getIconResId() {
        return mIconDrawableId;
    }

    public int getTitleResId() {
        return mTitleStringId;
    }
}
