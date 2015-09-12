package il.co.idocare.networking.responsehandlers;

import android.os.Bundle;

import ch.boye.httpclientandroidlib.client.ResponseHandler;
import il.co.idocare.Constants;

/**
 * Created by Vasiliy on 9/12/2015.
 */
public interface ServerHttpResponseHandler extends ResponseHandler<Bundle> {

    public final static String KEY_RESPONSE_STATUS_CODE =
            "il.co.idocare.networking.responsehandlers.KEY_RESPONSE_STATUS_CODE";

    public final static String KEY_RESPONSE_REASON_PHRASE =
            "il.co.idocare.networking.responsehandlers.KEY_RESPONSE_REASON_PHRASE";

    public final static String KEY_RESPONSE_STATUS_OK =
            "il.co.idocare.networking.responsehandlers.KEY_RESPONSE_STATUS_OK";

    public final static String KEY_RESPONSE_ENTITY =
            "il.co.idocare.networking.responsehandlers.KEY_RESPONSE_ENTITY";


    public final static String KEY_INTERNAL_STATUS_SUCCESS =
            "il.co.idocare.networking.responsehandlers.KEY_INTERNAL_STATUS_SUCCESS";

    public final static String KEY_MESSAGE =
            "il.co.idocare.networking.responsehandlers.KEY_MESSAGE";

    public final static String KEY_USER_ID = Constants.FIELD_NAME_USER_ID;

    public final static String KEY_PUBLIC_KEY = Constants.FIELD_NAME_USER_PUBLIC_KEY;



    // ---------------------------------------------------------------------------------------------
    //
    // Errors
    
    public final static String KEY_ERROR_TYPE =
            "il.co.idocare.networking.responsehandlers.KEY_ERROR_TYPE";

    public final static String VALUE_JSON_PARSE_ERROR =
            "il.co.idocare.networking.responsehandlers.VALUE_JSON_PARSE_ERROR";
}
