package il.co.idocare.sequences;

import android.util.Log;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for implementations of {@link Sequence} which contain common functionality.
 */
public abstract class AbstractSequence implements Sequence {


    private final Object STATE_LOCK = new Object();

    private int mState = STATE_NONE;
    private boolean mExecuted = false;

    // the set of listeners must be thread safe
    private Set<StateChangeListener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<StateChangeListener, Boolean>(1));

    /**
     * Subclasses must override this method with code that should be invoked upon Sequence
     * execution.
     */
    protected abstract void doWork();

    /**
     * @return the name of concrete implementation of Sequence for logging purposes
     */
    protected abstract String getName();

    @Override
    public void execute() {
        synchronized (STATE_LOCK) {
            if (mExecuted) {
                throw new IllegalStateException("the Sequence has already been executed");
            }

            Log.d(getName(), "started Sequence execution");
            mExecuted = true;
        }

        doWork();
    }

    @Override
    public final void executeInBackground() {

        // TODO: come up with a more sophisticated execution scheme
        new Thread(new Runnable() {
            @Override
            public void run() {
                execute();
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
     * Change the state of this Sequence. Has no effect if the new state is the same one as the
     * current state.<br>
     * Note: this state is externally visible.
     * @param newState new state of the Sequence.
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
    public void registerStateChangeListener(StateChangeListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener mustn't be null");
        mListeners.add(listener);
    }

    @Override
    public void unregisterSequenceStateChangeListener(StateChangeListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener mustn't be null");
        mListeners.remove(listener);
    }

    private void notifyStateChanged(int newState) {
        for (StateChangeListener listener : mListeners) {
            Log.d(getName(), "notifying the listener about state change; listener: " + listener);
            listener.onSequenceStateChanged(newState);
        }
    }
}
