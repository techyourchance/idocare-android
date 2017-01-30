package il.co.idocare.networking.newimplementation.schemes;

import com.google.gson.annotations.SerializedName;

public class RequestScheme {
    @SerializedName("request_id") private String mId;
    @SerializedName("created_by") private String mCreatedBy;
    @SerializedName("picked_up_by") private String mPickedUpBy;
    @SerializedName("created_at") private String mCreatedAt;
    @SerializedName("picked_up_at") private String mPickedUpAt;
    @SerializedName("closed_at") private String mClosedAt;
    @SerializedName("created_comment") private String mCreatedComment;
    @SerializedName("closed_comment") private String mClosedComment;
    @SerializedName("created_pictures") private String mCreatedPictures;
    @SerializedName("closed_pictures") private String mClosedPictures;
    @SerializedName("created_reputation") private int mCreatedReputation;
    @SerializedName("closed_reputation") private int mClosedReputation;
    @SerializedName("closed_by") private String mClosedBy;
    @SerializedName("lat") private double mLatitude;
    @SerializedName("long") private double mLongitude;

    public String getId() {
        return mId;
    }

    public String getCreatedBy() {
        return mCreatedBy;
    }

    public String getPickedUpBy() {
        return mPickedUpBy;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getPickedUpAt() {
        return mPickedUpAt;
    }

    public String getClosedAt() {
        return mClosedAt;
    }

    public String getCreatedComment() {
        return mCreatedComment;
    }

    public String getClosedComment() {
        return mClosedComment;
    }

    public String getCreatedPictures() {
        return mCreatedPictures;
    }

    public String getClosedPictures() {
        return mClosedPictures;
    }

    public int getCreatedReputation() {
        return mCreatedReputation;
    }

    public int getClosedReputation() {
        return mClosedReputation;
    }

    public String getClosedBy() {
        return mClosedBy;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }
}
