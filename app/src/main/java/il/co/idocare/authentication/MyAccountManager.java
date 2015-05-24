package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import il.co.idocare.Constants;

/**
 * This class provides some convenience methods that utilize AccountManager's APIs
 */
public class MyAccountManager {


    // TODO: if this class is not used - remove it.


    /**
     * This method returns the auth token associated with the "active account" (the last chosen
     * account as is stored in SharedPreferences).
     *
     * If there is no active account entry in SharedPreferences, or the active account has been
     * removed from the device, or auth token was invalidated - authentication activities will be
     * started to prompt the user for credentials.
     *
     * @param activity The {@link Activity} context to use for launching new
     *     sub-Activities to prompt to add an account, select an account,
     *     and/or enter a password, as necessary; used only to call
     *     startActivity(); should not be null
     * @return An {@link AccountManagerFuture} which resolves to a Bundle with
     *     at least the following fields:
     * <ul>
     * <li> {@link AccountManager#KEY_ACCOUNT_NAME} - the name of the account you supplied
     * <li> {@link AccountManager#KEY_ACCOUNT_TYPE} - the type of the account
     * <li> {@link AccountManager#KEY_AUTHTOKEN} - the auth token you wanted
     * </ul>
     *
     * (Other authenticator-specific values may be returned.)  If an auth token
     * could not be fetched, {@link AccountManagerFuture#getResult()} throws:
     * <ul>
     * <li> {@link AuthenticatorException} if the authenticator failed to respond
     * <li> {@link OperationCanceledException} if the operation is canceled for
     *      any reason, incluidng the user canceling a credential request
     * <li> {@link IOException} if the authenticator experienced an I/O problem
     *      creating a new auth token, usually because of network trouble
     * </ul>
     */
    public static AccountManagerFuture<Bundle> getAuthTokenForActiveAccount(Activity activity) {
        if (activity == null) throw new IllegalArgumentException("the argument mustn't be null");

        GetAuthTokenForActiveAccountThread thread = new GetAuthTokenForActiveAccountThread(activity);
        thread.start();

        return thread;

    }


    private static class GetAuthTokenForActiveAccountThread extends Thread implements
            AccountManagerFuture<Bundle> {

        private final Object LOCK = new Object();

        private final Activity mActivity;
        private AccountManagerFuture<Bundle> mFuture;

        public GetAuthTokenForActiveAccountThread(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void run() {

            SharedPreferences prefs =
                    mActivity.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

            // Retrieve the entry for active account from the SharedPreferences
            String accountName = prefs.getString(AccountManager.KEY_ACCOUNT_NAME, "");

            Account[] existingAccounts = AccountManager
                    .get(mActivity).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);


            // Ensure that there is a valid entry for active account
            if (TextUtils.isEmpty(accountName)) {
                // TODO: pop up account picker screen if there are IDoCare accounts, log in screen if not

            }

            Account account;
            account = new Account(accountName, AccountAuthenticator.ACCOUNT_TYPE);

            // Ensure that the active account is still registered on the device
            if (!Arrays.asList(existingAccounts).contains(account)) {
                // TODO: pop up account picker screen if there are IDoCare accounts, log in screen if not
                // TODO: consider using the data of active account for creating the same account again

            }

            synchronized (LOCK) {
                mFuture = AccountManager.get(mActivity).getAuthToken(
                        account,
                        AccountAuthenticator.ACCOUNT_TYPE,
                        null,
                        mActivity,
                        null,
                        null);

                LOCK.notifyAll();
            }

        }

        @Override
        public boolean cancel(boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Bundle getResult() throws
                OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(null, null);
        }

        @Override
        public Bundle getResult(long timeout, TimeUnit timeUnit) throws
                OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(timeout, timeUnit);
        }

        private Bundle internalGetResult(Long timeout, TimeUnit timeUnit) throws
                OperationCanceledException, IOException, AuthenticatorException {
            // Make the calling thread block until the future is set
            // TODO: maybe I need to add exception catching mechanism as well
            synchronized (LOCK) {
                while (mFuture == null) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // A call to future's getResult() is blocking, so the calling thread will continue to
            // be blocked until the result is returned or exception thrown
            return timeout != null && timeUnit != null ? mFuture.getResult(timeout, timeUnit) :
                    mFuture.getResult();
        }
    }



}
