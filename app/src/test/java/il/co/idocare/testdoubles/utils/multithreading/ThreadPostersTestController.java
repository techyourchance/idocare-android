package il.co.idocare.testdoubles.utils.multithreading;


import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.BackgroundThreadPosterTestDouble;
import com.techyourchance.threadposter.UiThreadPoster;
import com.techyourchance.threadposter.UiThreadPosterTestDouble;

/**
 * Objects of this class can be used in order to test components that depend on
 * {@link UiThreadPoster} and {@link BackgroundThreadPoster}
 */

public class ThreadPostersTestController {

    private UiThreadPosterTestDouble mUiThreadPosterTestDouble;
    private BackgroundThreadPosterTestDouble mBackgroundThreadPosterTestDouble;

    public ThreadPostersTestController() {
        mUiThreadPosterTestDouble = new UiThreadPosterTestDouble();
        mBackgroundThreadPosterTestDouble = new BackgroundThreadPosterTestDouble();
    }

    public UiThreadPoster getUiThreadPoster() {
        return mUiThreadPosterTestDouble;
    }

    public BackgroundThreadPoster getBackgroundThreadPoster() {
        return mBackgroundThreadPosterTestDouble;
    }

    /**
     * Call to this method will block the calling thread until all {@link Runnable} posted
     * to both thread posters obtained with {@link #getUiThreadPoster()} and
     * {@link #getBackgroundThreadPoster()} are finished.<br><br>
     *
     */
    public void join() {
        mBackgroundThreadPosterTestDouble.join();
        mUiThreadPosterTestDouble.join();
    }
}
