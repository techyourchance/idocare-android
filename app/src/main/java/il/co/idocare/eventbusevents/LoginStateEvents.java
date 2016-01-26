package il.co.idocare.eventbusevents;

/**
 * Created by Vasiliy on 1/26/2016.
 */
public final class LoginStateEvents {

    private LoginStateEvents() {}

    public static class LoginSucceededEvent {

        private final String mUsername;
        private final String mAuthToken;

        public LoginSucceededEvent(String username, String authToken) {
            mUsername = username;
            mAuthToken = authToken;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getAuthToken() {
            return mAuthToken;
        }

    }

    public static class LoginFailedEvent {

    }
}
