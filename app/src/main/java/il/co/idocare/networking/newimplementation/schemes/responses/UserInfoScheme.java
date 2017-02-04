package il.co.idocare.networking.newimplementation.schemes.responses;

import com.google.gson.annotations.SerializedName;

public class UserInfoScheme {
    @SerializedName("user_data_id") private String mUserId;
    @SerializedName("user_data_public_key") private String mPublicKey;

    public String getUserId() {
        return mUserId;
    }

    public String getPublicKey() {
        return mPublicKey;
    }
}
