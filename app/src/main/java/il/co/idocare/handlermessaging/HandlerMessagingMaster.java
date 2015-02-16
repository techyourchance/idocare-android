package il.co.idocare.handlermessaging;

import android.os.Handler;

/**
 * This interface represents the "Master" in messaging scheme - objects implementing this interface
 * can register "outbox" Handlers and send notifications and post Runnable objects to all these
 * Handlers at once.
 */
public interface HandlerMessagingMaster {

    /**
     * Add an outbox Handler to this Master's set of outbox Handlers. All outbox Handlers
     * are "treated" the same in context of events notifications by this Master.<br>
     * Implementations must enforce set property - no Handler is allowed to be added more than
     * once.
     * @param handler a new Handler to be "notified" of events
     */
    public void addOutboxHandler(Handler handler);

    /**
     * Remove a particular outbox Handler from the set of outbox Handlers. The object associated
     * with this Handler will no longer receive events notifications.
     * @param handler a Handler to remove
     */
    public void removeOutboxHandler(Handler handler);

    /**
     * Send a message notification (using {@link android.os.Message}) to all outbox Handlers.
     * @param what {@link android.os.Message#what}
     * @param arg1 {@link android.os.Message#arg1}
     * @param arg2 {@link android.os.Message#arg2}
     * @param obj {@link android.os.Message#obj}
     */
    public void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj);

}
