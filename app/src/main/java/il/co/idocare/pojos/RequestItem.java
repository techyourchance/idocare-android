package il.co.idocare.pojos;


import android.content.ContentValues;

import il.co.idocare.contentproviders.IDoCareContract.Requests;

/**
 * This object contains data about a single request (users involved, state, location, etc)
 */
public class RequestItem {

    public enum RequestStatus {NEW_BY_OTHER, NEW_BY_ME, PICKED_UP_BY_OTHER, PICKED_UP_BY_ME,
        CLOSED_BY_OTHER, CLOSED_BY_ME}

    private final static String LOG_TAG = "RequestItem";

    private long mId;
    private long mCreatedBy;
    private String mCreatedAt;
    private String mCreatedComment;
    private String mCreatedPictures;
    private int mCreatedReputation;
    private double mLat;
    private double mLong;
    private int mCreatedPollutionLevel;
    private long mPickedUpBy;
    private String mPickedUpAt;
    private long mClosedBy;
    private String mClosedAt;
    private String mClosedComment;
    private String mClosedPictures;
    private int mClosedReputation;


    public static RequestItem createRequestItem(long id) {
        return new RequestItem(id);
    }

    private RequestItem(long id) {
        mId = id;
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Adapters (converters)

    /**
     * Convert this request object to ContentValues object that can be passed to ContentPsovider
     * @return
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(Requests.REQUEST_ID, getId());
        values.put(Requests.CREATED_BY, getCreatedBy());
        values.put(Requests.CREATED_AT, getCreatedAt());
        values.put(Requests.CREATED_COMMENT, getCreatedComment());
        values.put(Requests.CREATED_PICTURES, getCreatedPictures());
        values.put(Requests.CREATED_REPUTATION, getCreatedReputation());
        values.put(Requests.LATITUDE, getLatitude());
        values.put(Requests.LONGITUDE, getLongitude());
        values.put(Requests.POLLUTION_LEVEL, getCreatedPollutionLevel());
        values.put(Requests.PICKED_UP_BY, getPickedUpBy());
        values.put(Requests.PICKED_UP_AT, getPickedUpAt());
        values.put(Requests.CLOSED_BY, getClosedBy());
        values.put(Requests.CLOSED_AT, getClosedAt());
        values.put(Requests.CLOSED_COMMENT, getClosedComment());
        values.put(Requests.CLOSED_PICTURES, getClosedPictures());
        values.put(Requests.CLOSED_REPUTATION, getClosedReputation());

        return values;
    }




    // ---------------------------------------------------------------------------------------------
    //
    // Setters

    public RequestItem setCreatedBy(long user) {
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


    public RequestItem setCreatedPictures(String pictures) {
        mCreatedPictures = pictures;
        return this;
    }

    public RequestItem setCreatedReputation(int reputation) {
        mCreatedReputation = reputation;
        return this;
    }

    public RequestItem setPickedUpBy(long user) {
        mPickedUpBy = user;
        return this;
    }

    public RequestItem setPickedUpAt(String date) {
        mPickedUpAt = date;
        return this;
    }


    public RequestItem setClosedBy(long user) {
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


    public RequestItem setClosedPictures(String pictures) {
        mClosedPictures = pictures;
        return this;
    }

    public RequestItem setClosedReputation(int reputation) {
        mClosedReputation = reputation;
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

    public int getCreatedReputation() {
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

    public int getClosedReputation() {
        return mClosedReputation;
    }

}
