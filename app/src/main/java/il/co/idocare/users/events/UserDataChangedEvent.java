package il.co.idocare.users.events;

/**
 * This event is used in order to communicate notifications about users data changes
 */
public class UserDataChangedEvent {

    private String mUserId;

    public UserDataChangedEvent(String userId) {
        mUserId = userId;
    }

    public String getUserId() {
        return mUserId;
    }
}
