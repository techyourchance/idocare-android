package il.co.idocare.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.Constants.FieldName;
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

            String status = jsonObj.getString(Constants.FieldName.RESPONSE_STATUS.getValue());
            String message = jsonObj.getString(Constants.FieldName.RESPONSE_MESSAGE.getValue());

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

        return jsonObj.getJSONObject(Constants.FieldName.RESPONSE_DATA.getValue());
    }

    /**
     * This method extracts the "data" JSON array from a string formatted as JSON object.
     * @param jsonString JSON string representing the object
     * @return JSONArray representing the "data" field of the argument
     * @throws org.json.JSONException if the argument is not formatted as JSON object, or this
     *         JSON object doesn't have "data" field formatted as JSON array
     */
    public static JSONArray extractDataJSONArray(String jsonString) throws JSONException {

        JSONObject jsonObj = new JSONObject(jsonString);

        return jsonObj.getJSONArray(Constants.FieldName.RESPONSE_DATA.getValue());
    }


    /**
     * Build RequestItem from JSON object containing request's details
     * @param jsonObject string formatted as JSON object containing request's details
     * @return RequestItem object, or null if the information in the argument JSON object is not
     *         enough to construct a valid RequestObject
     * @throws org.json.JSONException in case of a formatting error
     */
    public static RequestItem extractRequestItemFromJSONObject(JSONObject jsonObject)
            throws JSONException {

        // Ensure that all the required fields are present
        if (jsonObject.isNull(FieldName.REQUEST_ID.getValue()) ||
                jsonObject.isNull(FieldName.CREATED_BY.getValue()) ||
                jsonObject.isNull(FieldName.CREATED_AT.getValue()) ||
                jsonObject.isNull(FieldName.LATITUDE.getValue()) ||
                jsonObject.isNull(FieldName.LONGITUDE.getValue()) ||
                jsonObject.isNull(FieldName.CREATED_POLLUTION_LEVEL.getValue())) {
            return null;
        }

        // Construct basic RequestItem
        RequestItem request = RequestItem
                .createRequestItem(jsonObject.getLong(FieldName.REQUEST_ID.getValue()))
                .setCreatedBy(extractUserItemFromJSONObject(
                        jsonObject.getJSONObject(FieldName.CREATED_BY.getValue())))
                .setCreatedAt(jsonObject.getString(FieldName.CREATED_AT.getValue()))
                .setLatitude(jsonObject.getDouble(FieldName.LATITUDE.getValue()))
                .setLongitude(jsonObject.getDouble(FieldName.LONGITUDE.getValue()))
                .setCreatedPollutionLevel(jsonObject.getInt(FieldName.CREATED_POLLUTION_LEVEL.getValue()));

        // Set optional fields
        if (!jsonObject.isNull(FieldName.CREATED_COMMENT.getValue()))
            request.setCreatedComment(jsonObject.getString(FieldName.CREATED_COMMENT.getValue()));

        if (!jsonObject.isNull(FieldName.CREATED_PICTURES.getValue()))
            request.setCreatedPictures(jsonObject.getString(FieldName.CREATED_PICTURES.getValue()).split(", "));

        // If picked up then both "by" and "at" are required
        if (!jsonObject.isNull(FieldName.PICKED_UP_BY.getValue())) {
            request.setPickedUpBy(extractUserItemFromJSONObject(
                    jsonObject.getJSONObject(FieldName.PICKED_UP_BY.getValue())));
            request.setPickedUpAt(jsonObject.getString(FieldName.PICKED_UP_AT.getValue()));
        }

        // If closed then both "by" and "at" are required
        if (!jsonObject.isNull(FieldName.CLOSED_BY.getValue())) {
            request.setClosedBy(extractUserItemFromJSONObject(
                    jsonObject.getJSONObject(FieldName.CLOSED_BY.getValue())));
            request.setClosedAt(jsonObject.getString(FieldName.CLOSED_AT.getValue()));
        }

        if (!jsonObject.isNull(FieldName.CLOSED_COMMENT.getValue()))
            request.setClosedComment(FieldName.CLOSED_COMMENT.getValue());


        if (!jsonObject.isNull(FieldName.CLOSED_PICTURES.getValue()))
            request.setClosedPictures(jsonObject.getString(FieldName.CLOSED_PICTURES.getValue()).split(", "));


        return request;
    }

    /**
     * Build UserItem from JSON object containing user's details
     * @param jsonObject string formatted as JSON object containing user's details
     * @return UserItem object, or null if the information in the argument JSON object is not
     *         enough to construct a valid RequestObject
     * @throws org.json.JSONException in case of a formatting error
     */
    public static UserItem extractUserItemFromJSONObject(JSONObject jsonObject)
            throws JSONException {

        // Ensure that all the required fields are present
        if (jsonObject.isNull(FieldName.USER_ID.getValue())) {
            return null;
        }

        // Create basic UserItem
        UserItem user =  UserItem.createUserItem(jsonObject.getLong(FieldName.USER_ID.getValue()));


        // Set optional fields
        if (!jsonObject.isNull(FieldName.USER_NICKNAME.getValue()))
            user.setNickname(jsonObject.getString(FieldName.USER_NICKNAME.getValue()));

        return user;
    }


    /**
     * Parse JSON Array into list of RequestItem objects
     * @param jsonArray the array to parse
     * @return list of created RequestItem objects, or null if the argument is null
     * @throws JSONException in case the elements of the argument JSON array can not be
     *         parsed as request JSON objects
     */
    public static List<RequestItem> extractRequestItemsFromJSONArray(JSONArray jsonArray)
            throws JSONException {

        if (jsonArray == null) return null;

        List<RequestItem> requests = new ArrayList<RequestItem>(jsonArray.length());

        RequestItem requestItem;

        for (int i=0; i<jsonArray.length(); i++) {

            requestItem = IDoCareJSONUtils
                    .extractRequestItemFromJSONObject(jsonArray.getJSONObject(i));

            // Add the created RequestItem if everything was fine
            if (requestItem != null) requests.add(requestItem);

        }

        return requests;
    }


}
