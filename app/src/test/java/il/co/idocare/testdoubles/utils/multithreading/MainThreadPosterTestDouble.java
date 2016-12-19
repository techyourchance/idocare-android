package il.co.idocare.testdoubles.utils.multithreading;

import android.os.Handler;

import org.mockito.Mockito;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import il.co.idocare.utils.multithreading.MainThreadPoster;

/**
 * This is a test double of {@link MainThreadPoster} for unit tests. Instead of using the Main (UI)
 * Android's thread, this implementation sends each Runnable to a new background thread. Only one
 * background thread is allowed to run at a time, thus simulating a serial execution of Runnables.
 */

public class MainThreadPosterTestDouble extends MainThreadPoster {

    private final Object MONITOR = new Object();

    private Queue<Thread> mThreads = new LinkedBlockingQueue<>();

    @Override
    protected Handler getMainHandler() {
        return Mockito.mock(Handler.class);
    }

    @Override
    public void post(final Runnable runnable) {
        synchronized (MONITOR) {
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    // make sure all previous threads finished
                    MainThreadPosterTestDouble.this.join();
                    runnable.run();
                }
            });
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
