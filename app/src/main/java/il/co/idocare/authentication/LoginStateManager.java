package il.co.idocare.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import il.co.idocare.common.settings.SettingsManager;
import il.co.idocare.datamodels.pojos.UserSignupNativeData;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.networking.newimplementation.ServerApi;
import il.co.idocare.sequences.LoginFacebookSequence;
import il.co.idocare.sequences.LoginNativeSequence;
import il.co.idocare.sequences.Sequence;
import il.co.idocare.sequences.SignupNativeSequence;
import il.co.idocare.utils.Logger;
import il.co.idocare.utils.SecurityUtils;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import retrofit2.Call;
import retrofit2.Response;

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

    private boolean isLoggedInWithFacebook() {
        String facebookId = mSettingsManager.facebookId().getValue();
        return facebookId != null && !facebookId.isEmpty();
    }

    /**
     * Perform native signup
     */
    public void signUpNative(UserSignupNativeData userData) {
        final SignupNativeSequence signupNativeSequence =
                new SignupNativeSequence(userData);

        signupNativeSequence.registerStateChangeListener(new Sequence.StateChangeListener() {
            @Override
            public void onSequenceStateChanged(int newState) {
                if (newState == Sequence.STATE_EXECUTED_SUCCEEDED) {
                    String username = signupNativeSequence.getSequenceResult().getUsername();
                    String authToken = signupNativeSequence.getSequenceResult().getAuthToken();
                    String userId = signupNativeSequence.getSequenceResult().getUserId();
                    LoginStateManager.this.userLoggedInNative(username, authToken, userId);
                    EventBus.getDefault()
                            .post(new LoginStateEvents.LoginSucceededEvent());
                } else if (newState == Sequence.STATE_EXECUTED_FAILED) {
                    EventBus.getDefault().post(new LoginStateEvents.LoginFailedEvent());
                }
            }
        });
        signupNativeSequence.executeInBackground();
    }

    /**
     * Attempt to perform a native log in with the provided credentials.
     * @param username
     * @param password
     */
    public void logInNative(String username, String password) {


        final LoginNativeSequence loginNativeSequence =
                new LoginNativeSequence(username, password);

        loginNativeSequence.registerStateChangeListener(new Sequence.StateChangeListener() {
            @Override
            public void onSequenceStateChanged(int newState) {
                if (newState == Sequence.STATE_EXECUTED_SUCCEEDED) {
                    String username = loginNativeSequence.getSequenceResult().getUsername();
                    String authToken = loginNativeSequence.getSequenceResult().getAuthToken();
                    String userId = loginNativeSequence.getSequenceResult().getUserId();
                    LoginStateManager.this.userLoggedInNative(username, authToken, userId);
                    EventBus.getDefault()
                            .post(new LoginStateEvents.LoginSucceededEvent());
                } else if (newState == Sequence.STATE_EXECUTED_FAILED) {
                    EventBus.getDefault().post(new LoginStateEvents.LoginFailedEvent());
                }
            }
        });
        loginNativeSequence.executeInBackground();
    }

    /* pp */ void userLoggedInNative(String username, String publicKey, String userId) {
        clearUserSettings();
        setUserSettings(username, publicKey, userId, null);
    }

    /**
     * TODO: write javadoc
     */
    public void logInFacebook(AccessToken accessToken) {

        final LoginFacebookSequence loginFacebookSequence =
                new LoginFacebookSequence(accessToken, mLogger);

        loginFacebookSequence.registerStateChangeListener(new Sequence.StateChangeListener() {
            @Override
            public void onSequenceStateChanged(int newState) {
                if (newState == Sequence.STATE_EXECUTED_SUCCEEDED) {
                    String username = loginFacebookSequence.getSequenceResult().getUsername();
                    String authToken = loginFacebookSequence.getSequenceResult().getAuthToken();
                    String facebookId = loginFacebookSequence.getSequenceResult().getFacebookId();
                    LoginStateManager.this.userLoggedInFacebook(username, authToken, facebookId);
                    EventBus.getDefault()
                            .post(new LoginStateEvents.LoginSucceededEvent());
                } else if (newState == Sequence.STATE_EXECUTED_FAILED) {
                    EventBus.getDefault().post(new LoginStateEvents.LoginFailedEvent());
                }
            }
        });
        loginFacebookSequence.executeInBackground();

    }

    private void userLoggedInFacebook(String username, String authToken, String facebookId) {
        clearUserSettings();
        setUserSettings(username, authToken, facebookId, facebookId);
    }

    private void clearUserSettings() {
        mSettingsManager.userEmail().remove();
        mSettingsManager.userId().remove();
        mSettingsManager.authToken().remove();
        mSettingsManager.facebookId().remove();
    }

    private void setUserSettings(String username, String authToken, String userId, String facebookId) {
        mSettingsManager.userEmail().setValue(username);
        mSettingsManager.userId().setValue(userId);
        mSettingsManager.authToken().setValue(authToken);
        mSettingsManager.facebookId().setValue(facebookId);
    }

    /**
     * Log out the active user
     */
    public void logOut() {

        if (isLoggedInWithFacebook()) {
            LoginManager.getInstance().logOut();
        }

        clearUserSettings();

        // Show login screen the next time the app starts
        setLoginSkipped(false);

        EventBus.getDefault().post(new UserLoggedOutEvent());
    }


    /**
     * Set the value of "user chose to skip login" flag
     * @param loginSkipped true in order to set the flag, false to clear it
     */
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
        String facebookId = mSettingsManager.facebookId().getValue();

        if (userId == null || userId.isEmpty()) {
            return new LoggedInUserEntity("", "", "", ""); // null value object
        } else {
            return new LoggedInUserEntity(email, userId, facebookId, authToken);
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



    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events

    public static class UserLoggedInEvent {}

    public static class UserLoggedOutEvent {}

    // End of EventBus events
    //
    // ---------------------------------------------------------------------------------------------

}
