package il.co.idocare.authentication;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.networking.newimplementation.ServerApi;
import il.co.idocare.networking.newimplementation.schemes.responses.LoginNativeResponseScheme;
import il.co.idocare.utils.SecurityUtils;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Vasiliy on 2/3/2017.
 */

public class AuthManager {

    private final LoginStateManager mLoginStateManager;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final ServerApi mServerApi;
    private final EventBus mEventBus;

    public AuthManager(LoginStateManager loginStateManager,
                       BackgroundThreadPoster backgroundThreadPoster,
                       ServerApi serverApi,
                       EventBus eventBus) {
        mLoginStateManager = loginStateManager;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mServerApi = serverApi;
        mEventBus = eventBus;
    }

    public void logInNative(final String username, String password) {

        final String encodedUsername = SecurityUtils.encodeStringAsCredential(username);
        final String encodedPassword = SecurityUtils.encodeStringAsCredential(password);

        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                Call<LoginNativeResponseScheme> call = mServerApi.loginNative(encodedUsername, encodedPassword);
                try {
                    Response<LoginNativeResponseScheme> response = call.execute();
                    if (response.isSuccessful()) {
                        handleUserLoggedIn(username, response.body());
                        notifyLoginSucceeded();
                    } else {
                        notifyLoginFailed();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    notifyLoginFailed();
                }
            }
        });
    }

    private void handleUserLoggedIn(String username, LoginNativeResponseScheme loginNativeResponse) {
        String publicKey = loginNativeResponse.getUserInfo().getPublicKey();
        String userId = loginNativeResponse.getUserInfo().getUserId();
        mLoginStateManager.userLoggedInNative(username, publicKey, userId);
    }


    private void notifyLoginFailed() {
        mEventBus.post(new LoginStateEvents.LoginFailedEvent());
    }

    private void notifyLoginSucceeded() {
        mEventBus.post(new LoginStateEvents.LoginSucceededEvent());
    }


}
