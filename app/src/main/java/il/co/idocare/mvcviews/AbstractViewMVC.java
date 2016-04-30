package il.co.idocare.mvcviews;

import android.support.annotation.NonNull;
import android.view.View;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the base class for MVC views' implementations which provides basic common functionality
 */
public abstract class AbstractViewMVC<ListenerType> implements ViewMVC {

    private View mRootView;

    // thread safe set of listeners (maybe an overkill, but prefer to be on the safe side)
    private Set<ListenerType> mListeners =
            Collections.newSetFromMap(new ConcurrentHashMap<ListenerType, Boolean>(1));

    /**
     * Register a listener that will be notified of any input events performed on this MVC view
     */
    public void registerListener(ListenerType listener) {
        if (listener != null) mListeners.add(listener);
    }

    /**
     * Unregister a previously registered listener. Does nothing if the listener wasn't registered.
     */
    public void unregisterListener(ListenerType listener) {
        mListeners.remove(listener);
    }

    /**
     * Get a reference to the (thread safe) set containing all the registered listeners. Note that
     * the returned reference is a reference to the set itself, not to its copy.
     */
    protected Set<ListenerType> getListeners() {
        return mListeners;
    }

    /**
     * Set the root android view of this MVC view
     */
    protected void setRootView(@NonNull View rootView) {
        mRootView = rootView;
    }

    @Override
    public View getRootView() {
        return mRootView;
    }
}
