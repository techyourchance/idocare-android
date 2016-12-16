package il.co.idocare.datamodels.pojos;


import android.content.ContentValues;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import il.co.idocare.Constants;
import il.co.idocare.contentproviders.IDoCareContract.Requests;
import il.co.idocare.utils.UtilMethods;

/**
 * This object encapsulates the data
 */
public class RequestItemPojo {


    private final static String LOG_TAG = RequestItemPojo.class.getSimpleName();



    @SerializedName(Constants.FIELD_NAME_REQUEST_ID)
    private long mId = 0;
    @SerializedName(Constants.FIELD_NAME_CREATED_BY)
    private long mCreatedBy = 0;
    @SerializedName(Constants.FIELD_NAME_CREATED_AT)
    private String mCreatedAt = null;
    @SerializedName(Constants.FIELD_NAME_CREATED_COMMENT)
    private String mCreatedComment = null;
    @SerializedName(Constants.FIELD_NAME_CREATED_PICTURES)
    private String mCreatedPictures = null;
    @SerializedName(Constants.FIELD_NAME_CREATED_REPUTATION)
    private int mCreatedReputation = 0;
    @SerializedName(Constants.FIELD_NAME_LATITUDE)
    private double mLat = 0;
    @SerializedName(Constants.FIELD_NAME_LONGITUDE)
    private double mLong = 0;
    @SerializedName(Constants.FIELD_NAME_CREATED_POLLUTION_LEVEL)
    private int mCreatedPollutionLevel = 0;
    @SerializedName(Constants.FIELD_NAME_PICKED_UP_BY)
    private long mPickedUpBy = 0;
    @SerializedName(Constants.FIELD_NAME_PICKED_UP_AT)
    private String mPickedUpAt = null;
    @SerializedName(Constants.FIELD_NAME_CLOSED_BY)
    private long mClosedBy = 0;
    @SerializedName(Constants.FIELD_NAME_CLOSED_AT)
    private String mClosedAt = null;
    @SerializedName(Constants.FIELD_NAME_CLOSED_COMMENT)
    private String mClosedComment = null;
    @SerializedName(Constants.FIELD_NAME_CLOSED_PICTURES)
    private String mClosedPictures = null;
    @SerializedName(Constants.FIELD_NAME_CLOSED_REPUTATION)
    private int mClosedReputation = 0;
    @SerializedName(Constants.FIELD_NAME_LOCATION)
    private String mLocation = null;


    public RequestItemPojo(long id, long createdBy, String createdAt, String createdComment,
                           String createdPictures, double latitude, double longitude) {
        mId = id;
        mCreatedBy = createdBy;
        mCreatedAt = createdAt;
        mCreatedComment = createdComment;
        mCreatedPictures = createdPictures;
        mLat = latitude;
        mLong = longitude;
    }


    /**
     * Create RequestItemPojo from a string formatted as JSON object
     * @param jsonObjectString a string formatted as JSON object having request's data
     */
    public static RequestItemPojo create(String jsonObjectString) {
        Gson gson = new Gson();
        RequestItemPojo request = gson.fromJson(jsonObjectString, RequestItemPojo.class);

        request.formatDates();

        return request;
    }




    /*
    TODO: why is this method here? Why formatting dates at all? If formatting - not here!
     */
    protected void formatDates() {
        if (!TextUtils.isEmpty(getCreatedAt()))
            setCreatedAt(UtilMethods.formatDate(getCreatedAt()));
        if (!TextUtils.isEmpty(getPickedUpAt()))
            setPickedUpAt(UtilMethods.formatDate(getPickedUpAt()));
        if (!TextUtils.isEmpty(getClosedAt()))
            setClosedAt(UtilMethods.formatDate(getClosedAt()));
    }



    /**
     * Convert this request object to ContentValues object that can be passed to ContentProvider
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(Requests.COL_REQUEST_ID, getId());
        values.put(Requests.COL_CREATED_BY, getCreatedBy());
        values.put(Requests.COL_CREATED_AT, getCreatedAt());
        values.put(Requests.COL_CREATED_COMMENT, getCreatedComment());
        values.put(Requests.COL_CREATED_PICTURES, getCreatedPictures());
        values.put(Requests.COL_CREATED_VOTES, getCreatedVotes());
        values.put(Requests.COL_LATITUDE, getLatitude());
        values.put(Requests.COL_LONGITUDE, getLongitude());
        values.put(Requests.COL_POLLUTION_LEVEL, getCreatedPollutionLevel());
        values.put(Requests.COL_PICKED_UP_BY, getPickedUpBy());
        values.put(Requests.COL_PICKED_UP_AT, getPickedUpAt());
        values.put(Requests.COL_CLOSED_BY, getClosedBy());
        values.put(Requests.COL_CLOSED_AT, getClosedAt());
        values.put(Requests.COL_CLOSED_COMMENT, getClosedComment());
        values.put(Requests.COL_CLOSED_PICTURES, getClosedPictures());
        values.put(Requests.COL_CLOSED_VOTES, getClosedVotes());
        values.put(Requests.COL_LOCATION, getLocation());

        return values;
    }



    // ---------------------------------------------------------------------------------------------
    //
    // Setters

    public RequestItemPojo setCreatedBy(long user) {
        mCreatedBy = user;
        return this;
    }


    public RequestItemPojo setCreatedAt(String date) {
        mCreatedAt = date;
        return this;
    }

    public RequestItemPojo setLatitude(double latitude) {
        mLat = latitude;
        return  this;
    }

    public RequestItemPojo setLongitude(double longitude) {
        mLong = longitude;
        return this;
    }


    public RequestItemPojo setCreatedPollutionLevel(int pollutionLevel) {
        mCreatedPollutionLevel = pollutionLevel;
        return this;
    }


    public RequestItemPojo setCreatedComment(String comment) {
        mCreatedComment = comment;
        return this;
    }


    public RequestItemPojo setCreatedPictures(String pictures) {
        mCreatedPictures = pictures;
        return this;
    }

    public RequestItemPojo setCreatedReputation(int reputation) {
        mCreatedReputation = reputation;
        return this;
    }

    public RequestItemPojo setPickedUpBy(long user) {
        mPickedUpBy = user;
        return this;
    }

    public RequestItemPojo setPickedUpAt(String date) {
        mPickedUpAt = date;
        return this;
    }


    public RequestItemPojo setClosedBy(long user) {
        mClosedBy = user;
        return this;
    }

    public RequestItemPojo setClosedAt(String date) {
        mClosedAt = date;
        return this;
    }


    public RequestItemPojo setClosedComment(String comment) {
        mClosedComment = comment;
        return this;
    }


    public RequestItemPojo setClosedPictures(String pictures) {
        mClosedPictures = pictures;
        return this;
    }

    public RequestItemPojo setClosedReputation(int reputation) {
        mClosedReputation = reputation;
        return this;
    }


    public RequestItemPojo setLocation(String location) {
        mLocation = location;
        return this;
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Getters


    public long getId() {
        return mId;
    }

    public long getCreatedBy() {
        return mCreatedBy;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getCreatedComment() {
        return mCreatedComment;
    }

    public String getCreatedPictures() {
        return mCreatedPictures;
    }

    public int getCreatedVotes() {
        return mCreatedReputation;
    }

    public double getLatitude() {
        return mLat;
    }

    public double getLongitude() {
        return mLong;
    }

    public int getCreatedPollutionLevel() {
        return mCreatedPollutionLevel;
    }

    public long getPickedUpBy() {
        return mPickedUpBy;
    }

    public String getPickedUpAt() {
        return mPickedUpAt;
    }

    public long getClosedBy() {
        return mClosedBy;
    }

    public String getClosedAt() {
        return mClosedAt;
    }

    public String getClosedComment() {
        return mClosedComment;
    }

    public String getClosedPictures() {
        return mClosedPictures;
    }

    public int getClosedVotes() {
        return mClosedReputation;
    }

    public String getLocation() {
        return mLocation;
    }


}
