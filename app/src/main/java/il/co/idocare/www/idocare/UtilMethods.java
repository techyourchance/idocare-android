package il.co.idocare.www.idocare;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UtilMethods {

    private final static String LOG_TAG = "UtilMethods";


    private final static String JSON_TAG_REQUESTS = "data";

    /**
     * Count lines in string.
     * @param string it is assumed that line terminator is either \n or \r or \r\n, but not a mix of them.
     * @return number of lines in this string
     */
    public static int countLines(String string) {
        int lines = 1;
        int pos = 0;
        while ((pos = Math.max(string.indexOf("\n", pos), string.indexOf("\r", pos)) + 1) != 0) {
            lines++;
        }
        return lines;
    }


    /**
     * This method parses the JSON formatted text obtained from the server and extract the
     * details of the requests embedded in that text
     * @param jsonData
     * @return
     */
    public static List<RequestItem> extractRequestsFromJSON(String jsonData) {

        ArrayList<RequestItem> requestItemsList = new ArrayList<RequestItem>();

        if (jsonData == null || jsonData.length() <= 0) {
            Log.e(LOG_TAG, "jsonData is null or empty");
            return requestItemsList;
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonData);

            // Getting JSON Array
            JSONArray requestsArray = jsonObj.getJSONArray(JSON_TAG_REQUESTS);

            if (requestsArray != null && requestsArray.length() > 0) {

                JSONObject request;
                RequestItem requestItem;

                for (int i = 0; i < requestsArray.length(); i++) {

                    request = requestsArray.getJSONObject(i);
                    requestItem = RequestItem.createRequestItem(request);

                    if (requestItem != null) {
                        requestItemsList.add(requestItem);
                    } else {
                        Log.e(LOG_TAG, "couldn't build request item!");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return requestItemsList;
    }
}
