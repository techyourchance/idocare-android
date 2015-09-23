package il.co.idocare;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class Constants {

    public static final java.lang.String PICTURES_LIST_SEPARATOR = ", ";


    public static final int REQUEST_CODE_LOGIN = 0;



    // ---------------------------------------------------------------------------------------------
    //
    // URLs

    // Host
    private final static String DEV_ROOT_URL = "http://dev-04.idocare.co.il";
    private final static String QA_ROOT_URL = "http://qa-04.idocare.co.il";
    public final static String ROOT_URL = DEV_ROOT_URL;

    // Nodes
    public final static String SIGN_UP_NATIVE_URL = ROOT_URL + "/api-04/user/add";
    public final static String LOG_IN_NATIVE_URL = ROOT_URL + "/api-04/user/login";
    public final static String GET_NATIVE_USER_DATA_URL = ROOT_URL + "/api-04/user/get";

    public final static String GET_ALL_REQUESTS_URL = ROOT_URL + "/api-04/request";
    public final static String GET_REQUEST_DATA_URL = ROOT_URL + "/api-04/request/get";
    
    public final static String GET_ALL_ARTICLES_URL = ROOT_URL + "/api-04/article";

    // End of URLs
    //
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    //
    // Names of fields used in communication with the server

    public static final String FIELD_NAME_INTERNAL_STATUS = "status";
    public static final String FIELD_NAME_RESPONSE_MESSAGE = "message";
    public static final String FIELD_NAME_RESPONSE_DATA = "data";


    public static final String FIELD_NAME_USER_ID = "user_data_id";
    public static final String FIELD_NAME_USER_EMAIL = "user_data_email";
    public static final String FIELD_NAME_USER_NICKNAME = "user_data_nickname";
    public static final String FIELD_NAME_USER_FIRST_NAME = "user_data_first_name";
    public static final String FIELD_NAME_USER_LAST_NAME = "user_data_last_name";
    public static final String FIELD_NAME_USER_REPUTATION = "user_data_reputation";
    public static final String FIELD_NAME_USER_PICTURE = "user_data_picture";
    // TODO: the below two fields must be identical in name and in encoding
    public static final String FIELD_NAME_USER_PASSWORD_LOGIN = "user_data_auth";
    public static final String FIELD_NAME_USER_PASSWORD_SIGNUP = "user_data_password";
    public static final String FIELD_NAME_USER_PUBLIC_KEY = "user_data_public_key";
    public static final String FIELD_NAME_USER_FACEBOOK_ID= "user_data_fb_id";

    public static final String FIELD_NAME_ENTITY_ID = "entity_id";
    public static final String FIELD_NAME_ENTITY_PARAM = "entity_param";
    public static final String FIELD_NAME_SCORE = "score";

    public static final String FIELD_NAME_REQUEST_ID = "request_id";

    public static final String FIELD_NAME_CREATED_BY = "created_by";
    public static final String FIELD_NAME_CREATED_AT = "created_at";
    public static final String FIELD_NAME_CREATED_REPUTATION = "created_reputation";
    public static final String FIELD_NAME_CREATED_COMMENT = "created_comment";
    public static final String FIELD_NAME_CREATED_PICTURES = "created_pictures";
    public static final String FIELD_NAME_CREATED_POLLUTION_LEVEL = "pollution_level";
    public static final String FIELD_NAME_LONGITUDE = "long";
    public static final String FIELD_NAME_LATITUDE = "lat";

    public static final String FIELD_NAME_PICKED_UP_BY = "picked_up_by";
    public static final String FIELD_NAME_PICKED_UP_AT = "picked_up_at";
    public static final String FIELD_NAME_PICKED_UP_BY_ME = "picked_up_by_me";

    public static final String FIELD_NAME_CLOSED_BY = "closed_by";
    public static final String FIELD_NAME_CLOSED_AT = "closed_at";
    public static final String FIELD_NAME_CLOSED_COMMENT = "closed_comment";
    public static final String FIELD_NAME_CLOSED_PICTURES = "closed_pictures";
    public static final String FIELD_NAME_CLOSED_REPUTATION = "closed_reputation";

    public static final String FIELD_NAME_LOCATION= "location";

    // End of fields' names
    //
    // ---------------------------------------------------------------------------------------------


    /**
     * This enum (its ordinal() values) is used with startActivityForResult() and
     * onActivityResult() calls across the app
     */
    public enum StartActivityTag {
        CAPTURE_PICTURE
    }

    /**
     * Global app preferences file
     */
    public final static String PREFERENCES_FILE = "idocare_preferences";

    /**
     * An integer with a strictly positive value should be written to SharedPreferences if
     * the user chooses to skip login when the app starts
     */
    public final static String LOGIN_SKIPPED_KEY = "login_skipped";





    // TODO: alter the configuration of UIL according to our needs
    public final static DisplayImageOptions DEFAULT_DISPLAY_IMAGE_OPTIONS =
            new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
            .build();

    public static final String UIL_LOCAL_FILE_PREFIX = "file:///";




    /**
     * Static names of HTTP headers
     */
    public enum HttpHeader {
        USER_USERNAME("data"), USER_ID("id"), USER_TOKEN("token"), USER_TIMESTAMP("timestamp");

        private final static String PREFIX = "Idc-user-";

        private String mValue;
        HttpHeader(String value) {
            this.mValue = PREFIX + value;
        }

        public String getValue() {
            return mValue;
        }
    }


}
