package il.co.idocare.views;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import il.co.idocare.models.ModelMVC;

/**
 * MVC view interface.
 * MVC view is a "dumb" component used for presenting information to the user.<br>
 * Please note that MVC view is not the same as Android View - MVC view will usually wrap one or
 * more Android View's while adding logic for communication with MVC Controller and MVC Model.
 */
public interface ViewMVC {

    /**
     * Get the root Android View which is used internally by this MVC View for presenting data
     * to the user.<br>
     * The returned Android View might be used by an MVC Controller in order to query or alter the
     * properties of either the root Android View itself, or any of its child Android View's.
     * @return root Android View of this MVC View
     */
    public View getRootView();

    /**
     * Get the inbox Handler associated with this MVC View. This Handler can be used by other
     * components (e.g. MVC Controller) in order to notify this MVC View about events.<br>
     * Implementation note: Handler returned by this method might be associated with either UI
     * thread or some worker thread, and it is up to you to choose which approach to take
     * in each particular case.
     * @return Handler used by this MVC View in order to obtain messages.
     */
    public Handler getInboxHandler();

    /**
     * Add an outbox Handler to this MVC View's set of outbox Handlers. This Handler can
     * belong to other components (e.g. MVC Controller) which will be notified of user interaction
     * events by this MVC View.
     * All outbox Handlers are "treated" the same in context of events notifications.<br>
     * Implementations must enforce set property - no Handler is allowed to be added more than
     * once.
     * @param handler a new Handler to be "notified" of events
     */
    public void addOutboxHandler(Handler handler);

    /**
     * Remove a particular outbox Handler from the set of outbox Handlers. The component associated
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

    /**
     * Bind this MVC View to a particular MVC Model. The view is allowed to query the model for
     * its attributes in order to populate child Android Views of this MVC View.
     * @param model MVC Model which this MVC View will be bound to
     */
    public void bindModel(ModelMVC model);

    /**
     * Get the MVC Model this MVC View is bound to
     * @return MVC Model this MVC View is bound to, or null if there is none
     */
    public ModelMVC getBoundModel();

    /**
     * Unbind this MVC View from MVC Model it is bound to (if at all)
     */
    public void unbindModel();

    /**
     * This method aggregates all the information about the state of this MVC View into Bundle
     * object.
     * @return Bundle containing the state of this MVC View
     */
    public Bundle getViewState();
}
