package il.co.idocarecore.authentication;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.techyourchance.threadposter.BackgroundThreadPoster;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import il.co.idocarecore.Constants;
import il.co.idocarecore.datamodels.pojos.UserSignupData;
import il.co.idocarecore.eventbusevents.LoginStateEvents;
import il.co.idocarecore.networking.FilesDownloader;
import il.co.idocarecore.networking.ServerApi;
import il.co.idocarecore.networking.schemes.responses.AuthResponseScheme;
import il.co.idocarecore.utils.Logger;
import il.co.idocarecore.utils.SecurityUtils;
import il.co.idocarecore.Constants;
import il.co.idocarecore.datamodels.pojos.UserSignupData;
import il.co.idocarecore.eventbusevents.LoginStateEvents;
import il.co.idocarecore.networking.FilesDownloader;
import il.co.idocarecore.networking.ServerApi;
import il.co.idocarecore.networking.schemes.responses.AuthResponseScheme;
import il.co.idocarecore.utils.Logger;
import il.co.idocarecore.utils.SecurityUtils;
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

    private static final String TAG = "AuthManager";

    private final LoginStateManager mLoginStateManager;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final ServerApi mServerApi;
    private final FilesDownloader mFilesDownloader;
    private final EventBus mEventBus;
    private final Logger mLogger;

    public AuthManager(LoginStateManager loginStateManager,
                       BackgroundThreadPoster backgroundThreadPoster,
                       ServerApi serverApi,
                       FilesDownloader filesDownloader,
                       EventBus eventBus,
                       Logger logger) {
        mLoginStateManager = loginStateManager;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mServerApi = serverApi;
        mFilesDownloader = filesDownloader;
        mEventBus = eventBus;
        mLogger = logger;
    }


    public void logInFacebook(final AccessToken accessToken) {
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                logInFacebookSync(accessToken);
            }
        });
    }

    @WorkerThread
    private void logInFacebookSync(AccessToken accessToken) {
        // Construct a request to fetch user's details
        GraphRequest request = GraphRequest.newMeRequest(accessToken, null);
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email, picture.type(square).width(768)");
        request.setParameters(parameters);

        GraphResponse response = GraphRequest.executeAndWait(request);

        mLogger.v(TAG, "Contents of facebook/me response: " + response.getRawResponse());

        if (response.getError() != null) {
            mLogger.e(TAG, "facebook/me returned error response: " +
                    response.getError().getErrorMessage());
            // TODO: facebook errors should be processed - there is a way to recover from some of them
            return;
        }

        JSONObject jsonResponse = response.getJSONObject();
        if (jsonResponse == null) {
            mLogger.e(TAG, "couldn't obtain JSON object from FB response");
            return;
        }

        final String facebookId;
        String email;
        String password;
        String firstName;
        String lastName;
        String nickname;
        String pictureUrl;

        try {
            facebookId = jsonResponse.getString("id");
            email = jsonResponse.getString("email");
            password = facebookId;
            firstName = jsonResponse.getString("first_name");
            lastName = jsonResponse.getString("last_name");
            nickname = firstName + " " + lastName;
            pictureUrl = jsonResponse.getJSONObject("picture").getJSONObject("data").getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        AuthResponseScheme authResponseScheme = null;

        if ((authResponseScheme = logInNativeSync(email, password)) != null) {
            mLogger.d(TAG, "native login using FB credentials succeeded");
        } else {
            mLogger.d(TAG, "native login using FB credentials failed; attempting native signup");
            String localPictureUri = mFilesDownloader.downloadFileAndStoreLocallySync(pictureUrl);
            UserSignupData userData = new UserSignupData(email, password, nickname,
                                                         firstName, lastName, facebookId, localPictureUri);
            authResponseScheme = signUpNativeSync(userData);
        }

        processAuthenticationResponse(email, authResponseScheme);
    }


    public void logInNative(final String username, final String password) {
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                AuthResponseScheme responseScheme = logInNativeSync(username, password);
                processAuthenticationResponse(username, responseScheme);

            }
        });
    }

    @WorkerThread
    @Nullable
    private AuthResponseScheme logInNativeSync(String username, String password) {

        final String encodedUsername = SecurityUtils.encodeStringAsCredential(username);
        final String encodedPassword = SecurityUtils.encodeStringAsCredential(password);

        Call<AuthResponseScheme> call = mServerApi.loginNative(encodedUsername, encodedPassword);

        AuthResponseScheme responseScheme = null;
        try {
            Response<AuthResponseScheme> response = call.execute();
            if (response.isSuccessful()) {
                responseScheme = response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseScheme;
    }


    public void signUp(final UserSignupData signupUserData) {
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                String username = signupUserData.getEmail();
                AuthResponseScheme authResponseScheme = signUpNativeSync(signupUserData);
                processAuthenticationResponse(username, authResponseScheme);
            }
        });
    }

    @WorkerThread
    @Nullable
    private AuthResponseScheme signUpNativeSync(UserSignupData signupUserData) {
        final MultipartBody signupBody = createSignupBody(signupUserData);

        Call<AuthResponseScheme> call = mServerApi.signup(signupBody);

        AuthResponseScheme authResponseScheme = null;
        try {
            Response<AuthResponseScheme> response = call.execute();
            if (response.isSuccessful()) {
                authResponseScheme = response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return authResponseScheme;
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

    private void processAuthenticationResponse(String username,
                                               @Nullable AuthResponseScheme authResponseScheme) {
        if (authResponseScheme != null) {
            String publicKey = authResponseScheme.getUserInfo().getPublicKey();
            String userId = authResponseScheme.getUserInfo().getUserId();
            mLoginStateManager.userLoggedIn(username, publicKey, userId);
            notifyLoginSucceeded();
        } else {
            notifyLoginFailed();
        }
    }

    private void notifyLoginFailed() {
        mEventBus.post(new LoginStateEvents.LoginFailedEvent());
    }

    private void notifyLoginSucceeded() {
        mEventBus.post(new LoginStateEvents.LoginSucceededEvent());
    }


}
