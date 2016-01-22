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

    /**
     * Execute the Flow.<br>
     * Note: implementations must not assume that this method will be called on some particular
     * thread.
     */
    public void execute();

    /**
     * Since Flow represents sequence of "steps", it might transition between multiple states during
     * execution. By implementing this method, Flows that do have internal states should provide
     * external read access to this information.
     * @return the current state of the flow; always returns 0 if Flow doesn't have or doesn't
     *         publish its internal state.
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
}
