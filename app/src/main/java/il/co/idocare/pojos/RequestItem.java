package il.co.idocare.pojos;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants.FieldName;

public class RequestItem implements Parcelable {

    private final static String LOG_TAG = "RequestItem";

    public long mId;
    public UserItem mCreatedBy;
    public String mCreatedAt;
    public String mCreatedComment;
    public String[] mCreatedPictures;
    public double mLat;
    public double mLong;
    public int mCreatedPollutionLevel;
    public UserItem mPickedUpBy;
    public String mPickedUpAt;
    public UserItem mClosedBy;
    public String mClosedAt;
    public String mClosedComment;
    public String[] mClosedPictures;



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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeLong(mId);
        dest.writeParcelable(mCreatedBy, 0);
        dest.writeString(mCreatedAt);
        dest.writeString(mCreatedComment);
        dest.writeStringArray(mCreatedPictures);
        dest.writeDouble(mLat);
        dest.writeDouble(mLong);
        dest.writeInt(mCreatedPollutionLevel);

        dest.writeParcelable(mPickedUpBy, 0);
        dest.writeString(mPickedUpAt);

        dest.writeParcelable(mClosedBy, 0);
        dest.writeString(mClosedAt);
        dest.writeString(mClosedComment);
        dest.writeStringArray(mClosedPictures);
    }


    public static final Creator<RequestItem> CREATOR = new Creator<RequestItem>() {
        @Override
        public RequestItem[] newArray(int size) {
            return new RequestItem[size];
        }

        @Override
        public RequestItem createFromParcel(Parcel source) {
            return createRequestItem(source.readLong())
                    .setCreatedBy((UserItem) source.readParcelable(UserItem.class.getClassLoader()))
                    .setCreatedAt(source.readString())
                    .setCreatedComment(source.readString())
                    .setCreatedPictures(source.createStringArray())
                    .setLatitude(source.readDouble())
                    .setLongitude(source.readDouble())
                    .setCreatedPollutionLevel(source.readInt())
                    .setPickedUpBy((UserItem) source.readParcelable(UserItem.class.getClassLoader()))
                    .setPickedUpAt(source.readString())
                    .setClosedBy((UserItem) source.readParcelable(UserItem.class.getClassLoader()))
                    .setClosedAt(source.readString())
                    .setClosedComment(source.readString())
                    .setClosedPictures(source.createStringArray());
        }
    };


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
