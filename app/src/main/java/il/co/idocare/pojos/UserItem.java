package il.co.idocare.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants;

/**
 * Object containing data about registered users
 */
public class UserItem {

    public long mId;
    public String mNickname;

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




    // ---------------------------------------------------------------------------------------------
    //
    // Getters


    public long getId() {
        return mId;
    }

    public String getNickname() {
        return mNickname;
    }
}
