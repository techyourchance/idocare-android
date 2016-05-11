package il.co.idocare.datamodels.functional;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import il.co.idocare.Constants;
import il.co.idocare.datamodels.pojos.RequestItemPojo;

/**
 * Created by Vasiliy on 9/18/2015.
 */
public class RequestItem extends RequestItemPojo {

    private static final String LOG_TAG = RequestItem.class.getSimpleName();

    public enum RequestStatus {NEW_BY_OTHER, NEW_BY_ME, PICKED_UP_BY_OTHER, PICKED_UP_BY_ME,
        CLOSED_BY_OTHER, CLOSED_BY_ME, UNKNOWN}

    /**
     * This String array specifies the fields that are mandatory for RequestItem object in
     * order to be useful. This array can be used as is in queries against ContentProvider.
     */
    public static String[] MANDATORY_REQUEST_FIELDS = new String[] {
            Constants.FIELD_NAME_REQUEST_ID,
            Constants.FIELD_NAME_CREATED_BY,
            Constants.FIELD_NAME_CREATED_AT,
            Constants.FIELD_NAME_CREATED_COMMENT,
            Constants.FIELD_NAME_CREATED_PICTURES,
            Constants.FIELD_NAME_CREATED_REPUTATION,
            Constants.FIELD_NAME_CREATED_POLLUTION_LEVEL,
            Constants.FIELD_NAME_PICKED_UP_BY,
            Constants.FIELD_NAME_CLOSED_BY,
            Constants.FIELD_NAME_LONGITUDE,
            Constants.FIELD_NAME_LATITUDE,
    };




    private RequestStatus mStatus = RequestStatus.UNKNOWN;


    public RequestItem(long id, long createdBy, String createdAt, String createdComment,
                           String createdPictures, double latitude, double longitude) {
        super(id, createdBy, createdAt, createdComment, createdPictures, latitude, longitude);
    }

    /**
     * Create RequestItem object by querying the cursor at the current position.
     * @param cursor the Cursor to be queried
     * @return newly created RequestItem object
     * @throws IllegalArgumentException if any of the mandatory fields (as specified by
     * {@link RequestItem#MANDATORY_REQUEST_FIELDS}) are missing from the cursor
     */
    public static RequestItem create(Cursor cursor)
            throws IllegalArgumentException {
        RequestItem request = null;

        // Mandatory fields
        try {
            long requestId = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_REQUEST_ID));
            long createdBy = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_CREATED_BY));
            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_CREATED_AT));
            String createdComment = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_CREATED_COMMENT));
            String createdPictures = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_CREATED_PICTURES));
            int createdReputation = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_CREATED_REPUTATION));
            int createdPollutionLevel = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_CREATED_POLLUTION_LEVEL));
            long pickedUpBy = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_PICKED_UP_BY));
            long closedBy = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_CLOSED_BY));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_LONGITUDE));


            request = new RequestItem(requestId, createdBy, createdAt, createdComment,
                    createdPictures, latitude, longitude);
            request.setCreatedReputation(createdReputation);
            request.setCreatedPollutionLevel(createdPollutionLevel);
            request.setPickedUpBy(pickedUpBy);
            request.setClosedBy(closedBy);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Couldn't create a new RequestItemPojo: one or more " +
                    "of the mandatory fields missing from the cursor", e);
        }

        // Non-mandatory fields

        int i;

        if ((i = cursor.getColumnIndex(Constants.FIELD_NAME_PICKED_UP_AT)) != -1) {
            request.setPickedUpAt(cursor.getString(i));
        }
        if ((i = cursor.getColumnIndex(Constants.FIELD_NAME_CLOSED_AT)) != -1) {
            request.setClosedAt(cursor.getString(i));
        }
        if ((i = cursor.getColumnIndex(Constants.FIELD_NAME_CLOSED_COMMENT)) != -1) {
            request.setClosedComment(cursor.getString(i));
        }
        if ((i = cursor.getColumnIndex(Constants.FIELD_NAME_CLOSED_PICTURES)) != -1) {
            request.setClosedPictures(cursor.getString(i));
        }
        if ((i = cursor.getColumnIndex(Constants.FIELD_NAME_CLOSED_REPUTATION)) != -1) {
            request.setClosedReputation(cursor.getInt(i));
        }
        if ((i = cursor.getColumnIndex(Constants.FIELD_NAME_LOCATION)) != -1) {
            request.setLocation(cursor.getString(i));
        }

        request.formatDates();

        return request;

    }


    /**
     * Create RequestItemPojo from a string formatted as JSON object
     * @param jsonObjectString a string formatted as JSON object having request's data
     */
    public static RequestItem create(String jsonObjectString) {
        RequestItemPojo requestPojo = RequestItemPojo.create(jsonObjectString);

        RequestItem request = RequestItem.create(requestPojo);

        request.formatDates();

        return request;
    }


    /**
     * Create a new RequestItem from RequestItemPojo
     */
    public static RequestItem create (RequestItemPojo other) {
        RequestItem newRequest = new RequestItem(other.getId(), other.getCreatedBy(),
                other.getCreatedAt(), other.getCreatedComment(), other.getCreatedPictures(),
                other.getLatitude(), other.getLongitude());
        newRequest.setCreatedReputation(other.getCreatedVotes())
                .setCreatedPollutionLevel(other.getCreatedPollutionLevel())
                .setPickedUpBy(other.getPickedUpBy())
                .setPickedUpAt(other.getPickedUpAt())
                .setClosedBy(other.getClosedBy())
                .setClosedAt(other.getClosedAt())
                .setClosedComment(other.getClosedComment())
                .setClosedPictures(other.getClosedPictures())
                .setClosedReputation(other.getClosedVotes())
                .setLocation(other.getLocation());

        return newRequest;
    }


    /**
     * Create a copy of the existing RequestItem
     */
    public static RequestItem create (RequestItem other) {
        // Use the "create from POJO" method and add the additional fields from RequestItem
        RequestItem newRequest = RequestItem.create((RequestItemPojo) other);

        newRequest.setStatus(other.getStatus());

        return newRequest;
    }


    /**
     * This method will set a correct status for this request based on the ID of the currently
     * active user.
     * @param activeUserId ID of the active user. 0 will be treated as "no logged in user"
     */
    public void setStatus(long activeUserId) {

        if (getClosedBy() != 0) {
            if (activeUserId == getClosedBy())
                setStatus(RequestStatus.CLOSED_BY_ME);
            else
                setStatus(RequestStatus.CLOSED_BY_OTHER);
        } else if (getPickedUpBy() != 0) {
            if (activeUserId == getPickedUpBy())
                setStatus(RequestStatus.PICKED_UP_BY_ME);
            else
                setStatus(RequestStatus.PICKED_UP_BY_OTHER);
        } else if (getCreatedBy() != 0) {
            if (activeUserId == getCreatedBy())
                setStatus(RequestStatus.NEW_BY_ME);
            else
                setStatus(RequestStatus.NEW_BY_OTHER);
        } else {
            setStatus(RequestStatus.UNKNOWN);
            Log.e(LOG_TAG, "Could not set request status! Supplied user ID: " + activeUserId +
                    "\nFields values:" +
                    "\nCreated by: " + getCreatedBy() +
                    "\nPicked up by: " + getPickedUpBy() +
                    "\nClosed by: " + getClosedBy());
        }
    }




    /**
     * This method will set a correct status for this request based on the ID of the currently
     * active user.
     * @param activeUserId ID of the active user. 0 will be treated as "no logged in user"
     */
    public void setStatus(String activeUserId) {
        if (TextUtils.isEmpty(activeUserId))
            setStatus(0);
        else
            setStatus(Long.valueOf(activeUserId));
    }




    public boolean isClosed() {
        if (mStatus == RequestStatus.UNKNOWN)
            throw new IllegalStateException("request's status wasn't set");
        return mStatus == RequestStatus.CLOSED_BY_OTHER || mStatus == RequestStatus.CLOSED_BY_ME;
    }

    public boolean isPickedUp() {
        if (mStatus == RequestStatus.UNKNOWN)
            throw new IllegalStateException("request's status wasn't set");
        return mStatus == RequestStatus.PICKED_UP_BY_ME || mStatus == RequestStatus.PICKED_UP_BY_OTHER;
    }

    @Override
    public ContentValues toContentValues() {
        return super.toContentValues();
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Setters

    public RequestItem setStatus(RequestStatus status) {
        mStatus = status;
        return this;
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Getters

    public RequestStatus getStatus() {
        return mStatus;
    }




}
