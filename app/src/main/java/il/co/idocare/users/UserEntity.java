package il.co.idocare.users;

/**
 * This class encapsulates user data
 */

public class UserEntity {

    private String mUserId;
    private String mNickname;
    private String mFirstName;
    private String mLastName;
    private int mReputation;
    private String mPictureUrl;

    public static UserEntityBuilder newBuilder() {
        return new UserEntityBuilder();
    }

    public static UserEntityBuilder newBuilder(UserEntity user) {
        return newBuilder()
                .setUserId(user.mUserId)
                .setNickname(user.mNickname)
                .setFirstName(user.mFirstName)
                .setLastName(user.mLastName)
                .setReputation(user.mReputation)
                .setPictureUrl(user.mPictureUrl);
    }

    public UserEntity(String userId, String nickname, String firstName,
                      String lastName, int reputation, String pictureUrl) {
        mUserId = userId;
        mNickname = nickname;
        mFirstName = firstName;
        mLastName = lastName;
        mReputation = reputation;
        mPictureUrl = pictureUrl;
    }

    public String getUserId() {
        return mUserId;
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

    public int getReputation() {
        return mReputation;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

    // ---------------------------------------------------------------------------------------------
    //
    // Builder

    public static class UserEntityBuilder {

        private String mUserId;
        private String mNickname = "";
        private String mFirstName = "";
        private String mLastName = "";
        private int mReputation = 0;
        private String mPictureUrl = "";

        public UserEntityBuilder() {}

        public UserEntity build() {
            return new UserEntity(mUserId, mNickname, mFirstName, mLastName, mReputation, mPictureUrl);
        }

        public UserEntityBuilder setUserId(String userId) {
            mUserId = userId;
            return this;
        }

        public UserEntityBuilder setNickname(String nickname) {
            mNickname = nickname;
            return this;
        }

        public UserEntityBuilder setFirstName(String firstName) {
            mFirstName = firstName;
            return this;
        }

        public UserEntityBuilder setLastName(String lastName) {
            mLastName = lastName;
            return this;
        }

        public UserEntityBuilder setReputation(int reputation) {
            mReputation = reputation;
            return this;
        }

        public UserEntityBuilder setPictureUrl(String pictureUrl) {
            mPictureUrl = pictureUrl;
            return this;
        }
    }


}
