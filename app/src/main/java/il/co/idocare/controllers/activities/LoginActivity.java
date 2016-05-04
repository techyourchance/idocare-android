package il.co.idocare.controllers.activities;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import il.co.idocare.R;
import il.co.idocare.controllers.fragments.LoginChooserFragment;
import il.co.idocare.controllers.fragments.LoginNativeFragment;

/**
 * This activity takes care of login process. This activity is also used by AccountAuthenticator
 * service.
 */
public class LoginActivity extends AbstractActivity {

    public final static String ARG_ACCOUNT_NAME = "il.co.idocare.accountName";
    public static final String ARG_ACCOUNT_TYPE = "il.co.idocare.accountType";

    /**
     * When set to a strictly positive integer, this entry in the launching intent's extra
     * indicates that this activity was started from StartupActivity.
     * If the launching activity was StartupActivity, then MainActivity will be launched when
     * this activity finishes. Otherwise, this activity will be simply finished (rely on backstack)
     */
    public static final String ARG_LAUNCHED_FROM_STARTUP_ACTIVITY = "launched_from_startup_activity";


    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_single_frame_with_toolbar);

        initActionBar();

        if (savedInstanceState == null) {
            if (getIntent().hasExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)) {
                // If the above extra is present - this activity was started by
                // AccountAuthenticator. Therefore - show native login right away!
                replaceFragment(LoginNativeFragment.class, false, true, getIntent().getExtras());
            } else {
                Bundle args;
                if (getIntent() != null && getIntent().getExtras() != null)
                    args = getIntent().getExtras();
                else
                    args = new Bundle();
                args.putInt(LoginChooserFragment.ARG_PLAY_ANIMATION, 1);
                replaceFragment(LoginChooserFragment.class, false, true, args);
            }

        }


        // This part was copy-pasted from AccountAuthenticatorActivity
        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }

    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavigateUp();
            }
        });
    }


    // ---------------------------------------------------------------------------------------------
    //
    // The below methods were copy-pasted from AccountAuthenticatorActivity

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }

    @Override
    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
