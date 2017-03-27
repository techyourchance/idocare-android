package il.co.idocare.mvcviews;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the base class for MVC views' implementations which provides basic common functionality
 */
public abstract class AbstractViewMvc<ListenerType> implements ObservableViewMvc<ListenerType> {

    private View mRootView;

    // thread safe set of listeners (maybe an overkill, but prefer to be on the safe side)
    private Set<ListenerType> mListeners =
            Collections.newSetFromMap(new ConcurrentHashMap<ListenerType, Boolean>(1));

    @Override
    public void registerListener(ListenerType listener) {
        if (listener != null) mListeners.add(listener);
    }

    @Override
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

    protected <T extends View> T findViewById(@IdRes int id) {
        return (T) mRootView.findViewById(id);
    }

    protected Context getContext() {
        return getRootView().getContext();
    }

    protected String getString(@StringRes int id) {
        return getContext().getString(id);
    }


}
