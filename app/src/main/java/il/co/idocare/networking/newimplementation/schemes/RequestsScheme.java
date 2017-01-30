package il.co.idocare.networking.newimplementation.schemes;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RequestsScheme {

    @SerializedName("data") List<RequestScheme> mRequestSchemes;

    public List<RequestScheme> getRequestSchemes() {
        return mRequestSchemes;
    }
}
