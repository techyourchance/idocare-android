package il.co.idocarecore.networking.schemes.responses;

import com.google.gson.annotations.SerializedName;

public class UserInfoScheme {
    @SerializedName("user_data_id") private String mId;
    @SerializedName("user_data_first_name") private String mFirstName;
    @SerializedName("user_data_last_name") private String mLastName;
    @SerializedName("user_data_nickname") private String mNickname;
    @SerializedName("user_data_picture") private String mPicture;
    @SerializedName("user_data_reputation") private int mReputation;

    public String getId() {
        return mId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getNickname() {
        return mNickname;
    }

    public String getPicture() {
        return mPicture;
    }

    public int getReputation() {
        return mReputation;
    }
}
