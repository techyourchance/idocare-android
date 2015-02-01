package il.co.idocare.www.idocare;


import org.json.JSONException;
import org.json.JSONObject;

public class RequestItem {


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


    private RequestItem() {}

}
