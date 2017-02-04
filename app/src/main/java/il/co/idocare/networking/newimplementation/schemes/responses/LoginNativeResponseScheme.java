package il.co.idocare.networking.newimplementation.schemes.responses;

import com.google.gson.annotations.SerializedName;

public class LoginNativeResponseScheme {


    @SerializedName("data") private UserInfoScheme mUserInfoScheme;

    public UserInfoScheme getUserInfo() {
        return mUserInfoScheme;
    }
}
