package il.co.idocare.networking.newimplementation.schemes.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetRequestsScheme {

    @SerializedName("data") private List<RequestScheme> mRequestSchemes;

    public List<RequestScheme> getRequestSchemes() {
        return mRequestSchemes;
    }
}
