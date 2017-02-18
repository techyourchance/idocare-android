package il.co.idocare.datamodels.pojos;

import android.support.annotation.Nullable;

public class UserSignupData {

    private final String mEmail;
    private final String mPassword;
    private final String mNickname;
    private final String mFirstName;
    private final String mLastName;
    @Nullable
    private final String mFacebookId;
    @Nullable
    private final String mUserPicturePath;

    public UserSignupData(String email, String password, String nickname, String firstName,
                          String lastName, @Nullable String facebookId,
                          @Nullable String userPicturePath) {

        mEmail = email;
        mPassword = password;
        mNickname = nickname;
        mFirstName = firstName;
        mLastName = lastName;
        mFacebookId = facebookId;
        mUserPicturePath = userPicturePath;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getNickname() {
        return mNickname;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    @Nullable
    public String getFacebookId() {
        return mFacebookId;
    }

    @Nullable
    public String getUserPicturePath() {
        return mUserPicturePath;
    }
}
