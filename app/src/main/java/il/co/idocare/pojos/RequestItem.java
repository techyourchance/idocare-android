package il.co.idocare.pojos;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants.FieldName;

/**
 * This object contains data about a single request (users involved, state, location, etc)
 */
public class RequestItem {

    private final static String LOG_TAG = "RequestItem";

    private long mId;
    private UserItem mCreatedBy;
    private String mCreatedAt;
    private String mCreatedComment;
    private String[] mCreatedPictures;
    private double mLat;
    private double mLong;
    private int mCreatedPollutionLevel;
    private UserItem mPickedUpBy;
    private String mPickedUpAt;
    private UserItem mClosedBy;
    private String mClosedAt;
    private String mClosedComment;
    private String[] mClosedPictures;



    public static RequestItem createRequestItem(long id) {
        return new RequestItem(id);
    }

    private RequestItem (
            long id,
            UserItem createdBy,
            String createdAt,
            String createdComment,
            String[] createdPictures,
            double latitude,
            double longitude,
            int createdPollutionLevel,
            UserItem pickedUpBy,
            String pickedUpAt,
            UserItem closedBy,
            String closedAt,
            String closedComment,
            String[] closedPictures) {

        mId = id;
        mCreatedBy = createdBy;
        mCreatedComment = createdComment;
        mCreatedPictures = createdPictures;
        mLat = latitude;
        mLong = longitude;
        mCreatedPollutionLevel = createdPollutionLevel;
        mPickedUpBy = pickedUpBy;
        mPickedUpAt = mPickedUpAt;
        mClosedBy = closedBy;
        mClosedAt = closedAt;
        mClosedComment = closedComment;
        mClosedPictures = closedPictures;
    }

    private RequestItem(long id) {
        mId = id;
    }

    // ---------------------------------------------------------------------------------------------
    //
    // Setters

    public RequestItem setCreatedBy(UserItem user) {
        mCreatedBy = user;
        return this;
    }


    public RequestItem setCreatedAt(String date) {
        mCreatedAt = date;
        return this;
    }

    public RequestItem setLatitude(double latitude) {
        mLat = latitude;
        return  this;
    }

    public RequestItem setLongitude(double longitude) {
        mLong = longitude;
        return this;
    }


    public RequestItem setCreatedPollutionLevel(int pollutionLevel) {
        mCreatedPollutionLevel = pollutionLevel;
        return this;
    }


    public RequestItem setCreatedComment(String comment) {
        mCreatedComment = comment;
        return this;
    }


    public RequestItem setCreatedPictures(String[] pictures) {
        mCreatedPictures = pictures;
        return this;
    }

    public RequestItem setPickedUpBy(UserItem user) {
        mPickedUpBy = user;
        return this;
    }

    public RequestItem setPickedUpAt(String date) {
        mPickedUpAt = date;
        return this;
    }


    public RequestItem setClosedBy(UserItem user) {
        mClosedBy = user;
        return this;
    }

    public RequestItem setClosedAt(String date) {
        mClosedAt = date;
        return this;
    }


    public RequestItem setClosedComment(String comment) {
        mClosedComment = comment;
        return this;
    }


    public RequestItem setClosedPictures(String[] pictures) {
        mClosedPictures = pictures;
        return this;
    }



    // ---------------------------------------------------------------------------------------------
    //
    // Getters


    public long getId() {
        return mId;
    }

    public UserItem getCreatedBy() {
        return mCreatedBy;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getCreatedComment() {
        return mCreatedComment;
    }

    public String[] getCreatedPictures() {
        return mCreatedPictures;
    }

    public double getLat() {
        return mLat;
    }

    public double getLong() {
        return mLong;
    }

    public int getCreatedPollutionLevel() {
        return mCreatedPollutionLevel;
    }

    public UserItem getPickedUpBy() {
        return mPickedUpBy;
    }

    public String getPickedUpAt() {
        return mPickedUpAt;
    }

    public UserItem getClosedBy() {
        return mClosedBy;
    }

    public String getClosedAt() {
        return mClosedAt;
    }

    public String getClosedComment() {
        return mClosedComment;
    }

    public String[] getClosedPictures() {
        return mClosedPictures;
    }
}
