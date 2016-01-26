package il.co.idocare.sequences;

/**
 * Sequence in our application is a list of predefined "steps" which can be executed sequentially
 * once some initial data and/or constraints have been set.<br>
 * Execution of Sequence will usually be done in order to perform some "action" and we should try to
 * attain a one-to-one correspondence between "actions" executed in our application and Sequence
 * implementations.<br>
 * More complex program flows that might require additional data and/or constraints during
 * execution (e.g. user input) can be broken down into multiple Sequences.
 */
public interface Sequence {

    // ---------------------------------------------------------------------------------------------
    //
    // Constants

    /**
     * The default state that should be returned by {@link Sequence#getState()} of all Sequences
     * before the execution. Sequences that do not have additional internal states can stay in this
     * state during execution, but they must transition to either
     * {@link Sequence#STATE_EXECUTED_SUCCEEDED} or {@link Sequence#STATE_EXECUTED_FAILED}
     * when execution completes.
     */
    public static final int STATE_NONE = 0;

    /**
     * The state that should be returned by {@link Sequence#getState()} of all Sequences if
     * execution completed without errors.
     */
    public static final int STATE_EXECUTED_SUCCEEDED = Integer.MAX_VALUE;

    /**
     * The state that should be returned by {@link Sequence#getState()} of all Sequences if
     * execution completed, but there were errors that prevented the Sequence from fulfilling
     * its functionality.
     */
    public static final int STATE_EXECUTED_FAILED = Integer.MAX_VALUE - 1;

    // End of constants
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Public methods

    /**
     * Execute the Sequence. This method must not be called more than once.<br>
     * Note: implementations must not assume that this method will be called on some particular
     * thread.
     */
    public void execute();

    /**
     * Since Sequence represents sequence of "steps", it might transition between multiple states
     * during execution. By implementing this method, Sequences should provide external read access
     * to this information.
     * @return the current state of the Sequence
     */
    public int getState();

    /**
     * Register a new listener that should be notified when Sequence's internal state changes. Has
     * no effect if the listener is already registered.
     * @param listener listener to register for notifications; mustn't be null
     */
    public void registerSequenceStateChangeListener(SequenceStateChangeListener listener);

    /**
     * Unregister a listener. Has no effect if the listener wasn't registered.
     * @param listener listener to unregister from notifications; mustn't be null
     */
    public void unregisterSequenceStateChangeListener(SequenceStateChangeListener listener);

    // End of public methods
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Inner classes

    /**
     * Classes implementing this interface can register with Sequence in order to be notified when
     * Sequence's state changes.
     */
    public interface SequenceStateChangeListener {

        /**
         * This callback method will be invoked whenever Sequence's internal state changes.<br>
         * Note: this method might be invoked on any thread.
         * @param newState the new state of the Sequence
         */
        public void onSequenceStateChanged(int newState);
    }

    // End of inner classes
    //
    // ---------------------------------------------------------------------------------------------

}
