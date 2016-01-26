package il.co.idocare.flows;

/**
 * Flow in our application is some predefined sequence of "steps" which can be executed sequentially
 * once some initial data and/or constraints have been set.<br>
 * Execution of Flow will usually be done in order to perform some "action" and we should try to
 * attain a one-to-one correspondence between "actions" executed in our application and Flow
 * implementations.<br>
 * More complex "program flows" that might require additional data and/or constraints during
 * execution (e.g. user input) can be broken down into multiple Flows.
 */
public interface Flow {


    // ---------------------------------------------------------------------------------------------
    //
    // Constants

    /**
     * The default state that should be returned by {@link Flow#getState()} of all Flows before
     * the execution. Flows that do not have additional internal states can stay in this state
     * during execution, but they must transition to either {@link Flow#STATE_EXECUTED_SUCCEEDED}
     * or {@link Flow#STATE_EXECUTED_FAILED} when execution completes.
     */
    public static final int STATE_NONE = 0;

    /**
     * The state that should be returned by {@link Flow#getState()} of all FLows if execution
     * completed without errors.
     */
    public static final int STATE_EXECUTED_SUCCEEDED = Integer.MAX_VALUE;

    /**
     * The state that should be returned by {@link Flow#getState()} of all FLows if execution
     * completed, but there were errors that prevented the Flow from fulfilling its functionality.
     */
    public static final int STATE_EXECUTED_FAILED = Integer.MAX_VALUE - 1;

    // End of constants
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Public methods

    /**
     * Execute the Flow. This method must not be called more than once.<br>
     * Note: implementations must not assume that this method will be called on some particular
     * thread.
     */
    public void execute();

    /**
     * Since Flow represents sequence of "steps", it might transition between multiple states during
     * execution. By implementing this method, Flows should provide external read access to this
     * information.
     * @return the current state of the flow
     */
    public int getState();

    /**
     * Register a new listener that should be notified when Flow's internal state changes. Has no
     * effect if the listener is already registered.
     * @param listener listener to register for notifications; mustn't be null
     */
    public void registerFlowStateChangeListener(FlowStateChangeListener listener);

    /**
     * Unregister a listener. Has no effect if the listener wasn't registered.
     * @param listener listener to unregister from notifications; mustn't be null
     */
    public void unregisterFlowStateChangeListener(FlowStateChangeListener listener);

    // End of public methods
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Inner classes

    /**
     * Classes implementing this interface can register with Flows in order to be notified when
     * Flow's state changes.
     */
    public interface FlowStateChangeListener {

        /**
         * This callback method will be invoked whenever Flow's internal state changes.<br>
         * Note: this method might be invoked on any thread.
         * @param newState the new state of the Flow
         */
        public void onFlowStateChanged(int newState);
    }

    // End of inner classes
    //
    // ---------------------------------------------------------------------------------------------

}
