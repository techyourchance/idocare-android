package il.co.idocare.networking.newimplementation.schemes.responses;

import com.google.gson.annotations.SerializedName;

public class UserInfoScheme {
    @SerializedName("user_data_id") private String mId;
    @SerializedName("user_data_first_name") private String mFirstName;
    @SerializedName("user_data_last_name") private String mLastName;
    @SerializedName("user_data_nickname") private String mNickname;
    @SerializedName("user_data_picture") private String mPicture;
    @SerializedName("user_data_reputation") private int mReputation;


}
