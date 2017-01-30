package il.co.idocare.networking.newimplementation;

import il.co.idocare.networking.newimplementation.schemes.RequestsScheme;
import retrofit2.Call;
import retrofit2.http.POST;

/**
 * This interface contains definitions of endpoints consumed by Retrofit
 */
public interface ServerApi {

    @POST("request")
    Call<RequestsScheme> requestsList();


}
