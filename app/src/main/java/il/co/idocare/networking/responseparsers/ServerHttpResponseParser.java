package il.co.idocare.networking.responseparsers;

import android.os.Bundle;

import ch.boye.httpclientandroidlib.HttpResponse;
import il.co.idocare.Constants;

/**
 * The general interface for classes that will be used to parse HTTP responses from the server
 */
public interface ServerHttpResponseParser {

    /*
    Explanation of implementation of Decorator design pattern in ServerResponseParsers:

    ServerResponseParsers are not independent - it doesn't make sense to try and
    parse the data returned in the response independently of the HTTP status of that particular
    response. Due to this dependency, there is really no need for JsonParser to be able
    to function as a standalone parser - this parser only makes sense as a decorator to the more
    general parser which parses the more "high level" HTTP attributes.
     */


    public Bundle parseResponse(HttpResponse httpResponse) throws HttpResponseParseException;

    // ---------------------------------------------------------------------------------------------
    //
    // General keys used by all (or, at least, majority of) ResponseParsers

    /**
     * This key corresponds to the HTTP status code returned with the response
     */
    public final static String KEY_RESPONSE_STATUS_CODE =
            "il.co.idocare.networking.responseparsers.KEY_RESPONSE_STATUS_CODE";

    /**
     * This key corresponds to the HTTP reason phrase returned with the response
     */
    public final static String KEY_RESPONSE_REASON_PHRASE =
            "il.co.idocare.networking.responseparsers.KEY_RESPONSE_REASON_PHRASE";

    /**
     * This key will be set if the parsed response has a status code of OK
     */
    public final static String KEY_RESPONSE_STATUS_OK =
            "il.co.idocare.networking.responseparsers.KEY_RESPONSE_STATUS_OK";

    /**
     * This key corresponds to the entity of the response (if any is present)
     */
    public final static String KEY_RESPONSE_ENTITY =
            "il.co.idocare.networking.responseparsers.KEY_RESPONSE_ENTITY";



    /**
     * JSON objects returned by the server have the following structure:<br>
     * <pre>
     * {"status":"status string","message":"message string","data":&lt; element|array|object &gt;}
     * </pre>
     * This key corresponds to the message string.
     */
    public final static String KEY_INTERNAL_MESSAGE =
            "il.co.idocare.networking.responseparsers.KEY_INTERNAL_MESSAGE";

    /**
     * JSON objects returned by the server have the following structure:<br>
     * <pre>
     * {"status":"status string","message":"message string","data":&lt; element|array|object &gt;}
     * </pre>
     * This key corresponds to the status string.
     */
    public final static String KEY_INTERNAL_STATUS =
            "il.co.idocare.networking.responseparsers.KEY_INTERNAL_STATUS";

    /**
     * JSON objects returned by the server have the following structure:<br>
     * <pre>
     * {"status":"status string","message":"message string","data":&lt; element|array|object &gt;}
     * </pre>
     * This key will be set if the returned JSON object hav a successful status string
     */
    public final static String KEY_INTERNAL_STATUS_SUCCESS =
            "il.co.idocare.networking.responseparsers.KEY_INTERNAL_STATUS_SUCCESS";

    /**
     * JSON objects returned by the server have the following structure:<br>
     * <pre>
     * {"status":"status string","message":"message string","data":&lt; element|array|object &gt;}
     * </pre>
     * This key corresponds to the data string.
     */
    public final static String KEY_INTERNAL_DATA_JSON =
            "il.co.idocare.networking.responseparsers.KEY_INTERNAL_DATA_JSON";


    /**
     * This key will be set if there were any errors while handling a particular response. Since
     * there might me more than one error, this key will point to a String array containing all
     * the reported errors.
     */
    public final static String KEY_ERRORS =
            "il.co.idocare.networking.responseparsers.KEY_ERRORS";




    // ---------------------------------------------------------------------------------------------
    //
    // Keys and values specific to particular parsers or groups of parsers
    //
    // Note that these are defined in the general interface (as opposed to the specific parsers
    // that set them) because we would want to use a Decorator design pattern for parsers and
    // hide the details of which parsers were used from the code which uses these parsers.




    public final static String KEY_USER_ID = Constants.FIELD_NAME_USER_ID;

    public final static String KEY_PUBLIC_KEY = Constants.FIELD_NAME_USER_PUBLIC_KEY;

    // TODO: we would like to take this value from Constants, but there is no username there, just email. Decide what to do!
    public final static String KEY_USERNAME =
            "il.co.idocare.networking.responseparsers.KEY_USERNAME";



    // ---------------------------------------------------------------------------------------------
    //
    // Errors. These values can be written into String array referenced by KEY_ERRORS key

    // These values are used to group errors by type (allows for handling errors in groups)
    public static final int ERROR_VALUE_GROUP_OFFSET = 1000;
    public static final int VALUE_GENERAL_ERRORS_GROUP = ERROR_VALUE_GROUP_OFFSET * 0;
    public static final int VALUE_JSON_ERRORS_GROUP = ERROR_VALUE_GROUP_OFFSET * 1;

    // General errors
    public final static int VALUE_NO_ENTITY_IN_RESPONSE_ERROR = VALUE_GENERAL_ERRORS_GROUP + 1;

    // JSON formatting errors
    public final static int VALUE_JSON_PARSE_ERROR = VALUE_JSON_ERRORS_GROUP + 1;
    public final static int VALUE_JSON_NO_INTERNAL_STATUS= VALUE_JSON_ERRORS_GROUP + 2;
    public final static int VALUE_JSON_NO_INTERNAL_MESSAGE = VALUE_JSON_ERRORS_GROUP + 3;
    public final static int VALUE_JSON_NO_INTERNAL_DATA= VALUE_JSON_ERRORS_GROUP + 4;
}
