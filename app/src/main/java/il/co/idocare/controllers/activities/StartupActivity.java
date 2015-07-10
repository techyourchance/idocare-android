package il.co.idocare.controllers.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import il.co.idocare.R;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.fragments.SplashFragment;

/**
 * This startup activity is the main entry point into the app.
 */
public class StartupActivity extends AbstractActivity {

    private static final String LOG_TAG = StartupActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_startup);


        /*
        VZ: Since all we do in SplashFragment is just show some graphics, and there are no other
        fragments shown in StartupActivity, we could make this activity fragmentless (thus
        simplifying things). I chose to keep "fragmentation" in order to be able to easily extend
        the functionality of startup activity in the future (if such a need arises)
         */
        if (savedInstanceState == null) {
            replaceFragment(SplashFragment.class, false, true, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        // Ask for an auth token
        final AccountManagerFuture<Bundle> future = AccountManager.get(this).getAuthTokenByFeatures(
                AccountAuthenticator.ACCOUNT_TYPE,
                AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT,
                null,
                this,
                null,
                null,
                null,
                null);

        // This async task waits for an auth token to be obtained and handles errors (if any)
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {

                Long initTime = System.currentTimeMillis();

                try {
                    Bundle result = future.getResult();

                    // Make sure that the splash fragment is shown at least 5 seconds
                    long currTime = System.currentTimeMillis();
                    if (currTime < initTime + 2*1000) {
                        try {
                            Thread.sleep(initTime + 2*1000 - currTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    return true;
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return false;
            }

            @Override
            protected void onPostExecute(Boolean authSuccessful) {
                if (authSuccessful) {
                    // Perform immediate sync at startup
                    Bundle settingsBundle = new Bundle();
                    settingsBundle.putBoolean(
                            ContentResolver.SYNC_EXTRAS_MANUAL, true);
                    settingsBundle.putBoolean(
                            ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                    ContentResolver.requestSync(getActiveAccount(), IDoCareContract.AUTHORITY,
                            settingsBundle);

                    Intent intent = new Intent(StartupActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // TODO: consider handling this error case in a different way
                    Log.i(LOG_TAG, "Could not obtain auth token");
                    Toast.makeText(StartupActivity.this, "Could not obtain auth token", Toast.LENGTH_LONG).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}