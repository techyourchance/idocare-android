package il.co.idocare.authentication;

/**
 * This POJO encapsulates information related to logged in user
 */
public class LoggedInUserEntity {

    private final String mEmail;
    private final String mUserId;
    private final String mFacebookId;
    private final String mAuthToken;

    public LoggedInUserEntity(String email, String userId, String facebookId, String authToken) {
        mEmail = email;
        mUserId = userId;
        mFacebookId = facebookId;
        mAuthToken = authToken;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public String getUserId() {
        return mUserId;
    }
}
