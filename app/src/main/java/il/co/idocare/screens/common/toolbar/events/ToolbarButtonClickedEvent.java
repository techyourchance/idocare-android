package il.co.idocare.screens.common.toolbar.events;

/**
 * This event will be posted to EventBus when any of toolbar's buttons is being clicked
 */

public class ToolbarButtonClickedEvent {

    public static final int BUTTON_UP = 1;
    public static final int BUTTON_SHOW_NAV_DRAWER = 2;
    public static final int BUTTON_HIDE_NAV_DRAWER = 3;

    private final int mButtonIndex;

    public ToolbarButtonClickedEvent(int buttonIndex) {
        switch (buttonIndex) {
            case BUTTON_UP:
            case BUTTON_SHOW_NAV_DRAWER:
            case BUTTON_HIDE_NAV_DRAWER:
                mButtonIndex = buttonIndex;
            default:
                throw new IllegalArgumentException("invalid button index: " + buttonIndex);
        }
    }

    /**
     * @return either of BUTTON_ constants defined in this class
     */
    public int getButtonIndex() {
        return mButtonIndex;
    }
}
