package il.co.idocare.networking.newimplementation;

import il.co.idocare.networking.newimplementation.schemes.responses.RequestsResponseScheme;
import il.co.idocare.networking.newimplementation.schemes.responses.LoginNativeResponseScheme;
import il.co.idocare.networking.newimplementation.schemes.responses.RequestResponseScheme;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

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
    Call<RequestsResponseScheme> getRequests();

    @POST("request/add")
    Call<RequestResponseScheme> createNewRequest(@Body MultipartBody part);

    @FormUrlEncoded
    @POST("request/pickup")
    Call<Void> pickupRequest(@Field("request_id") String requestId);


}
