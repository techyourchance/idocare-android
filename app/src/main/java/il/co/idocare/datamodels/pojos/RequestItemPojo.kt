package il.co.idocare.datamodels.pojos


import android.content.ContentValues
import android.text.TextUtils

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

import il.co.idocare.Constants
import il.co.idocare.contentproviders.IDoCareContract.Requests
import il.co.idocare.utils.UtilMethods

/**
 * This object encapsulates the data
 */
open class RequestItemPojo(id: Long, createdBy: Long, createdAt: String, createdComment: String,
                           createdPictures: String, latitude: Double, longitude: Double) {


    // ---------------------------------------------------------------------------------------------
    //
    // Getters


    @SerializedName(Constants.FIELD_NAME_REQUEST_ID)
    private var id: Long = 0
    @SerializedName(Constants.FIELD_NAME_CREATED_BY)
    private var mCreatedBy: Long = 0
    @SerializedName(Constants.FIELD_NAME_CREATED_AT)
    private var mCreatedAt: String? = null
    @SerializedName(Constants.FIELD_NAME_CREATED_COMMENT)
    private var mCreatedComment: String? = null
    @SerializedName(Constants.FIELD_NAME_CREATED_PICTURES)
    private var mCreatedPictures: String? = null
    @SerializedName(Constants.FIELD_NAME_CREATED_REPUTATION)
    var createdVotes = 0
        private set
    @SerializedName(Constants.FIELD_NAME_LATITUDE)
    private var mLat = 0.0
    @SerializedName(Constants.FIELD_NAME_LONGITUDE)
    private var mLong = 0.0
    @SerializedName(Constants.FIELD_NAME_CREATED_POLLUTION_LEVEL)
    private var mCreatedPollutionLevel = 0
    @SerializedName(Constants.FIELD_NAME_PICKED_UP_BY)
    private var mPickedUpBy: Long = 0
    @SerializedName(Constants.FIELD_NAME_PICKED_UP_AT)
    private var mPickedUpAt: String? = null
    @SerializedName(Constants.FIELD_NAME_CLOSED_BY)
    private var mClosedBy: Long = 0
    @SerializedName(Constants.FIELD_NAME_CLOSED_AT)
    private var mClosedAt: String? = null
    @SerializedName(Constants.FIELD_NAME_CLOSED_COMMENT)
    private var mClosedComment: String? = null
    @SerializedName(Constants.FIELD_NAME_CLOSED_PICTURES)
    private var mClosedPictures: String? = null
    @SerializedName(Constants.FIELD_NAME_CLOSED_REPUTATION)
    var closedVotes = 0
        private set
    @SerializedName(Constants.FIELD_NAME_LOCATION)
    private var mLocation: String? = null


    init {
        this.id = id
        mCreatedBy = createdBy
        mCreatedAt = createdAt
        mCreatedComment = createdComment
        mCreatedPictures = createdPictures
        mLat = latitude
        mLong = longitude
    }


    /*
    TODO: why is this method here? Why formatting dates at all? If formatting - not here!
     */
    protected fun formatDates() {
        if (!TextUtils.isEmpty(getCreatedAt()))
            setCreatedAt(UtilMethods.formatDate(getCreatedAt()))
        if (!TextUtils.isEmpty(getPickedUpAt()))
            setPickedUpAt(UtilMethods.formatDate(getPickedUpAt()))
        if (!TextUtils.isEmpty(getClosedAt()))
            setClosedAt(UtilMethods.formatDate(getClosedAt()))
    }


    /**
     * Convert this request object to ContentValues object that can be passed to ContentProvider
     */
    open fun toContentValues(): ContentValues {
        val values = ContentValues()

        values.put(Requests.COL_REQUEST_ID, id)
        values.put(Requests.COL_CREATED_BY, getCreatedBy())
        values.put(Requests.COL_CREATED_AT, getCreatedAt())
        values.put(Requests.COL_CREATED_COMMENT, getCreatedComment())
        values.put(Requests.COL_CREATED_PICTURES, getCreatedPictures())
        values.put(Requests.COL_CREATED_VOTES, createdVotes)
        values.put(Requests.COL_LATITUDE, getLatitude())
        values.put(Requests.COL_LONGITUDE, getLongitude())
        values.put(Requests.COL_POLLUTION_LEVEL, getCreatedPollutionLevel())
        values.put(Requests.COL_PICKED_UP_BY, getPickedUpBy())
        values.put(Requests.COL_PICKED_UP_AT, getPickedUpAt())
        values.put(Requests.COL_CLOSED_BY, getClosedBy())
        values.put(Requests.COL_CLOSED_AT, getClosedAt())
        values.put(Requests.COL_CLOSED_COMMENT, getClosedComment())
        values.put(Requests.COL_CLOSED_PICTURES, getClosedPictures())
        values.put(Requests.COL_CLOSED_VOTES, closedVotes)
        values.put(Requests.COL_LOCATION, getLocation())

        return values
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Setters

    fun setCreatedBy(user: Long): RequestItemPojo {
        mCreatedBy = user
        return this
    }


    fun setCreatedAt(date: String): RequestItemPojo {
        mCreatedAt = date
        return this
    }

    fun setLatitude(latitude: Double): RequestItemPojo {
        mLat = latitude
        return this
    }

    fun setLongitude(longitude: Double): RequestItemPojo {
        mLong = longitude
        return this
    }


    fun setCreatedPollutionLevel(pollutionLevel: Int): RequestItemPojo {
        mCreatedPollutionLevel = pollutionLevel
        return this
    }


    fun setCreatedComment(comment: String): RequestItemPojo {
        mCreatedComment = comment
        return this
    }


    fun setCreatedPictures(pictures: String): RequestItemPojo {
        mCreatedPictures = pictures
        return this
    }

    fun setCreatedReputation(reputation: Int): RequestItemPojo {
        createdVotes = reputation
        return this
    }

    fun setPickedUpBy(user: Long): RequestItemPojo {
        mPickedUpBy = user
        return this
    }

    fun setPickedUpAt(date: String): RequestItemPojo {
        mPickedUpAt = date
        return this
    }


    fun setClosedBy(user: Long): RequestItemPojo {
        mClosedBy = user
        return this
    }

    fun setClosedAt(date: String): RequestItemPojo {
        mClosedAt = date
        return this
    }


    fun setClosedComment(comment: String): RequestItemPojo {
        mClosedComment = comment
        return this
    }


    fun setClosedPictures(pictures: String): RequestItemPojo {
        mClosedPictures = pictures
        return this
    }

    fun setClosedReputation(reputation: Int): RequestItemPojo {
        closedVotes = reputation
        return this
    }


    fun setLocation(location: String): RequestItemPojo {
        mLocation = location
        return this
    }
    fun getId(): Long {
        return id
    }

    fun getCreatedBy(): Long {
        return mCreatedBy
    }

    fun getCreatedAt(): String? {
        return mCreatedAt
    }

    fun getCreatedComment(): String? {
        return mCreatedComment
    }

    fun getCreatedPictures(): String? {
        return mCreatedPictures
    }

    fun getLatitude(): Double {
        return mLat
    }

    fun getLongitude(): Double {
        return mLong
    }

    fun getCreatedPollutionLevel(): Int {
        return mCreatedPollutionLevel
    }

    fun getPickedUpBy(): Long {
        return mPickedUpBy
    }

    fun getPickedUpAt(): String? {
        return mPickedUpAt
    }

    fun getClosedBy(): Long {
        return mClosedBy
    }

    fun getClosedAt(): String? {
        return mClosedAt
    }

    fun getClosedComment(): String? {
        return mClosedComment
    }

    fun getClosedPictures(): String? {
        return mClosedPictures
    }

    fun getLocation(): String? {
        return mLocation
    }

    companion object {

        /**
         * Create RequestItemPojo from a string formatted as JSON object
         * @param jsonObjectString a string formatted as JSON object having request's data
         */
        fun create(jsonObjectString: String): RequestItemPojo {
            val gson = Gson()
            val request = gson.fromJson<RequestItemPojo>(jsonObjectString, RequestItemPojo::class.java!!)

            request.formatDates()

            return request
        }
    }


}
