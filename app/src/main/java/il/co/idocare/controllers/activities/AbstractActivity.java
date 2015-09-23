package il.co.idocare.controllers.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.FacebookSdk;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.fragments.IDoCareFragmentCallback;
import il.co.idocare.controllers.fragments.IDoCareFragmentInterface;

/**
 * This is a wrapper around a standard Activity class which provides few convenience methods
 */
public abstract class AbstractActivity extends AppCompatActivity implements
        IDoCareFragmentCallback {

    private static final String LOG_TAG = AbstractActivity.class.getSimpleName();


    private UserStateManager mUserStateManager;

    private Runnable mPostLoginRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserStateManager = new UserStateManager(this);

        mPostLoginRunnable = null;

        FacebookSdk.sdkInitialize(getApplicationContext());

    }

    @Override
    public void onBackPressed() {
        if (onNavigateUp()) {
            return;
        }

        super.onBackPressed();
    }




    // ---------------------------------------------------------------------------------------------
    //
    // Fragments management

    // TODO: maybe we need to preserve the state of the replaced fragments?
    @Override
    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                boolean clearBackStack, Bundle args) {

        if (clearBackStack) {
            // Remove all entries from back stack
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }


        if (isFragmentShown(claz)) {
            // The requested fragment is already shown - nothing to do
            // Log.v(LOG_TAG, "the fragment " + claz.getSimpleName() + " is already shown");
            return;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // Create new fragment
        Fragment newFragment;

        try {
            newFragment = claz.newInstance();
            if (args != null) newFragment.setArguments(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        // Change to a new fragment
        ft.replace(R.id.frame_contents, newFragment, claz.getClass().getSimpleName());
        ft.commit();


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getFragmentManager().executePendingTransactions();
            }
        });

    }

    /**
     * Check whether a fragment of a specific class is currently shown
     * @param claz class of fragment to test. Null considered as "test no fragment shown"
     * @return true if fragment of the same class (or a superclass) is currently shown
     */
    private boolean isFragmentShown(Class<? extends Fragment> claz) {
        Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);


        return (currFragment == null && claz == null) || (
                currFragment != null && claz.isInstance(currFragment));
    }


    @Override
    public boolean onNavigateUp() {

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            return true;
        } else {
            Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);
            // Check if currently shown fragment is of type IDoCareFragmentInterface
            if (currFragment != null &&
                    IDoCareFragmentInterface.class.isAssignableFrom(currFragment.getClass())) {
                // Get the hierarchical parent of the currently shown fragment
                Class<? extends Fragment> hierParent =
                        ((IDoCareFragmentInterface)currFragment).getNavHierParentFragment();

                if (hierParent != null) {
                    replaceFragment(hierParent, false, true, null);
                    return true;
                }
            }
        }

        return false;
    }


    // End of fragments management
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // User state management

    @Override
    public UserStateManager getUserStateManager() {
        return mUserStateManager;
    }


    private Account getActiveOrDummyAccount() {
        Account account = getUserStateManager().getActiveAccount();

        if (account != null) {
            return account;
        } else {
            account = new Account(AccountAuthenticator.DUMMY_ACCOUNT_NAME,
                    AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);
            // TODO: this call returns false in case the account exists or error ocurred - handle both separately
            AccountManager.get(this).addAccountExplicitly(account, null, null);
            AccountManager.get(this).setAuthToken(
                    account,
                    AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT,
                    AccountAuthenticator.DUMMY_ACCOUNT_AUTH_TOKEN);
            return account;
        }
    }


    @Override
    public void askUserToLogIn(String message, final Runnable runnable) {
        // This listener will handle dialog button clicks
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        initiateLoginFlow(runnable);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(AbstractActivity.this);
        builder.setMessage(message)
                .setPositiveButton(getResources().getString(R.string.btn_dialog_positive),
                        dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.btn_dialog_negative),
                        dialogClickListener)
                .show();
    }

    /**
     * Initiate a flow that will take the user through login process
     */
    public void initiateLoginFlow(@Nullable final Runnable runnable) {
        if (runnable != null) {
            // Ensure that we do not overwrite runnables
            if (mPostLoginRunnable != null)
                Log.e(LOG_TAG, "tried to set a new Runnable " +
                        "for post login execution while the previous one " +
                        "hasn't been consumed yet!");

            mPostLoginRunnable = runnable;
        }
        Intent intent = new Intent(AbstractActivity.this, LoginActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN);
    }

    /**
     * Initiate a flow that will take the user through logout process
     */
    public void initiateLogoutFlow(@Nullable Runnable runnable) {
        getUserStateManager().logOut();
        if (runnable != null)
            runOnUiThread(runnable);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {
            case Constants.REQUEST_CODE_LOGIN:
                // If there is a logged in user after login activity finished and the
                // runnable was set - execute it on main thread
                if (getUserStateManager().getActiveAccount() != null
                        && mPostLoginRunnable != null) {
                    runOnUiThread(mPostLoginRunnable);
                }
                mPostLoginRunnable = null; // In any case - clear the runnable
                return;

            default:
                break;
        }

        /*
        This code is required in order to support Facebook's LoginButton functionality -
        since we do not use support Fragments, we can't use LoginButton.setFragment() call,
        thus not allowing the Fragments to receive onActivityResult() notifications after FB
        login events. This code redirects the call to the visible fragment.
        NOTE: this code is based on the assumption that all subclasses will have FrameLayout
              named "frame_contents"
         */
        Fragment currFragment = getFragmentManager().findFragmentById(R.id.frame_contents);
        if (currFragment != null)
            currFragment.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    // End of user state management
    //
    // ---------------------------------------------------------------------------------------------




    // ---------------------------------------------------------------------------------------------
    //
    // Sync settings management

    public void enableAutomaticSync() {
        Account acc = getActiveOrDummyAccount();
        ContentResolver.setIsSyncable(acc, IDoCareContract.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(acc, IDoCareContract.AUTHORITY, true);
    }

    public void disableAutomaticSync() {
        Account acc = getActiveOrDummyAccount();
        ContentResolver.setIsSyncable(acc, IDoCareContract.AUTHORITY, 0);
    }

    @Override
    public void requestImmediateSync() {
        Account acc = getActiveOrDummyAccount();

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(acc, IDoCareContract.AUTHORITY, settingsBundle);
    }

    // End of sync settings management
    //
    // ---------------------------------------------------------------------------------------------


}
