package il.co.idocare.www.idocare;

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


    // TODO: remove hardcoded username and password
    public final static String USERNAME = "admin";
    public final static String PASSWORD = "123456";

    // URLs used to issue requests to the server
    public final static String IMGLIST_URL = "http://dev-04.idocare.co.il/api-04/imglist";
    public final static String IMGTEST_URL = "http://dev-04.idocare.co.il/api-04/imgtest";
    public final static String ADD_REQUEST_URL = "http://dev-04.idocare.co.il/api-04/request/add";
    public final static String GET_ALL_REQUESTS_URL = "http://dev-04.idocare.co.il/api-04/request";


    // Names of fields of JSON objects returned in server responses
    // TODO: maybe this shouldn't be placed in Constants?
    public final static String PICTURES_HTTP_FIELD_NAME = "imagesBefore";







}
