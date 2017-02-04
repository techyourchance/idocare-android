package il.co.idocare.networking.newimplementation;

import java.util.Map;

import il.co.idocare.Constants;
import il.co.idocare.networking.newimplementation.schemes.responses.GetRequestsScheme;
import il.co.idocare.networking.newimplementation.schemes.responses.LoginNativeResponseScheme;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

/**
 * This interface contains definitions of endpoints consumed by Retrofit
 */
public interface ServerApi {

    @FormUrlEncoded
    @POST("user/login")
    Call<LoginNativeResponseScheme> loginNative(
            @Header("Idc-user-data") String username,
            @Field("user_data_auth") String password
    );

    @POST("request")
    Call<GetRequestsScheme> getRequests();

    @Multipart
    @POST("request/add")
    Call<Void> createNewRequest(@Part MultipartBody.Part params, @Part MultipartBody.Part pictures);


}
