package il.co.idocare.networking.newimplementation.schemes.responses;

import com.google.gson.annotations.SerializedName;

public class LoginNativeResponseScheme {


    @SerializedName("data") private AuthInfoScheme mAuthInfoScheme;

    public AuthInfoScheme getUserInfo() {
        return mAuthInfoScheme;
    }
}
