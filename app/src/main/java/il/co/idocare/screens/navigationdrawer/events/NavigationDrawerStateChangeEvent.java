package il.co.idocare.screens.navigationdrawer.events;

/**
 * This event will be posted to event bus when navigation drawer's state changes
 */

public class NavigationDrawerStateChangeEvent {

    public static final int STATE_OPENED = 1;
    public static final int STATE_CLOSED = 2;

    private final int mState;

    /**
     * @param state one of the STATE_ constants defined in this class
     */
    public NavigationDrawerStateChangeEvent(int state) {
        if (state != STATE_OPENED && state != STATE_CLOSED) {
            throw new IllegalArgumentException("invalid state");
        }
        mState = state;
    }

    public int getState() {
        return mState;
    }
}
