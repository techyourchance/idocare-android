package il.co.idocare.utils.multithreading;

import android.os.Handler;
import android.os.Looper;

/**
 * This object should be used whenever we need to post anything to UI thread.
 */
public class MainThreadPoster {

    private Handler mMainHandler;

    public MainThreadPoster() {
        mMainHandler = getMainHandler();
    }

    protected  Handler getMainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    public void post(Runnable runnable) {
        mMainHandler.post(runnable);
    }

}
