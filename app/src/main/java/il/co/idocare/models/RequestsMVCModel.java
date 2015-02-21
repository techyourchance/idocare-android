package il.co.idocare.models;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.pojos.RequestItem;

/**
 * Created by Vasiliy on 2/19/2015.
 */
public class RequestsMVCModel {

    private List<RequestItem> mRequestItems;

    public RequestsMVCModel() {
        mRequestItems = new ArrayList<RequestItem>();
    }


    public List<RequestItem> getAllRequests() {
        return mRequestItems;
    }


    /**
     * Get request item having a particular ID
     * @param id ID of the request
     * @return RequestItem object having the required ID, or null if there is no such request
     */
    public RequestItem getRequest(long id) {
        for (RequestItem item : mRequestItems) {
            if (item.mId == id)
                return item;
        }
        return null;
    }
}
