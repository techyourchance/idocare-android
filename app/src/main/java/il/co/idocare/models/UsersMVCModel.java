package il.co.idocare.models;

import android.app.Activity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

import il.co.idocare.Constants;
import il.co.idocare.connectivity.ServerRequest;
import il.co.idocare.pojos.UserItem;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.IDoCareJSONUtils;

/**
 * Created by Vasiliy on 2/19/2015.
 */
public class UsersMVCModel extends AbstractModelMVC implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "UsersMVCModel";

    /*
     This object will be used for synchronization of the map underlying this model. We need this
     lock because even though the map which is used (ConcurrentHashMap) is thread safe, it is not
     synchronized in a sense that allows for modifications to be made while the map (or the
     Collection returned by its values() method) is being read.
     In cases when few modifications to the map should be done atomically (if such cases will be
     present at all), this lock object will be used.
     This object will also be used for blocking execution of threads using wait/notify mechanism.
      */
    private final Object LOCK = new Object();

    private ConcurrentHashMap<Long, UserItem> mUserItems;
    private Activity mActivity;


    public UsersMVCModel(Activity activity) {
        mUserItems = new ConcurrentHashMap<Long, UserItem>();
        // The only reason for this context is to be able to access shared preferences file
        // TODO: try to find a solution that does not require passing the context around
        mActivity = activity;
    }


    /**
     * Get user item having a particular ID.<br>
     * NOTE: the returned object might be empty, in which case the required data will be fetched
     * asynchronously. Clients using this method should also register for updates to this
     * particular ID.
     * @param id ID of the user
     * @return UserItem object having the required ID
     */
    public UserItem getUser (long id)  {
        if(!mUserItems.containsKey(id)) {
            mUserItems.put(id, UserItem.createUserItem(id));
            fetchUserDataFromServer(id);
        }

        return mUserItems.get(Long.valueOf(id));
    }


    /**
     * Execute a new server request asking to fetch user data
     */
    private void fetchUserDataFromServer(long id) {
        ServerRequest serverRequest = new ServerRequest(Constants.GET_USER_DATA_URL,
                Constants.ServerRequestTag.GET_USER_DATA, this);

        IDoCareHttpUtils.addStandardHeaders(mActivity, serverRequest);

        serverRequest.addTextField(Constants.FieldName.USER_ID.getValue(), String.valueOf(id));

        serverRequest.execute();
    }

    @Override
    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.GET_USER_DATA) {
            if (responseStatusOk && IDoCareJSONUtils.verifySuccessfulStatus(responseData)) {

                // TODO: decide how to handle JSON parsing exceptions. Maybe rerun server request?

                JSONObject userJSONObj = null;
                try {
                    userJSONObj = IDoCareJSONUtils.extractDataJSONObject(responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                UserItem userItem = null;
                try {
                    userItem = IDoCareJSONUtils.extractUserItemFromJSONObject(userJSONObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                updateModel(userItem);
            }
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }

    private void updateModel(UserItem userItem) {

        long id = userItem.getId();

        if (!mUserItems.containsKey(id)) {
            mUserItems.put(id, userItem);
            // Assuming that if this user wasn't in the map, then there are no listeners
            // registered for its changes.
        } else {
            // TODO: instead of simply replacing UserItem, we need to make sure there was an actual change
            mUserItems.remove(id);
            mUserItems.put(id, userItem);
            // Send a notification about change in data of a specific user
            notifyOutboxHandlers(Constants.MessageType.M_USER_DATA_UPDATE.ordinal(), 0, 0,
                    Long.valueOf(id));
        }
    }


}
