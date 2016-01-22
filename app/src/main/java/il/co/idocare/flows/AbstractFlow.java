package il.co.idocare.flows;

import android.util.Log;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for Flows which contain common functionality.
 */
public abstract class AbstractFlow implements Flow {

    private final Object STATE_LOCK = new Object();

    private int mState = 0;

    // the set of listeners must be thread safe
    private Set<FlowStateChangeListener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<FlowStateChangeListener, Boolean>(1));

    /**
     * Subclasses must override this method with code that should be invoked upon Flows
     * execution.
     */
    protected abstract void doWork();

    /**
     * @return the name of concrete implementation of Flow for logging purposes
     */
    protected abstract String getName();

    @Override
    public final void execute() {
        Log.d(getName(), "started flow execution");
        // TODO: come up with a more sophisticated execution scheme
        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }).start();
    }

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
            Log.d(getName(), "setState; current state: " + mState +"; " +
                    "new state: " + newState);
            if (newState == mState) {
                Log.d(getName(), "setState; current and new states are identical - aborting");
            } else {
                mState = newState;
                notifyStateChanged(newState);
            }
        }
    }

    @Override
    public void registerFlowStateChangeListener(FlowStateChangeListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener mustn't be null");
        mListeners.add(listener);
    }

    @Override
    public void unregisterFlowStateChangeListener(FlowStateChangeListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener mustn't be null");
        mListeners.remove(listener);
    }

    private void notifyStateChanged(int newState) {
        for (FlowStateChangeListener listener : mListeners) {
            Log.d(getName(), "notifying the listener about state change; listener: " + listener);
            listener.onFlowStateChanged(newState);
        }
    }
}
