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
     * Execute the Flow. Flows will usually be executed on background threads.
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
}
