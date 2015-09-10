package il.co.idocare.controllers.activities;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.os.Bundle;

import il.co.idocare.R;
import il.co.idocare.authentication.AccountAuthenticator;
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


    private static final String LOG_TAG = LoginActivity.class.getSimpleName();


    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_frame_layout);

        if (savedInstanceState == null) {
            if (getIntent().hasExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)) {
                // If the above extra is present - this activity was started by
                // AccountAuthenticator. Therefore - show native login right away!
                replaceFragment(LoginNativeFragment.class, false, true, getIntent().getExtras());
            } else {
                replaceFragment(LoginChooserFragment.class, false, true, getIntent().getExtras());
            }

        }


        // This part was copy-pasted from AccountAuthenticatorActivity
        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }

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

}
