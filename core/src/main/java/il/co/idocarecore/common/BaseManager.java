package il.co.idocarecore.common;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for managers in the application
 * @param <LISTENER_CLASS> generics that designates the class of the listeners
 */
public class BaseManager<LISTENER_CLASS> {

    private Set<LISTENER_CLASS> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<LISTENER_CLASS, Boolean>(1));


    public void registerListener(@NonNull LISTENER_CLASS listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(@NonNull LISTENER_CLASS listener) {
        mListeners.remove(listener);
    }

    protected Set<LISTENER_CLASS> getListeners() {
        return mListeners;
    }

}
