package il.co.idocare.testdoubles.utils.multithreading;


import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import il.co.idocare.utils.multithreading.BackgroundThreadPoster;

/**
 * This is a test double of {@link BackgroundThreadPoster} for unit tests. This implementation
 * sends each Runnable to a new background thread.
 */

public class BackgroundThreadPosterTestDouble extends BackgroundThreadPoster {

    private final Object MONITOR = new Object();

    private Queue<Thread> mThreads = new LinkedBlockingQueue<>();


    @Override
    public void post(Runnable runnable) {
        synchronized (MONITOR) {
            Thread worker = new Thread(runnable);
            worker.start();
            mThreads.add(worker);
        }
    }

    /**
     * Call to this method will block until all Runnables posted to this "test double" UNTIL THE
     * MOMENT OF A CALL will be completed.
     */
    public void join() {
        Queue<Thread> threadsCopy;
        synchronized (MONITOR) {
            threadsCopy = new LinkedBlockingQueue<>(mThreads);
        }

        Thread thread;
        while ((thread = threadsCopy.poll()) != null) {
            try {
                // there is race condition - "self" thread and further threads could be added; we need
                // to wait only until threads before "self" finish
                if (thread.getId() == Thread.currentThread().getId()) {
                    break;
                } else {
                    thread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
