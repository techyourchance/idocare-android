package il.co.idocare.eventbusevents;

/**
 * This class contains all events related to Dialogs
 */
public final class DialogEvents {

    private DialogEvents() {}

    /**
     * Base class for all dialogs related events
     */
    private static abstract class DialogBaseEvent {
        private String mTag;

        public DialogBaseEvent(String tag) {
            mTag = tag;
        }

        public String getTag() {
            return mTag;
        }
    }

    /**
     * This event will be posted to EventBus when InfoDialog is dismissed
     */
    public static class InfoDialogDismissedEvent extends DialogBaseEvent {

        public InfoDialogDismissedEvent(String tag) {
            super(tag);
        }
    }

    /**
     * This event will be posted to EventBus when user clicks one of the buttons in PromptDialog
     */
    public static class PromptDialogDismissedEvent extends DialogBaseEvent {

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
}
