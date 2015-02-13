package il.co.idocare.controllers;

import android.os.Handler;

/**
 * MVC Controller interface.
 * MVC Controller's function is to communicate events between the MVC Views and MVC Models, as well
 * as to control the flow of the program based on these events.<br>
 * In our architecture the communication is carried out by Handlers associated with MVC Views,
 * Models and Controllers. Handlers approach was favored over Listeners approach because Handlers
 * are safer in multi-threading environment.
 */
public interface ControllerMVC   {

    /**
     * Get the inbox Handler associated with this MVC Controller. This Handler can be used by other
     * components (e.g. MVC Views) in order to notify this MVC Controller about events, but it
     * can also be used by the MVC Controller itself in order to enqueue parts of its code.<br>
     * Implementation note: Handler returned by this method might be associated with either UI
     * thread or some worker thread, and it is up to you to choose which approach to take
     * in each particular case.
     * @return Handler used by this MVC Controller in order to obtain messages and enqueue parts
     *         of its code for execution.
     */
    public Handler getInboxHandler();

    /**
     * Add an outbox Handler to this MVC Controller's set of outbox Handlers. This Handler can
     * belong to other components (e.g. MVC View) which will be notified of events/updates by this
     * MVC Controller.<br>
     * All outbox Handlers are "treated" the same in context of events/updates notifications.<br>
     * Implementations must enforce set property - no Handler is allowed to be added more than
     * once.
     * @param handler a new Handler to be "notified" of events/updates
     */
    public void addOutboxHandler(Handler handler);

    /**
     * Remove a particular outbox Handler from the set of outbox Handlers. The component associated
     * with this Handler will no longer receive events/updates notifications.
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
