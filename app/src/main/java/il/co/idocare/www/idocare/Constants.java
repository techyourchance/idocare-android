package il.co.idocare.www.idocare;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class Constants {


    /**
     * This enum is used in order to relate HTTP response to a particular HTTP request
     */
    public enum ServerRequestTag {REQUEST_DETAILS, NEW_REQUEST, GET_ALL_REQUESTS}

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
    public final static String ADD_REQUEST_URL = "http://dev-04.idocare.co.il/api-04/request/add";
    public final static String GET_ALL_REQUESTS_URL = "http://dev-04.idocare.co.il/api-04/request";









}
