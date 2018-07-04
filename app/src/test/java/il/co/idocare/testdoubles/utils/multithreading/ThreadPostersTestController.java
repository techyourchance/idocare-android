package il.co.idocare.testdoubles.utils.multithreading;

import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import il.co.idocare.utils.multithreading.MainThreadPoster;

/**
 * Objects of this class can be used in order to test components that depend on
 * {@link MainThreadPoster} and {@link BackgroundThreadPoster}
 */

public class ThreadPostersTestController {

    private MainThreadPosterTestDouble mMainThreadPosterTestDouble;
    private BackgroundThreadPosterTestDouble mBackgroundThreadPosterTestDouble;

    public ThreadPostersTestController() {
        mMainThreadPosterTestDouble = new MainThreadPosterTestDouble();
        mBackgroundThreadPosterTestDouble = new BackgroundThreadPosterTestDouble();
    }

    public MainThreadPoster getMainThreadPoster() {
        return mMainThreadPosterTestDouble;
    }

    public BackgroundThreadPoster getBackgroundThreadPoster() {
        return mBackgroundThreadPosterTestDouble;
    }

    /**
     * Call to this method will block the calling thread until all {@link Runnable} posted
     * to both thread posters obtained with {@link #getMainThreadPoster()} and
     * {@link #getBackgroundThreadPoster()} are finished.<br><br>
     *
     */
    public void join() {
        mBackgroundThreadPosterTestDouble.join();
        mMainThreadPosterTestDouble.join();
    }
}
