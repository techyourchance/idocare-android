package il.co.idocare.controllers.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.connectivity.ServerRequest;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.utils.IDoCareJSONUtils;
import il.co.idocare.views.AuthenticateViewMVC;

/**
 * Created by Vasiliy on 4/30/2015.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity implements ServerRequest.OnServerResponseCallback {

    public final static String ARG_ACCOUNT_NAME = "arg_account_name";
    public final static String ARG_AUTH_TOKEN_TYPE = "arg_auth_type";
    public static final String ARG_ACCOUNT_TYPE = "arg_account_type";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "arg_is_adding_new_account";

    private static final String LOG_TAG = AuthenticatorActivity.class.getSimpleName();

    private static final String KEY_ERROR_MSG = "key_error_msg";

    private AuthenticateViewMVC mViewMVC;

    private final List<Handler> mOutboxHandlers = new ArrayList<Handler>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewMVC = new AuthenticateViewMVC(LayoutInflater.from(this), null);

        mViewMVC.addOutboxHandler(getInboxHandler());
        addOutboxHandler(mViewMVC.getInboxHandler());

        setContentView(mViewMVC.getRootView());

//        if (accountName != null) {
//            ((TextView)findViewById(R.id.accountName)).setText(accountName);
//        }

    }


    /**
     * Initiate login server request
     */
    private void sendLoginRequest() {

        Bundle userDataBundle = mViewMVC.getViewState();

        ServerRequest serverRequest = new ServerRequest(ServerRequest.LOGIN_URL,
                ServerRequest.ServerRequestTag.LOGIN, this);

        byte[] usernameBytes;
        byte[] passwordBytes;
        try {
            usernameBytes = ("fuckyouhackers" + userDataBundle.getString(AuthenticateViewMVC.VIEW_STATE_USERNAME)).getBytes("UTF-8");
            passwordBytes = ("fuckyouhackers" + userDataBundle.getString(AuthenticateViewMVC.VIEW_STATE_PASSWORD)).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e ) {
            // Really? Not supporting UTF-8???
            return;
        }

        serverRequest.addHeader(Constants.HttpHeader.USER_USERNAME.getValue(),
                Base64.encodeToString(usernameBytes, Base64.NO_WRAP));

        serverRequest.addTextField(Constants.FieldName.USER_PASSWORD.getValue(),
                Base64.encodeToString(passwordBytes, Base64.NO_WRAP));


        serverRequest.execute();

        notifyOutboxHandlers(Constants.MessageType.C_LOGIN_REQUEST_SENT.ordinal(), 0, 0, null);

    }


    @Override
    public void serverResponse(boolean responseStatusOk, ServerRequest.ServerRequestTag tag, String responseData) {
        if (tag == ServerRequest.ServerRequestTag.LOGIN) {

            notifyOutboxHandlers(Constants.MessageType.C_LOGIN_RESPONSE_RECEIVED.ordinal(), 0, 0, null);

            Bundle data = null;

            if (responseStatusOk) {
                data = extractResponseData(responseData);
            } else {
                data = new Bundle();
                data.putString(KEY_ERROR_MSG, "Unsuccessful server response. Response data: " + responseData);
            }

            finishLogin(data);
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }


    private void finishLogin(Bundle data) {

        AccountManager accountManager = AccountManager.get(this);

        if (data.containsKey(KEY_ERROR_MSG)) {
            Toast.makeText(this, "Incorrect username and/or password", Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG, "Incorrect username and/or password:  " + data.getString(KEY_ERROR_MSG));
            return;
        }

        String accountName = data.getString(AccountManager.KEY_ACCOUNT_NAME);
        String accountType = data.getString(AccountManager.KEY_ACCOUNT_TYPE);
        String authToken = data.getString(AccountManager.KEY_AUTHTOKEN);
        String authTokenType = getIntent().getStringExtra(ARG_AUTH_TOKEN_TYPE);
        if (authTokenType == null) authTokenType = AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT;



        final Account account = new Account(accountName, accountType);

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            accountManager.addAccountExplicitly(account, null, null);
            accountManager.setAuthToken(account, authTokenType, authToken);
        } else {
            accountManager.setAuthToken(account, authTokenType, authToken);
        }

        // Remember the account as a default account for the app
        SharedPreferences prefs =
                this.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        prefs.edit().putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
        prefs.edit().putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);


        final Intent result = new Intent();
        result.putExtras(data);

        setAccountAuthenticatorResult(data);
        setResult(RESULT_OK, result);
        finish();
    }



    private Bundle extractResponseData(String jsonData) {
        Bundle data = new Bundle();

        try {

            if (!IDoCareJSONUtils.verifySuccessfulStatus(jsonData)) {
                data.putString(KEY_ERROR_MSG, "Authentication failed");
                return data;
            }

            JSONObject dataObj = IDoCareJSONUtils.extractDataJSONObject(jsonData);
            long userId = dataObj.getLong(Constants.FieldName.USER_ID.getValue());
            String authToken = dataObj.getString(Constants.FieldName.USER_AUTH_TOKEN.getValue());

            data.putString(AccountManager.KEY_ACCOUNT_NAME, String.valueOf(userId));
            data.putString(AccountManager.KEY_ACCOUNT_TYPE, getIntent().getStringExtra(ARG_ACCOUNT_TYPE));
            data.putString(AccountManager.KEY_AUTHTOKEN, authToken);

        } catch (JSONException e) {
            e.printStackTrace();
            data.putString(KEY_ERROR_MSG, "Couldn't parse server response");
        }

        return data;
    }

    private Handler getInboxHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {
                    case V_LOGIN_BUTTON_CLICK:
                        sendLoginRequest();
                        break;
                    default:
                        Log.w(LOG_TAG, "Message of type "
                                + Constants.MESSAGE_TYPE_VALUES[msg.what].toString() + " wasn't consumed");
                }
            }
        };
    }


    private void addOutboxHandler(Handler handler) {
        // Not sure that there will be use case that requires sync, but just as precaution...
        synchronized (mOutboxHandlers) {
            if (!mOutboxHandlers.contains(handler)) {
                mOutboxHandlers.add(handler);
            }
        }
    }


    private void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj) {
        // Not sure that there will be use case that requires sync, but just as precaution...
        synchronized (mOutboxHandlers) {
            for (Handler handler : mOutboxHandlers) {
                Message msg = Message.obtain(handler, what, arg1, arg2, obj);
                msg.sendToTarget();
            }
        }
    }

}
