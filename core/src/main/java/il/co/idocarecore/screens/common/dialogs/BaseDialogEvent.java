package il.co.idocarecore.screens.common.dialogs;

/**
 * Base class for all dialogs related events
 */
public abstract class BaseDialogEvent {
    private String mTag;

    public BaseDialogEvent(String tag) {
        mTag = tag;
    }

    public String getDialogTag() {
        return mTag;
    }
}
