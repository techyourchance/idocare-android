package il.co.idocare.authentication;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import il.co.idocare.Constants;
import il.co.idocare.datamodels.pojos.UserSignupData;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.networking.newimplementation.ServerApi;
import il.co.idocare.networking.newimplementation.schemes.responses.LoginNativeResponseScheme;
import il.co.idocare.utils.SecurityUtils;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * This class is "dual" for LoginStateManager. Responsibility of this class is to handle commands
 * whereas LoginStateManager handles queries.
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
                        processLoginNativeResponse(username, response.body());
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

    private void processLoginNativeResponse(String username, LoginNativeResponseScheme loginNativeResponse) {
        String publicKey = loginNativeResponse.getUserInfo().getPublicKey();
        String userId = loginNativeResponse.getUserInfo().getUserId();
        mLoginStateManager.userLoggedInNative(username, publicKey, userId);
    }


    public void signUp(final UserSignupData signupUserData) {

        final String username = signupUserData.getEmail();

        final MultipartBody signupBody = createSignupBody(signupUserData);

        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                Call<LoginNativeResponseScheme> call = mServerApi.signup(signupBody);
                try {
                    Response<LoginNativeResponseScheme> response = call.execute();
                    if (response.isSuccessful()) {
                        processLoginNativeResponse(username, response.body());
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

    private MultipartBody createSignupBody(UserSignupData signupUserData) {
        MultipartBody.Builder builder = new MultipartBody.Builder();

        builder.setType(MultipartBody.FORM);

        String encodedUsername = SecurityUtils.encodeStringAsCredential(signupUserData.getEmail());
        String encodedPassword = SecurityUtils.encodeStringAsCredential(signupUserData.getPassword());

        builder.addFormDataPart(Constants.FIELD_NAME_USER_EMAIL, encodedUsername);
        builder.addFormDataPart(Constants.FIELD_NAME_USER_PASSWORD_SIGNUP, encodedPassword);
        builder.addFormDataPart(Constants.FIELD_NAME_USER_NICKNAME, signupUserData.getNickname());
        builder.addFormDataPart(Constants.FIELD_NAME_USER_FIRST_NAME, signupUserData.getFirstName());
        builder.addFormDataPart(Constants.FIELD_NAME_USER_LAST_NAME, signupUserData.getLastName());

        // facebook ID (optional)
        if (signupUserData.getFacebookId() != null) {
            builder.addFormDataPart(
                    Constants.FIELD_NAME_USER_FACEBOOK_ID, signupUserData.getFacebookId());
        }

        if (signupUserData.getUserPicturePath() != null) {


            String pictureUri = signupUserData.getUserPicturePath();
            File pictureFile = new File(pictureUri);

            if (pictureFile.exists()) {
                builder.addFormDataPart(
                        Constants.FIELD_NAME_USER_PICTURE,
                        "userPicture.jpg",
                        RequestBody.create(MediaType.parse("image/*"), pictureFile));
            } else {
                throw new RuntimeException("picture file doesn't exist: " + pictureFile);
            }

        }

        return builder.build();
    }


    private void notifyLoginFailed() {
        mEventBus.post(new LoginStateEvents.LoginFailedEvent());
    }

    private void notifyLoginSucceeded() {
        mEventBus.post(new LoginStateEvents.LoginSucceededEvent());
    }


}
