package il.co.idocare.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants;

/**
 * Created by Vasiliy on 2/17/2015.
 */
public class UserItem implements Parcelable {

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeLong(mId);
        dest.writeString(mNickname);
    }


    public static final Creator<UserItem> CREATOR = new Creator<UserItem>() {
        @Override
        public UserItem[] newArray(int size) {
            return new UserItem[size];
        }

        @Override
        public UserItem createFromParcel(Parcel source) {
            return createUserItem(source.readLong())
                    .setNickname(source.readString());
        }
    };

    // ---------------------------------------------------------------------------------------------
    //
    // Setters

    public UserItem setNickname(String nickname) {
        mNickname = nickname;
        return this;
    }
}
