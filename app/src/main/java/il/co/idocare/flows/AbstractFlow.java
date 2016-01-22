package il.co.idocare.flows;

/**
 * Base class for Flows which contain common functionality.
 */
public abstract class AbstractFlow implements Flow {

    private final Object STATE_LOCK = new Object();

    private int mState = 0;

    @Override
    public int getState() {
        synchronized (STATE_LOCK) {
            return mState;
        }
    }

    /**
     * Change the state of this Flow. Has no effect if the new state is the same one as the
     * current state.<br>
     * Note: this state is externally visible.
     * @param newState new state of the Flow.
     */
    protected void setState(int newState) {
        synchronized (STATE_LOCK) {
            mState = newState;
        }
    }

}
