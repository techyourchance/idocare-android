package il.co.idocare.networking.newimplementation.schemes.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetUsersInfoResponseScheme {


    @SerializedName("data") private List<UserInfoScheme> mUsersInfo;

    public List<UserInfoScheme> getUsersInfo() {
        return mUsersInfo;
    }
}
