package il.co.idocare;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class Constants {


    /**
     * This enum is used in order to relate HTTP response to a particular HTTP request
     */
    public enum ServerRequestTag {GET_ALL_REQUESTS, REQUEST_DETAILS, NEW_REQUEST,
        PICKUP_REQUEST, CLOSE_REQUEST}

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



    // URLs used to issue requests to the server
    public final static String IMGLIST_URL = "http://dev-04.idocare.co.il/api-04/imglist";
    public final static String IMGTEST_URL = "http://dev-04.idocare.co.il/api-04/imgtest";
    public final static String GET_ALL_REQUESTS_URL = "http://dev-04.idocare.co.il/api-04/request";
    public final static String ADD_REQUEST_URL = "http://dev-04.idocare.co.il/api-04/request/add";
    public final static String PICKUP_REQUEST_URL = "http://dev-04.idocare.co.il/api-04/request/pickup";
    public final static String CLOSE_REQUEST_URL = "http://dev-04.idocare.co.il/api-04/request/close";


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

        C_AUTHENTICATION_INITIATED,
        C_AUTHENTICATION_COMPLETED,
    }

    /**
     * This array is required in order to use MessageType enum in switch-case comparisons.
     */
    public static final MessageType[] MESSAGE_TYPE_VALUES = MessageType.values();







}
