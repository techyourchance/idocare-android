package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.facebook.login.LoginManager;

import org.greenrobot.eventbus.EventBus;

import il.co.idocare.authentication.events.UserLoggedOutEvent;
import il.co.idocare.common.settings.SettingsManager;
import il.co.idocare.utils.Logger;

/**
 * This class manages the login state of the user - it aggregates information from all login
 * mechanisms supported by the application (native, Facebook, etc.)
 */
public class LoginStateManager {

    private static final String TAG = "LoginStateManager";

    private static final String DUMMY_ACCOUNT_NAME = "dummy_account";
    private static final String DUMMY_ACCOUNT_TYPE = "il.co.idocare.account";

    private final AccountManager mAccountManager;
    private final SettingsManager mSettingsManager;
    private final Logger mLogger;

    public LoginStateManager(AccountManager accountManager,
                             SettingsManager settingsManager, Logger logger) {
        mAccountManager = accountManager;
        mSettingsManager = settingsManager;
        mLogger = logger;
    }

    /**
     * Check whether there is a currently logged in user.
     * @return true if the user is logged in by ANY mean (Facebook, native, etc.)
     */
    public boolean isLoggedIn() {
        String authToken = mSettingsManager.authToken().getValue();
        return authToken != null && !authToken.isEmpty();
    }

    /* pp */ void userLoggedIn(String username, String publicKey, String userId) {
        clearUserSettings();
        setUserSettings(username, publicKey, userId);
    }

    private void clearUserSettings() {
        mSettingsManager.userEmail().remove();
        mSettingsManager.userId().remove();
        mSettingsManager.authToken().remove();
    }

    private void setUserSettings(String username, String authToken, String userId) {
        mSettingsManager.userEmail().setValue(username);
        mSettingsManager.userId().setValue(userId);
        mSettingsManager.authToken().setValue(authToken);
    }

    /**
     * Log out the active user
     */
    public void logOut() {

        LoginManager.getInstance().logOut(); // log out if logged in with FB

        clearUserSettings();

        // Show login screen the next time the app starts
        setLoginSkipped(false);

        EventBus.getDefault().post(new UserLoggedOutEvent());
    }

    public void setLoginSkipped(boolean loginSkipped) {
        mSettingsManager.loginSkipped().setValue(loginSkipped);
    }

    public boolean isLoginSkipped() {
        return mSettingsManager.loginSkipped().getValue();
    }

    public LoggedInUserEntity getLoggedInUser() {
        String email = mSettingsManager.userEmail().getValue();
        String userId = mSettingsManager.userId().getValue();
        String authToken = mSettingsManager.authToken().getValue();

        if (userId == null || userId.isEmpty()) {
            return new LoggedInUserEntity("", "", ""); // null value object
        } else {
            return new LoggedInUserEntity(email, userId, authToken);
        }
    }

    /**
     * This method should be used only by components that require Android account (e.g. SyncController)
     */
    public Account getAccountManagerAccount() {
        // make sure that AccountManager recognizes the returned account (otherwise it is useless)
        Account acc = new Account(DUMMY_ACCOUNT_NAME, DUMMY_ACCOUNT_TYPE);
        mAccountManager.addAccountExplicitly(acc, null, null);
        return acc;
    }

}
