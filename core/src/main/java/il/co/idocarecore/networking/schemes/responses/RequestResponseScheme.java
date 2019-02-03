package il.co.idocarecore.networking.schemes.responses;

import com.google.gson.annotations.SerializedName;

public class RequestResponseScheme {

    @SerializedName("data") private RequestScheme mRequestScheme;

    public RequestScheme getRequestScheme() {
        return mRequestScheme;
    }
}
