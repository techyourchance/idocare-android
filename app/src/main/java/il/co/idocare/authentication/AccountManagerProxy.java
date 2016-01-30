package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.Arrays;

import il.co.idocare.Constants;
import il.co.idocare.utils.Logger;

/**
 * This class wraps the native AccountManager and adds application specific logic.
 */
public class AccountManagerProxy {

    private static final String TAG = "AccountManagerProxy";

    private AccountManager mAccountManager;
    private Logger mLogger;

    public AccountManagerProxy(AccountManager accountManager, Logger logger) {
        mAccountManager = accountManager;
        mLogger = logger;
    }

    /**
     * Call to this method will add a new account and set its auth token. If the required account
     * already exists - call to this method will only update its auth token.
     * @return true if the account was created (or had already existed) and its auth token was
     *         set; false if account couldn't be created
     */
    public boolean addNativeAccount(String username, String userId, String authToken) {

        mLogger.d(TAG, "attempting to add a native account; username: " + username
                + "; user ID: " + userId + "; authToken: " + authToken);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(authToken)) {
            mLogger.e(TAG, "account addition failed - invalid parameters");
            return false;
        }

        String accountType = AccountAuthenticator.ACCOUNT_TYPE_DEFAULT;

        final Account account = new Account(username, accountType);
        Bundle userdata = new Bundle(1);
        userdata.putString(Constants.FIELD_NAME_USER_ID, userId);
        mAccountManager.addAccountExplicitly(account, null, userdata);

        Account[] existingAccounts =
                mAccountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);

        /*
         The below code both checks whether the required account exists and removes all other
         accounts, thus ensuring existence of a single account on the device...
         TODO: reconsider single account approach and this particular implementation
          */
        boolean targetAccountExists = Arrays.asList(existingAccounts).contains(account);

        if (!targetAccountExists) {
            mLogger.d(TAG, "failed to add native account");
            return false;
        }

        // The required account exists - update its authToken and remove all other accounts
        for (Account acc : existingAccounts) {
            if (acc.equals(account)) {
                setNativeAccountAuthToken(username, accountType, authToken);
            } else {
                mAccountManager.removeAccount(acc, null, null);
            }
        }

        return true;
    }


    private void setNativeAccountAuthToken(String accountName, String accountType,
                                           String authToken) {
        Account account = new Account(accountName, accountType);
        mAccountManager.setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT,
                authToken);
    }
}
