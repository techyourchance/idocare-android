package il.co.idocare.location;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import il.co.idocare.networking.GeneralApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class OpenStreetMapsReverseGeocoder implements ReverseGeocoder {

    private final GeneralApi mGeneralApi;

    public OpenStreetMapsReverseGeocoder(GeneralApi generalApi) {
        mGeneralApi = generalApi;
    }

    @Override
    public String getFromLocation(double latitude, double longitude, Locale locale) {

        Call<ResponseBody> call = mGeneralApi.reverseGeocode(
                18,
                1,
                locale.getCountry() + "," + Locale.US.getCountry(),
                latitude,
                longitude
        );

        try {
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()) {
                String responseEntityString = response.body().string();
                return parseResponse(responseEntityString);
            } else  {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private String parseResponse(String entityString) {
        JSONObject addressObj;
        try {
            addressObj = new JSONObject(entityString).getJSONObject("address");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        try {
            if (addressObj.has("city")) {
                StringBuilder sb = new StringBuilder();

                sb.append(addressObj.getString("city"));

                if (addressObj.has("road")) {
                    sb.append(" , ").append(addressObj.getString("road"));
                    if (addressObj.has("house_number"))
                        sb.append(" , ").append(addressObj.getString("house_number"));
                }

                return sb.toString();

            } else if (addressObj.has("state")) {
                return addressObj.getString("state");
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
