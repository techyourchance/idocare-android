package il.co.idocare.requests;


import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This object encapsulates the data about a single request
 */
public class RequestEntity {

    private final String mId;

    private final String mCreatedBy;

    private final String mCreatedAt;

    private final String mCreatedComment;

    private final List<String> mCreatedPictures;

    private final int mCreatedVotes;
    
    private final double mLatitude;
    
    private final double mLongitude;
    
    private final String mPickedUpBy;
    
    private final String mPickedUpAt;
    
    private final String mClosedBy;
    
    private final String mClosedAt;
    
    private final String mClosedComment;
    
    private final List<String> mClosedPictures;
    
    private final int mClosedVotes;
    
    private final String mLocation;

    private final boolean mModifiedLocally;

    public RequestEntity(String id, String createdBy, String createdAt, String createdComment,
                         List<String> createdPictures, int createdVotes,
                         double latitude, double longitude, String pickedUpBy, String pickedUpAt,
                         String closedBy, String closedAt, String closedComment,
                         List<String> closedPictures, int closedVotes, String location,
                         boolean modifiedLocally) {
        mId = id;
        mCreatedBy = createdBy;
        mCreatedAt = createdAt;
        mCreatedComment = createdComment;
        mCreatedPictures = createdPictures != null ? new ArrayList<>(createdPictures) : new ArrayList<String>(0);
        mCreatedVotes = createdVotes;
        mLatitude = latitude;
        mLongitude = longitude;
        mPickedUpBy = pickedUpBy;
        mPickedUpAt = pickedUpAt;
        mClosedBy = closedBy;
        mClosedAt = closedAt;
        mClosedComment = closedComment;
        mClosedPictures = closedPictures != null ? new ArrayList<>(closedPictures) : new ArrayList<String>(0);
        mClosedVotes = closedVotes;
        mLocation = location;
        mModifiedLocally = modifiedLocally;
    }



    // ---------------------------------------------------------------------------------------------
    //
    // Builder instantiation

    public static RequestEntityBuilder getBuilder() {
        return new RequestEntityBuilder();
    }

    public static RequestEntityBuilder getBuilder(@NonNull RequestEntity requestEntity) {
        return new RequestEntityBuilder(requestEntity);
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Status

    /**
     * NOTE: this method will return "true" for both picked up and closed requests
     * @return true if this request has already been picked up
     */
    public boolean isPickedUp() {
        return getPickedUpBy() != null && !getPickedUpBy().isEmpty();
    }

    /**
     * @return true if this request has already been closed
     */
    public boolean isClosed() {
        return getClosedBy() != null && !getClosedBy().isEmpty();
    }

    // ---------------------------------------------------------------------------------------------
    //
    // Getters


    public String getId() {
        return String.valueOf(mId);
    }

    public String getCreatedBy() {
        return mCreatedBy;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getCreatedComment() {
        return mCreatedComment;
    }

    public List<String> getCreatedPictures() {
        return Collections.unmodifiableList(mCreatedPictures);
    }

    public int getCreatedVotes() {
        return mCreatedVotes;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public String getPickedUpBy() {
        return mPickedUpBy;
    }

    public String getPickedUpAt() {
        return mPickedUpAt;
    }

    public String getClosedBy() {
        return mClosedBy;
    }

    public String getClosedAt() {
        return mClosedAt;
    }

    public String getClosedComment() {
        return mClosedComment;
    }

    public List<String> getClosedPictures() {
        return Collections.unmodifiableList(mClosedPictures);
    }

    public int getClosedVotes() {
        return mClosedVotes;
    }

    public String getLocation() {
        return mLocation;
    }

    public boolean isModifiedLocally() {
        return mModifiedLocally;
    }

    // ---------------------------------------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------------------------------------


    public static class RequestEntityBuilder {

        private String mId;
        private String mCreatedBy = null;
        private String mCreatedAt = null;
        private String mCreatedComment = null;
        private List<String> mCreatedPictures = null;
        private int mCreatedVotes = 0;
        private double mLatitude = 0.0;
        private double mLongitude = 0.0;
        private String mPickedUpBy = null;
        private String mPickedUpAt = null;
        private String mClosedBy = null;
        private String mClosedAt = null;
        private String mClosedComment = null;
        private List<String> mClosedPictures = null;
        private int mClosedVotes = 0;
        private String mLocation = null;
        private boolean mModifiedLocally = false;

        private RequestEntityBuilder() {}

        private RequestEntityBuilder(@NonNull RequestEntity requestEntity) {
            mId = requestEntity.mId;
            mCreatedBy = requestEntity.mCreatedBy;
            mCreatedAt = requestEntity.mCreatedAt;
            mCreatedComment = requestEntity.mCreatedComment;
            mCreatedPictures = requestEntity.mCreatedPictures;
            mCreatedVotes = requestEntity.mCreatedVotes;
            mLatitude = requestEntity.mLatitude;
            mLongitude = requestEntity.mLongitude;
            mPickedUpBy = requestEntity.mPickedUpBy;
            mPickedUpAt = requestEntity.mPickedUpAt;
            mClosedBy = requestEntity.mClosedBy;
            mClosedAt = requestEntity.mClosedAt;
            mClosedComment = requestEntity.mClosedComment;
            mClosedPictures = requestEntity.mClosedPictures;
            mClosedVotes = requestEntity.mClosedVotes;
            mLocation = requestEntity.mLocation;
            mModifiedLocally = requestEntity.mModifiedLocally;
        }

        public RequestEntityBuilder setId(String id) {
            mId = id;
            return this;
        }

        public RequestEntityBuilder setCreatedBy(String createdBy) {
            mCreatedBy = createdBy;
            return this;
        }

        public RequestEntityBuilder setCreatedAt(String createdAt) {
            mCreatedAt = createdAt;
            return this;
        }

        public RequestEntityBuilder setCreatedComment(String createdComment) {
            mCreatedComment = createdComment;
            return this;
        }

        public RequestEntityBuilder setCreatedPictures(List<String> createdPictures) {
            mCreatedPictures = createdPictures;
            return this;
        }

        public RequestEntityBuilder setCreatedVotes(int createdVotes) {
            mCreatedVotes = createdVotes;
            return this;
        }

        public RequestEntityBuilder setLatitude(double latitude) {
            mLatitude = latitude;
            return this;
        }

        public RequestEntityBuilder setLongitude(double longitude) {
            mLongitude = longitude;
            return this;
        }

        public RequestEntityBuilder setPickedUpBy(String pickedUpBy) {
            mPickedUpBy = pickedUpBy;
            return this;
        }

        public RequestEntityBuilder setPickedUpAt(String pickedUpAt) {
            mPickedUpAt = pickedUpAt;
            return this;
        }

        public RequestEntityBuilder setClosedBy(String closedBy) {
            mClosedBy = closedBy;
            return this;
        }

        public RequestEntityBuilder setClosedAt(String closedAt) {
            mClosedAt = closedAt;
            return this;
        }

        public RequestEntityBuilder setClosedComment(String closedComment) {
            mClosedComment = closedComment;
            return this;
        }

        public RequestEntityBuilder setClosedPictures(List<String> closedPictures) {
            mClosedPictures = closedPictures;
            return this;
        }

        public RequestEntityBuilder setClosedVotes(int closedVotes) {
            mClosedVotes = closedVotes;
            return this;
        }

        public RequestEntityBuilder setLocation(String location) {
            mLocation = location;
            return this;
        }

        public RequestEntityBuilder setModifiedLocally(boolean modifiedLocally) {
            mModifiedLocally = modifiedLocally;
            return this;
        }

        public RequestEntity build() {
            return new RequestEntity(mId, mCreatedBy, mCreatedAt, mCreatedComment, mCreatedPictures,
                    mCreatedVotes, mLatitude, mLongitude, mPickedUpBy, mPickedUpAt, mClosedBy,
                    mClosedAt, mClosedComment, mClosedPictures, mClosedVotes, mLocation, mModifiedLocally);
        }
    }
}
