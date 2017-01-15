package il.co.idocare.dialogs.events;

/**
 * This event will be posted to EventBus when user clicks one of the buttons in PromptDialog
 */
public class PromptDialogDismissedEvent extends BaseDialogEvent {

    public static final int BUTTON_POSITIVE = 1;
    public static final int BUTTON_NEGATIVE = 2;

    private int mButtonClicked;

    public PromptDialogDismissedEvent(String tag, int buttonClicked) {
        super(tag);
        if (buttonClicked != BUTTON_POSITIVE && buttonClicked != BUTTON_NEGATIVE) {
            throw new IllegalArgumentException("illegal button index");
        }
        mButtonClicked = buttonClicked;
    }

    /**
     * @return either {@link #BUTTON_POSITIVE} or {@link #BUTTON_NEGATIVE}
     */
    public int getClickedButtonIndex() {
        return mButtonClicked;
    }

}
