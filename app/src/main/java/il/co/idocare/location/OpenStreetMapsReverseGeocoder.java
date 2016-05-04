package il.co.idocare.location;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpUriRequest;
import ch.boye.httpclientandroidlib.client.utils.URIBuilder;
import ch.boye.httpclientandroidlib.impl.client.CloseableHttpClient;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

/**
 * Created by Vasiliy on 8/28/2015.
 */
public class OpenStreetMapsReverseGeocoder implements ReverseGeocoder {

    private static final String OSM_SCHEME = "http";
    private static final String OSM_HOST = "nominatim.openstreetmaps.org";
    private static final String OSM_REVERSE_GEOCODE_PATH = "/reverse";

    @Override
    public String getFromLocation(double latitude, double longitude, Locale locale) {


        // Build the required URI
        URI uri = null;
        try {
            uri = new URIBuilder()
                    .setScheme(OSM_SCHEME)
                    .setHost(OSM_HOST)
                    .setPath(OSM_REVERSE_GEOCODE_PATH)
                    .setParameter("format", "json")
                    .setParameter("zoom", "18")
                    .setParameter("addressdetails", "1")
                    .setParameter("lat", String.valueOf(latitude))
                    .setParameter("lon", String.valueOf(longitude))
                    .setParameter("accept-language", locale.getCountry() + "," + Locale.US.getCountry())
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        HttpUriRequest httpRequest = new HttpGet(uri);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        CloseableHttpResponse httpResponse = null;
        String responseEntityString = "";
        // Execute the request and read the entity payload
        try {
            httpResponse = httpClient.execute(httpRequest);
            responseEntityString = EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (httpResponse != null)
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        // Parse the response
        if ((httpResponse.getStatusLine().getStatusCode() / 100 != 2) ||
                TextUtils.isEmpty(responseEntityString))
            return null;
        else
            return parseResponse(responseEntityString);

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
