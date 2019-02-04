package il.co.idocarecore.screens.common.mvcviews;

/**
 * This interface corresponds to MVC views that need to notify MVC controllers of input events
 */
public interface ObservableViewMvc<ListenerType> extends ViewMvc {

    /**
     * Register a listener that will be notified of any input events performed on this MVC view
     */
    void registerListener(ListenerType listener);

    /**
     * Unregister a previously registered listener. Does nothing if the listener wasn't registered.
     */
    void unregisterListener(ListenerType listener);

}
