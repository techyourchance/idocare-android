package il.co.idocare.utils.multithreading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This object should be used whenever we need to post anything to a (random) background thread.
 */
public class BackgroundThreadPoster {

    ExecutorService mExecutorService = Executors.newCachedThreadPool();

    public void post(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

}
