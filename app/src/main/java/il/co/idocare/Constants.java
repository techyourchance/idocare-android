package il.co.idocare;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class Constants {



    /**
     * This enum is used in order to relate HTTP response to a particular HTTP request
     */
    public enum ServerRequestTag {GET_ALL_REQUESTS, REQUEST_DETAILS, NEW_REQUEST,
        PICKUP_REQUEST, CLOSE_REQUEST, VOTE_FOR_REQUEST, GET_USER_DATA, LOGIN}

    /**
     * This enum (its ordinal() values) is used with startActivityForResult() and
     * onActivityResult() calls across the app
     */
    public enum StartActivityTag {CAPTURE_PICTURE_FOR_NEW_REQUEST}

    /**
     * Global app preferences file
     */
    public final static String PREFERENCES_FILE = "idocare_preferences";

    // TODO: alter the configuration of UIL according to our needs
    public final static DisplayImageOptions DEFAULT_DISPLAY_IMAGE_OPTIONS =
            new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();

    public static final String UIL_LOCAL_FILE_PREFIX = "file:///";




    // URLs used to issue requests to the server
    public final static String IMGLIST_URL = "http://dev-04.idocare.co.il/api-04/imglist";
    public final static String IMGTEST_URL = "http://dev-04.idocare.co.il/api-04/imgtest";
    public final static String LOGIN_URL = "http://dev-04.idocare.co.il/api-04/user/login";
    public final static String GET_ALL_REQUESTS_URL = "http://dev-04.idocare.co.il/api-04/request";
    public final static String ADD_REQUEST_URL = "http://dev-04.idocare.co.il/api-04/request/add";
    public final static String PICKUP_REQUEST_URL = "http://dev-04.idocare.co.il/api-04/request/pickup";
    public final static String VOTE_REQUEST_URL = "http://dev-04.idocare.co.il/api-04/request/vote";
    public final static String CLOSE_REQUEST_URL = "http://dev-04.idocare.co.il/api-04/request/close";
    public static final String GET_USER_DATA_URL = "http://dev-04.idocare.co.il/api-04/user/get";


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

    /**
     * Static names of HTTP and JSON fields
     */
    public enum FieldName {
        RESPONSE_STATUS("status"), RESPONSE_MESSAGE("message"), RESPONSE_DATA("data"),

        USER_PASSWORD("user_data_auth"), USER_NICKNAME("user_data_nickname"), USER_ID("user_data_id"),
        USER_PUBLIC_KEY("user_data_public_key"), USER_FIRST_NAME("user_data_first_name"),
        USER_LAST_NAME("user_data_last_name"), USER_REPUTATION("user_data_reputation"),
        USER_PICTURE("user_data_picture"),

        ENTITY_ID("entity_id"), ENTITY_PARAM("entity_param"), SCORE("score"),

        REQUEST_ID("request_id"),

        CREATED_BY("created_by"), CREATED_AT("created_at"), CREATED_REPUTATION("created_reputation"),
        CREATED_COMMENT("created_comment"), CREATED_PICTURES("created_pictures"),
        CREATED_POLLUTION_LEVEL("pollution_level"), LONGITUDE("long"), LATITUDE("lat"),

        PICKED_UP_BY("picked_up_by"), PICKED_UP_AT("picked_up_at"), PICKED_UP_BY_ME("picked_up_by_me"),

        CLOSED_BY("closed_by"), CLOSED_AT("closed_at"), CLOSED_COMMENT("closed_comment"),
        CLOSED_PICTURES("closed_pictures"), CLOSED_REPUTATION("closed_reputation");

        private String mValue;
        FieldName(String value) {
            this.mValue=value;
        }

        public String getValue() {
            return mValue;
        }
    }


    /**
     * Various message types that might be passed between MVC components.<br>
     * Prefix values:<br>
     *     M_ - messages originating in MVC Models<br>
     *     V_ - messages originating in MVC Views<br>
     *     C_ - messages originating in MVC Controllers<br>
     * Implementation note: the members of this enum might be used as regular enums are used, or
     * might be referenced/compared by their {@link Enum#ordinal()} value.
     *
     */
    public enum MessageType {
        V_REQUEST_ITEM_CLICKED,
        V_LOGIN_BUTTON_CLICK,
        V_ADD_NEW_REQUEST_BUTTON_CLICKED,
        V_CLOSE_REQUEST_BUTTON_CLICKED,
        V_PICKUP_REQUEST_BUTTON_CLICKED,
        V_TAKE_PICTURE_BUTTON_CLICKED,
        V_CREATED_VOTE_UP_BUTTON_CLICKED,
        V_CREATED_VOTE_DOWN_BUTTON_CLICKED,
        V_CLOSED_VOTE_UP_BUTTON_CLICKED,
        V_CLOSED_VOTE_DOWN_BUTTON_CLICKED,

        C_AUTHENTICATION_INITIATED,
        C_AUTHENTICATION_COMPLETED,

        M_USER_DATA_UPDATE,
        M_REQUEST_DATA_UPDATE,
    }

    /**
     * This array is required in order to use MessageType enum in switch-case comparisons.
     */
    public static final MessageType[] MESSAGE_TYPE_VALUES = MessageType.values();







}
