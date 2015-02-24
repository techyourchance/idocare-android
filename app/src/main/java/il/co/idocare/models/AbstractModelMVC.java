package il.co.idocare.models;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.handlermessaging.HandlerMessagingMaster;

/**
 * This is an abstract implementation of ModelMVC interface which provides some convenience logic
 * specific to the app<br>
 * MVC Models of this app should extend this class.
 */
public class AbstractModelMVC implements HandlerMessagingMaster {


    final List<Handler> mOutboxHandlers = new ArrayList<Handler>();

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

}
