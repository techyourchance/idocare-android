package il.co.idocarecore.networking.schemes.responses;

import com.google.gson.annotations.SerializedName;

public class AuthResponseScheme {


    @SerializedName("data") private AuthInfoScheme mAuthInfoScheme;

    public AuthInfoScheme getUserInfo() {
        return mAuthInfoScheme;
    }
}
