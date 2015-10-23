package il.co.idocare.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants;

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

            String status = jsonObj.getString(Constants.FIELD_NAME_INTERNAL_STATUS);
            String message = jsonObj.getString(Constants.FIELD_NAME_INTERNAL_MESSAGE);

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

        return jsonObj.getJSONObject(Constants.FIELD_NAME_INTERNAL_DATA);
    }



}
