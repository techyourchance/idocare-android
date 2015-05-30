package il.co.idocare.pojos;


import android.content.ContentValues;
import android.database.Cursor;

import il.co.idocare.Constants.FieldName;
import il.co.idocare.contentproviders.IDoCareContract.Requests;

/**
 * This object contains data about a single request (users involved, state, location, etc)
 */
public class RequestItem {

    public enum RequestStatus {NEW_BY_OTHER, NEW_BY_ME, PICKED_UP_BY_OTHER, PICKED_UP_BY_ME,
        CLOSED_BY_OTHER, CLOSED_BY_ME}

    /**
     * This String array specifies the fields that are mandatory for RequestItem object in
     * order to be useful. This array can be used as is in queries against ContentProvider.
     */
    public static String[] MANDATORY_REQUEST_FIELDS = new String[] {
            FieldName.REQUEST_ID.getValue(),
            FieldName.CREATED_BY.getValue(),
            FieldName.CREATED_AT.getValue(),
            FieldName.CREATED_COMMENT.getValue(),
            FieldName.CREATED_PICTURES.getValue(),
            FieldName.CREATED_REPUTATION.getValue(),
            FieldName.CREATED_POLLUTION_LEVEL.getValue(),
            FieldName.PICKED_UP_BY.getValue(),
            FieldName.CLOSED_BY.getValue(),
            FieldName.LONGITUDE.getValue(),
            FieldName.LATITUDE.getValue(),
    };

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


    /**
     * Create RequestItem object having a particular id
     * @param id ID of the request
     * @return newly created RequestItem object
     */
    public static RequestItem createRequestItem(long id) {
        return new RequestItem(id);
    }

    /**
     * Create RequestItem object by querying the cursor at the current position.
     * @param cursor the Cursor to be queried
     * @return newly created RequestItem object
     * @throws IllegalArgumentException if any of the mandatory fields (as specified by
     * {@link RequestItem#MANDATORY_REQUEST_FIELDS}) are missing from the cursor
     */
    public static RequestItem createRequestItem(Cursor cursor) throws IllegalArgumentException {
        RequestItem request = null;

        // Mandatory fields
        try {
            long requestId = cursor.getLong(cursor.getColumnIndexOrThrow(FieldName.REQUEST_ID.getValue()));
            long createdBy = cursor.getLong(cursor.getColumnIndexOrThrow(FieldName.CREATED_BY.getValue()));
            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(FieldName.CREATED_AT.getValue()));
            String createdComment = cursor.getString(cursor.getColumnIndexOrThrow(FieldName.CREATED_COMMENT.getValue()));
            String createdPictures = cursor.getString(cursor.getColumnIndexOrThrow(FieldName.CREATED_PICTURES.getValue()));
            int createdReputation = cursor.getInt(cursor.getColumnIndexOrThrow(FieldName.CREATED_REPUTATION.getValue()));
            int createdPollutionLevel = cursor.getInt(cursor.getColumnIndexOrThrow(FieldName.CREATED_POLLUTION_LEVEL.getValue()));
            long pickedUpBy = cursor.getLong(cursor.getColumnIndexOrThrow(FieldName.PICKED_UP_BY.getValue()));
            long closedBy = cursor.getLong(cursor.getColumnIndexOrThrow(FieldName.CLOSED_BY.getValue()));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(FieldName.LATITUDE.getValue()));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(FieldName.LONGITUDE.getValue()));


            request = createRequestItem(requestId);
            request = RequestItem.createRequestItem(requestId);
            request.setCreatedBy(createdBy);
            request.setCreatedAt(createdAt);
            request.setCreatedComment(createdComment);
            request.setCreatedPictures(createdPictures);
            request.setCreatedReputation(createdReputation);
            request.setCreatedPollutionLevel(createdPollutionLevel);
            request.setPickedUpBy(pickedUpBy);
            request.setClosedBy(closedBy);
            request.setLatitude(latitude);
            request.setLongitude(longitude);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Couldn't create a new RequestItem: one or more " +
                    "of the mandatory fields missing from the cursor", e);
        }

        // Non-mandatory fields

        int i;

        if ((i = cursor.getColumnIndex(FieldName.PICKED_UP_AT.getValue())) != -1) {
            request.setPickedUpAt(cursor.getString(i));
        }
        if ((i = cursor.getColumnIndex(FieldName.CLOSED_AT.getValue())) != -1) {
            request.setClosedAt(cursor.getString(i));
        }
        if ((i = cursor.getColumnIndex(FieldName.CLOSED_COMMENT.getValue())) != -1) {
            request.setClosedComment(cursor.getString(i));
        }
        if ((i = cursor.getColumnIndex(FieldName.CLOSED_PICTURES.getValue())) != -1) {
            request.setClosedPictures(cursor.getString(i));
        }
        if ((i = cursor.getColumnIndex(FieldName.CLOSED_REPUTATION.getValue())) != -1) {
            request.setClosedReputation(cursor.getInt(i));
        }


        return request;

    }

    private RequestItem(long id) {
        mId = id;
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Adapters (converters)

    /**
     * Convert this request object to ContentValues object that can be passed to ContentProvider
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
