package il.co.idocare;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestItem implements Parcelable {

    private final static String LOG_TAG = "RequestItem";

    public String mId;
    public String mOpenedBy;
    public String mPickedUpBy;
    public String mCreationDate;
    public String mPickedUpDate;
    public String mCloseDate;
    public String mNoteBefore;
    public String mNoteAfter;
    public String mLat;
    public String mLong;
    public String[] mImagesBefore;
    public String[] mImagesAfter;
    public String mPollutionLevel;

    public static RequestItem createRequestItem(JSONObject requestJSONObject) {
        RequestItem item = new RequestItem();
        // TODO: field names in JSON should come from constants

        try {
            item.mId = requestJSONObject.getString("id");
            item.mOpenedBy = requestJSONObject.getString("opened_by");
            item.mPickedUpBy = requestJSONObject.getString("picked_up_by");
            item.mCreationDate = requestJSONObject.getString("creation_date");
            item.mPickedUpDate = requestJSONObject.getString("picked_up_date");
            item.mCloseDate = requestJSONObject.getString("close_date");
            item.mNoteBefore = requestJSONObject.getString("note_before");
            item.mNoteAfter = requestJSONObject.getString("note_after");
            item.mLat = requestJSONObject.getString("lat");
            item.mLong = requestJSONObject.getString("long");

            if (!requestJSONObject.getString("images_before").equals("") &&
                    !requestJSONObject.getString("images_before").equalsIgnoreCase("null")) {
                item.mImagesBefore = requestJSONObject.getString("images_before").split(", ");
            }

            if (!requestJSONObject.getString("images_after").equals("") &&
                    !requestJSONObject.getString("images_after").equalsIgnoreCase("null")) {
                item.mImagesAfter= requestJSONObject.getString("images_after").split(", ");
            }

            item.mPollutionLevel= requestJSONObject.getString("pollution_level");

        } catch (JSONException e) {
            item = null;
            e.printStackTrace();
        }

        return item;
    }


    public RequestItem(Parcel source) {
        if (!RequestItem.class.isAssignableFrom(source.getClass())) {
            Log.e(LOG_TAG, "the Parcel provided to the constructor is not of type RequestItem");
            return;
        }

        mId = source.readString();
        mOpenedBy = source.readString();
        mPickedUpBy = source.readString();
        mCreationDate = source.readString();
        mPickedUpDate = source.readString();
        mCloseDate = source.readString();
        mNoteBefore = source.readString();
        mNoteAfter = source.readString();
        mLat = source.readString();
        mLong = source.readString();
        source.readStringArray(mImagesBefore);
        source.readStringArray(mImagesAfter);
        mPollutionLevel= source.readString();
        
    }

    private RequestItem(){}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(mId);
        dest.writeString(mOpenedBy);
        dest.writeString(mPickedUpBy);
        dest.writeString(mCreationDate);
        dest.writeString(mPickedUpDate);
        dest.writeString(mCloseDate);
        dest.writeString(mNoteBefore);
        dest.writeString(mNoteAfter);
        dest.writeString(mLat);
        dest.writeString(mLong);
        dest.writeStringArray(mImagesBefore);
        dest.writeStringArray(mImagesAfter);
        dest.writeString(mPollutionLevel);
    }


    public static final Creator<RequestItem> CREATOR = new Creator<RequestItem>() {
        @Override
        public RequestItem[] newArray(int size) {
            return new RequestItem[size];
        }

        @Override
        public RequestItem createFromParcel(Parcel source) {
            return new RequestItem(source);
        }
    };
}
