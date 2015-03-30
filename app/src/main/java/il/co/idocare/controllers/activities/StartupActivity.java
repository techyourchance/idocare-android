package il.co.idocare.controllers.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.controllers.fragments.LoginFragment;
import il.co.idocare.controllers.fragments.SplashFragment;

/**
 * Created by Vasiliy on 3/23/2015.
 */
public class StartupActivity extends AbstractActivity {

    private static final String LOG_TAG = "StartupActivity";

    // ---------------------------------------------------------------------------------------------
    //
    // Activity lifecycle management


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_startup);

        // Decide which fragment to show if the app is not restored
        if (savedInstanceState == null) {

            SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_FILE, MODE_PRIVATE);

            if (prefs.contains(Constants.FieldName.USER_ID.getValue()) &&
                    prefs.contains(Constants.FieldName.USER_PUBLIC_KEY.getValue())) {
                // Show splash screen if user details exist
                replaceFragment(SplashFragment.class, false, null);
            } else {
                // Bring up login fragment
                replaceFragment(LoginFragment.class, false, null);
            }

        }

        /*
        TODO: make both login and splash fragments lead to MainActivity
         */

    }

    // End of activity lifecycle management
    //
    // ---------------------------------------------------------------------------------------------

}