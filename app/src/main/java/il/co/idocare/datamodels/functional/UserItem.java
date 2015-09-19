package il.co.idocare.datamodels.functional;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import il.co.idocare.Constants;
import il.co.idocare.contentproviders.IDoCareContract;

/**
 * Object containing data about registered users
 */
public class UserItem {


    public static final String[] MANDATORY_USER_FIELDS = {
            Constants.FIELD_NAME_USER_ID,
            Constants.FIELD_NAME_USER_NICKNAME,
            Constants.FIELD_NAME_USER_FIRST_NAME,
            Constants.FIELD_NAME_USER_LAST_NAME,
            Constants.FIELD_NAME_USER_REPUTATION,
            Constants.FIELD_NAME_USER_PICTURE
    };


    @SerializedName(Constants.FIELD_NAME_USER_ID)
    private long mId;
    @SerializedName(Constants.FIELD_NAME_USER_NICKNAME)
    private String mNickname;
    @SerializedName(Constants.FIELD_NAME_USER_FIRST_NAME)
    private String mFirstName;
    @SerializedName(Constants.FIELD_NAME_USER_LAST_NAME)
    private String mLastName;
    @SerializedName(Constants.FIELD_NAME_USER_REPUTATION)
    private int mReputation;
    @SerializedName(Constants.FIELD_NAME_USER_PICTURE)
    private String mPictureUrl;



    private UserItem(long id) {
        mId = id;
    }

    public static UserItem create(long userId) {
        return new UserItem(userId);
    }

    /**
     * Create UserItem object by querying the cursor at the current position.
     * @param cursor the Cursor to be queried
     * @return newly created UserItem object
     * @throws IllegalArgumentException if any of the mandatory fields (as specified by
     * {@link UserItem#MANDATORY_USER_FIELDS}) are missing from the cursor
     */
    public static UserItem create(Cursor cursor) {
        UserItem user = null;

        // Mandatory fields
        try {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_USER_ID));
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_USER_NICKNAME));
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_USER_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_USER_LAST_NAME));
            int reputation = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_USER_REPUTATION));
            String pictureUrl = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_NAME_USER_PICTURE));

            user = create(id);
            user.setNickname(nickname);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setReputation(reputation);
            user.setPictureUrl(pictureUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Couldn't create a new RequestItemPojo: one or more " +
                    "of the mandatory fields missing from the cursor", e);
        }

        return user;
    }

    /**
     * Create UserItem from a string formatted as JSON object
     */
    public static UserItem create(String jsonObjectString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonObjectString, UserItem.class);

    }


    public static UserItem createAnonymousUser() {
        UserItem user = create(0);
        user.setFirstName("-");
        user.setLastName("-");
        user.setReputation(0);
        user.setPictureUrl("");
        return user;
    }

    /**
     * Convert this request object to ContentValues object that can be passed to ContentProvider
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(IDoCareContract.Users.COL_USER_ID, getId());
        values.put(IDoCareContract.Users.COL_USER_NICKNAME, getNickname());
        values.put(IDoCareContract.Users.COL_USER_FIRST_NAME, getFirstName());
        values.put(IDoCareContract.Users.COL_USER_LAST_NAME, getLastName());
        values.put(IDoCareContract.Users.COL_USER_REPUTATION, getReputation());
        values.put(IDoCareContract.Users.COL_USER_PICTURE, getPictureUrl());

        return values;
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

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public int getReputation() {
        return mReputation;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

}

