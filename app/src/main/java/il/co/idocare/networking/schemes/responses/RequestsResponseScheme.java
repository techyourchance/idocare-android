package il.co.idocare.networking.schemes.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RequestsResponseScheme {

    @SerializedName("data") private List<RequestScheme> mRequestSchemes;

    public List<RequestScheme> getRequestSchemes() {
        return mRequestSchemes;
    }
}
