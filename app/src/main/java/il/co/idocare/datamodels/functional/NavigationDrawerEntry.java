package il.co.idocare.datamodels.functional;

/**
 * This class encapsulates information about a single entry shown in navigation drawer
 */
public class NavigationDrawerEntry {


    private final int mTitleStringId;
    private final int mIconDrawableId;
    private final String mTag;

    public NavigationDrawerEntry(int titleStringId, int iconDrawableId, String tag) {
        mTitleStringId = titleStringId;
        mIconDrawableId = iconDrawableId;
        mTag = tag;
    }

    public int getIconResId() {
        return mIconDrawableId;
    }

    public int getTitleResId() {
        return mTitleStringId;
    }

    public String getTag() {
        return mTag;
    }
}
