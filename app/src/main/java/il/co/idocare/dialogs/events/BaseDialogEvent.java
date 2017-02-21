package il.co.idocare.dialogs.events;

/**
 * Base class for all dialogs related events
 */
/* package */ abstract class BaseDialogEvent {
    private String mTag;

    public BaseDialogEvent(String tag) {
        mTag = tag;
    }

    public String getDialogTag() {
        return mTag;
    }
}
