package il.co.idocare.pojos;

import android.hardware.usb.UsbRequest;
import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.client.UserTokenHandler;
import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants;

/**
 * Object containing data about registered users
 */
public class UserItem {

    private long mId;
    private String mNickname;
    private String mFirstName;
    private String mLastName;
    private int mReputation;
    private String mPictureUrl;

    public static UserItem createUserItem(long userId) {
        UserItem item = new UserItem(userId);

        return item;
    }

    private UserItem() {}

    private UserItem(long id) {
        mId = id;
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Setters

    public UserItem setNickname(String nickname) {
        mNickname = nickname;
        return this;
    }
    
    public UserItem setFirstName(String firstName) {
        mFirstName = firstName;
        return this;
    }

    public UserItem setLastName(String lastName) {
        mLastName = lastName;
        return this;
    }

    public UserItem setReputation(int reputation) {
        mReputation = reputation;
        return this;
    }

    public UserItem setPictureUrl(String url) {
        mPictureUrl = url;
        return this;
    }

    // ---------------------------------------------------------------------------------------------
    //
    // Getters


    public long getId() {
        return mId;
    }

    public String getNickname() {
        return mNickname;
    }

    public int getReputation() {
        return mReputation;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }
}
