package il.co.idocarecore.networking;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface GeneralApi {

    @Streaming
    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

    /**
     * This call performs reverse geocoding using Open Street Maps API.
     * For params description see:  http://wiki.openstreetmap.org/wiki/Nominatim
     */
    @GET("http://nominatim.openstreetmaps.org/reverse?format=json&email=vasiliy.zukanov@gmail.com")
    Call<ResponseBody> reverseGeocode(
            @Query("zoom") int zoom,
            @Query("addressdetails") int addressdetails,
            @Query("accept-language") String acceptLanguage,
            @Query("lat") double latitude,
            @Query("lon") double longitude
    );

}
