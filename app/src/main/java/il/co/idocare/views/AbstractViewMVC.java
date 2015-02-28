package il.co.idocare.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.handlermessaging.HandlerMessagingMaster;
import il.co.idocare.handlermessaging.HandlerMessagingSlave;
import il.co.idocare.models.ModelMVC;

/**
 * This is an abstract implementation of ViewMVC interface which provides some convenience
 * logic specific to the app.
 */
public abstract class AbstractViewMVC implements
        ViewMVC,
        HandlerMessagingMaster,
        HandlerMessagingSlave {

    Handler mInboxHandler;
    final List<Handler> mOutboxHandlers = new ArrayList<Handler>();
    ModelMVC mModelMVC;


    // ---------------------------------------------------------------------------------------------
    //
    // Handler messaging methods


    /**
     * Handle the message received by the inbox Handler
     * @param msg message to handle
     */
    protected abstract void handleMessage(Message msg);

    @Override
    public Handler getInboxHandler() {
        // Since most of the work done in MVC Views consist of manipulations on underlying
        // Android Views, it will be convenient (and less error prone) if MVC View's inbox Handler
        // will be running on UI thread.
        if (mInboxHandler == null) {
            mInboxHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    AbstractViewMVC.this.handleMessage(msg);
                }
            };
        }
        return mInboxHandler;
    }

    @Override
    public void addOutboxHandler(Handler handler) {
        // Not sure that there will be use case that requires sync, but just as precaution...
        synchronized (mOutboxHandlers) {
            if (!mOutboxHandlers.contains(handler)) {
                mOutboxHandlers.add(handler);
            }
        }
    }

    @Override
    public void removeOutboxHandler(Handler handler) {
        // Not sure that there will be use case that requires sync, but just as precaution...
        synchronized (mOutboxHandlers) {
            mOutboxHandlers.remove(handler);
        }
    }

    @Override
    public void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj) {
        // Not sure that there will be use case that requires sync, but just as precaution...
        synchronized (mOutboxHandlers) {
            for (Handler handler : mOutboxHandlers) {
                Message msg = Message.obtain(handler, what, arg1, arg2, obj);
                msg.sendToTarget();
            }
        }

    }

    // End of Handler messaging methods
    //
    // ---------------------------------------------------------------------------------------------

}
