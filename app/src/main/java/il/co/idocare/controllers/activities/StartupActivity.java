package il.co.idocare.controllers.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.controllers.fragments.SplashFragment;

/**
 * This startup activity is the main entry point into the app.
 */
public class StartupActivity extends AbstractActivity {

    private static final String LOG_TAG = StartupActivity.class.getSimpleName();


    private long mInitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_single_frame);


        if (savedInstanceState == null) {

            mInitTime = System.currentTimeMillis();

            replaceFragment(SplashFragment.class, false, true, null);

        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // This asynctask waits for a predefined amount of time and then switches to MainActivity
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                long currTime = System.currentTimeMillis();

                if (currTime < mInitTime + 2*1000) {
                    try {
                        Thread.sleep(mInitTime + 2*1000 - currTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return null;

            }

            @Override
            protected void onPostExecute(Void obj) {

                UserStateManager userStateManager = new UserStateManager(StartupActivity.this);

                Intent intent;

                //this code don't need to know about hte exact mechanism the user used to log in
                // TODO: remove unhealthy dependencies once proper login flow established
                if (userStateManager.isLoggedInNative()) {
                    // If the user is logged in - show the MainFragment
                    intent = new Intent(StartupActivity.this, MainActivity.class);
                } else {
                    SharedPreferences prefs =
                            getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

                    if (prefs.getInt(Constants.LOGIN_SKIPPED_KEY, 0) > 0) {
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

                        // This need to be placed here in order to override exit animation
                        startActivity(intent);
                        finish();

                        // Disable exit animation
                        overridePendingTransition(0, 0);

                        return;
                    }

                    // TODO: just in case FB logout wasn't completed - remove once proper login flow established
                    if (userStateManager.isLoggedInWithFacebook())
                        userStateManager.logOut();
                }

                startActivity(intent);
                finish();

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void setTitle(String title) {
        // This method is irrelevant here
    }

}