package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;

import java.util.Arrays;

/**
 * This class manages the login state of the user - it aggregates information from all login
 * mechanisms supported by the application (native, Facebook, etc.)
 */
public class UserStateManager {

    private static final String LOG_TAG = UserStateManager.class.getSimpleName();


    private Context mContext;
    private AccountManager mAccountManager;

    public UserStateManager(Context context) {
        if (context == null)
            throw new IllegalArgumentException("valid context must be supplied!");
        mContext = context;
        mAccountManager = AccountManager.get(mContext);
    }


    /**
     * Check whether there is a currently logged in user.
     * @return true if the user is logged in by ANY mean (Facebook, native, etc.)
     */
    public boolean isLoggedIn() {
        return isLoggedInWithFacebook() || isLoggedInNative();
    }


    private boolean isLoggedInWithFacebook() {
        /*
        accessToken in this situation can be null even when there is a logged in user - this can
        happen if getCurrentAccessToken() is called when onCurrentAccessTokenChanged() is in
        progress.
        Additional info:
        http://stackoverflow.com/questions/29294015/how-to-check-if-user-is-logged-in-with-fb-sdk-4-0-for-android
        http://stackoverflow.com/questions/30379616/how-to-get-current-facebook-access-token-on-app-start

        TODO: RESOLVE THIS BUG!!!!!!!!
         */
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;

    }

    private boolean isLoggedInNative() {
        AccountManager am = AccountManager.get(mContext);

        Account[] accounts = am.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);

        if (accounts.length == 0) return false;

        if (accounts.length > 1) {
            Log.e(LOG_TAG, "There is more than a single native account on the device. " +
                    "Using the first one returned." +
                    "\nTotal native accounts: " + String.valueOf(accounts.length));
        }
        return true;
    }


    /**
     * Call to this method will add a new account and set its auth token. If the required account
     * already exists - call to this method will only update its auth token
     * @param accountName
     * @param accountType
     * @param authToken
     * @return true if the account was successfully added; false if could not add the account for
     *         any reason
     */
    public boolean addNativeAccount(String accountName, String accountType, String authToken) {

        if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType) || TextUtils.isEmpty(authToken))
            throw new IllegalArgumentException("all parameters must be non-empty");

        Account account = new Account(accountName, accountType);
        mAccountManager.addAccountExplicitly(account, null, null);

        Account[] existingAccounts =
                mAccountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);

        /*
         The below code both checks whether the required account exists and removes all other
         accounts, thus ensuring existence of a single account on the device...
         TODO: reconsider single account approach and this particular implementation
          */
        boolean addedSuccessfully = Arrays.asList(existingAccounts).contains(account);
        // If the required account wasn't added - return
        if (!addedSuccessfully) return false;
        // If the required account exists - update its authToken and remove all other accounts
        for (Account acc : existingAccounts) {
            if (acc.equals(account)) {
                setNativeAccountAuthToken(accountName, accountType, authToken);
            } else {
                mAccountManager.removeAccountExplicitly(acc);
            }
        }
        return true;
    }

    public void setNativeAccountAuthToken(String accountName, String accountType,
                                          String authToken) {

        Account account = new Account(accountName, accountType);

        mAccountManager.setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT, authToken);

    }

    public void addFacebookAccount() {

    }
}
