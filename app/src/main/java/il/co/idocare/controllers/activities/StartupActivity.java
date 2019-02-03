package il.co.idocare.controllers.activities;

import android.content.Intent;
import android.os.Bundle;

import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import javax.inject.Inject;

import il.co.idocare.R;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocare.controllers.fragments.SplashFragment;

/**
 * This startup activity is the main entry point into the app.
 */
public class StartupActivity extends AbstractActivity {

    private static final String TAG = "StartupActivity";

    private static final String KEY_INIT_TIME = "KEY_INIT_TIME";

    @Inject LoginStateManager mLoginStateManager;
    @Inject BackgroundThreadPoster mBackgroundThreadPoster;
    @Inject UiThreadPoster mUiThreadPoster;

    private long mInitTime = 0;

    private boolean mResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getControllerComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_single_frame);

        if (savedInstanceState == null) {
            mInitTime = System.currentTimeMillis();
            replaceFragment(SplashFragment.class, false, true, null);
        } else {
            mInitTime = savedInstanceState.getLong(KEY_INIT_TIME);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_INIT_TIME, mInitTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        waitAndThenSwitchToNextActivity();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    private void waitAndThenSwitchToNextActivity() {

        // perform on background thread
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                long currTime = System.currentTimeMillis();

                // wait if needed
                if (currTime < mInitTime + 2 * 1000) {
                    try {
                        Thread.sleep(mInitTime + 2 * 1000 - currTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // switch to next activity on main thread
                mUiThreadPoster.post(new Runnable() {
                    @Override
                    public void run() {
                        switchToNextActivityIfResumed();
                    }
                });
            }
        });
    }

    private void switchToNextActivityIfResumed() {

        if (!mResumed) return;

        Intent intent;
        boolean disableExitAnimation = false;

        if (mLoginStateManager.isLoggedIn()) {
            // If the user is logged in - show the MainFragment
            intent = new Intent(StartupActivity.this, MainActivity.class);
        } else {
            if (mLoginStateManager.isLoginSkipped()) {
                // If the user has already chosen to skip login at startup - switch to
                // MainActivity right away.
                intent = new Intent(StartupActivity.this, MainActivity.class);
            } else {
                // Present a login screen to the user, but make sure that LoginActivity knows
                // that is has been started from StartupActivity
                intent = new Intent(StartupActivity.this, LoginActivity.class);
                intent.putExtra(LoginActivity.ARG_LAUNCHED_FROM_STARTUP_ACTIVITY, 1);
                // Disable entry animation
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                disableExitAnimation = true;
            }
        }

        startActivity(intent);
        finish();

        if (disableExitAnimation) {
            overridePendingTransition(0, 0);
        }
    }


    @Override
    public void setTitle(String title) {
        // This method is irrelevant here
    }

}