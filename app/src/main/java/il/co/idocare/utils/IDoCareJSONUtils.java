package il.co.idocare.utils;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.pojos.UserItem;

/**
 * Created by Vasiliy on 2/17/2015.
 */
public class IDoCareJSONUtils {

    private static final String LOG_TAG = "IDoCareJSONUtils";

    /**
     * Verify that the server returned "success" status in JSON object
     * @param jsonString JSON string representing the object
     * @return true if the "status" field is "success", false in any other case (incl. formatting
     *         errors)
     */
    public static boolean verifySuccessfulStatus (String jsonString) {
        try {
            JSONObject jsonObj = new JSONObject(jsonString);

            String status = jsonObj.getString(Constants.FIELD_NAME_RESPONSE_STATUS);
            String message = jsonObj.getString(Constants.FIELD_NAME_RESPONSE_MESSAGE);

            if (status.equals("success")) {
                return true;
            } else {
                Log.w(LOG_TAG,
                        "Unsuccessful status of the response: " + status + ". Message: " + message);
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * This method extracts the "data" JSON object from a string formatted as JSON object.
     * @param jsonString JSON string representing the object
     * @return JSONObject representing the "data" field of the argument
     * @throws org.json.JSONException if the argument is not formatted as JSON object, or this
     *         JSON object doesn't have "data" field formatted as JSON object
     */
    public static JSONObject extractDataJSONObject (String jsonString) throws JSONException {

        JSONObject jsonObj = new JSONObject(jsonString);

        return jsonObj.getJSONObject(Constants.FIELD_NAME_RESPONSE_DATA);
    }



    private static String formatDate(String stringDate) {
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdfIn.parse(stringDate);
            SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yy 'at' HH:mm ");
            return sdfOut.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "date error";
        }
    }


}
