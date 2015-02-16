package il.co.idocare.handlermessaging;

import android.os.Handler;


/**
 * This interface represents the "Slave" in messaging scheme - components implementing this
 * interface can provide their "inbox" Handlers to other components (Masters) which may
 * use it in order to notify this Slave of events and post Runnable objects to it.
 */
public interface HandlerMessagingSlave {

    /**
     * Get the inbox Handler associated with this Slave. This Handler can be used by other
     * components (Masters) in order to notify this Slave about events and post Runnable objects.<br>
     * Implementation note: Handler returned by this method might be associated with either UI
     * thread or any other thread, and it is up to you to choose which approach to take
     * in each particular case.
     * @return Handler used by this Slave in order to obtain messages.
     */
    public Handler getInboxHandler();


}
