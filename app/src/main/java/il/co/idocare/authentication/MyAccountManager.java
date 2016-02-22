package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;

import java.util.Arrays;

import il.co.idocare.Constants;
import il.co.idocare.nonstaticproxies.TextUtilsProxy;
import il.co.idocare.utils.Logger;

/**
 * This class wraps the native AccountManager and adds application specific logic.
 */
public class MyAccountManager {

    private static final String TAG = "MyAccountManager";

    private AccountManager mAccountManager;
    private Logger mLogger;
    private TextUtilsProxy mTextUtils;

    public MyAccountManager(AccountManager accountManager, Logger logger, TextUtilsProxy textUtils) {
        mAccountManager = accountManager;
        mLogger = logger;
        mTextUtils = textUtils;
    }


    /**
     *
     * @return active user account, or null if no active user account and couldn't add dummy account
     */
    public Account getActiveAccount() {
        Account[] accounts =
                mAccountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);

        if (accounts.length == 0) {
            if (addDummyAccount())
                return getDummyAccount();
            else
                return null;
        }


        if (accounts.length > 1) {
            mLogger.e(TAG, "There is more than one account on the device. Using the first one." +
                    " Total native accounts: " + String.valueOf(accounts.length));
        }

        // Checking for dummy account
        if (accounts[0].equals(getDummyAccount()))
            return null;
        else
            return accounts[0];
    }

    /**
     * Call to this method will add a new account and set its auth token. If the required account
     * already exists - call to this method will only update its auth token.
     * @return true if the account was created (or had already existed) and its auth token was
     *         set; false if account couldn't be created
     */
    public boolean addAccount(String username, String userId, String authToken) {
        mLogger.d(TAG, "attempting to add a native account; " +
                "username: " + username+ "; user ID: " + userId + "; authToken: " + authToken);

        if (mTextUtils.isEmpty(username)
                || mTextUtils.isEmpty(userId)
                || mTextUtils.isEmpty(authToken)) {
            throw new IllegalArgumentException("all parameters must be non-empty");
        }

        String accountType = AccountAuthenticator.ACCOUNT_TYPE_DEFAULT;

        Account account = new Account(username, accountType);
        Bundle userdata = new Bundle(1);
        userdata.putString(Constants.FIELD_NAME_USER_ID, userId);

        return addAccount(account, userdata, authToken);
    }

    private boolean addAccount(Account account, Bundle userData, String authToken) {
        mLogger.d(TAG, "addAccount; account = " + account + "; userData = " + userData);

        String accountType = account.type;
        mAccountManager.addAccountExplicitly(account, null, userData);

        Account[] existingAccounts = mAccountManager.getAccountsByType(accountType);
        /*
         The below code both checks whether the required account exists and removes all other
         accounts, thus ensuring existence of a single account on the device...
         TODO: reconsider single account approach and this particular implementation
          */
        boolean targetAccountExists = Arrays.asList(existingAccounts).contains(account);

        if (!targetAccountExists) {
            mLogger.d(TAG, "failed to add an account");
            return false;
        }

        // The required account exists - update its authToken and remove all other accounts
        for (Account acc : existingAccounts) {
            if (acc.equals(account)) {
                mAccountManager.setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT,
                        authToken);
            } else {
                mAccountManager.removeAccount(acc, null, null);
            }
        }

        return true;
    }


    public Account getDummyAccount() {
        Account account = new Account(AccountAuthenticator.DUMMY_ACCOUNT_NAME,
                AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);
        return account;
    }

    private boolean addDummyAccount() {
        Account dummyAccount = getDummyAccount();
        return addAccount(dummyAccount, null, AccountAuthenticator.DUMMY_ACCOUNT_AUTH_TOKEN);
    }


}
