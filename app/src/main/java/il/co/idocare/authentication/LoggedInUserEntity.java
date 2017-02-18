package il.co.idocare.authentication;

/**
 * This POJO encapsulates information related to logged in user
 */
public class LoggedInUserEntity {

    private final String mEmail;
    private final String mUserId;
    private final String mAuthToken;

    public LoggedInUserEntity(String email, String userId, String authToken) {
        mEmail = email;
        mUserId = userId;
        mAuthToken = authToken;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public String getUserId() {
        return mUserId;
    }
}
