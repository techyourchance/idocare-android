package il.co.idocare.controllers.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import il.co.idocare.Constants;
import il.co.idocare.Constants.FieldName;
import il.co.idocare.Constants.MessageType;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.IDoCareJSONUtils;
import il.co.idocare.views.LoginViewMVC;

public class LoginFragment extends AbstractFragment implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "LoginFragment";

    LoginViewMVC mViewMVCLogin;

    Bundle mLoginBundle;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewMVCLogin = new LoginViewMVC(inflater, container);
        // Provide inbox Handler to the MVC View
        mViewMVCLogin.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mViewMVCLogin.getInboxHandler());

        // Hide action bar
        if (getActivity().getActionBar() != null) getActivity().getActionBar().hide();

        return mViewMVCLogin.getRootView();
    }


    @Override
    public boolean isTopLevelFragment() {
        return true;
    }

    @Override
    public Class<? extends AbstractFragment> getNavHierParentFragment() {
        return null;
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {
            case V_LOGIN_BUTTON_CLICK:
                sendLoginRequest();
                break;
            default:
                Log.w(LOG_TAG, "Message of type "
                        + Constants.MESSAGE_TYPE_VALUES[msg.what].toString() + " wasn't consumed");
        }
    }

    /**
     * Initiate login server request
     */
    private void sendLoginRequest() {

        mLoginBundle = mViewMVCLogin.getViewState();

        ServerRequest serverRequest = new ServerRequest(Constants.LOGIN_URL,
                Constants.ServerRequestTag.LOGIN, this);

        byte[] usernameBytes;
        byte[] passwordBytes;
        try {
            usernameBytes = ("fuckyouhackers" + mLoginBundle.getString("username")).getBytes("UTF-8");
            passwordBytes = ("fuckyouhackers" + mLoginBundle.getString("password")).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e ) {
            // Really? Not supporting UTF-8???
            return;
        }

        serverRequest.addHeader(Constants.HttpHeader.USER_USERNAME.getValue(),
                Base64.encodeToString(usernameBytes, Base64.NO_WRAP));

        serverRequest.addTextField(FieldName.USER_PASSWORD.getValue(),
                Base64.encodeToString(passwordBytes, Base64.NO_WRAP));


        serverRequest.execute();

        notifyOutboxHandlers(MessageType.C_AUTHENTICATION_INITIATED.ordinal(), 0, 0, null);

    }


    @Override
    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.LOGIN) {
            if (responseStatusOk && processResponseAndStoreCredentials(responseData)) {
                // Show action bar
                if (getActivity().getActionBar() != null) getActivity().getActionBar().show();
                replaceFragment(HomeFragment.class, false, null);
            } else {
                notifyOutboxHandlers(MessageType.C_AUTHENTICATION_COMPLETED.ordinal(), 0, 0, null);
                Toast.makeText(getActivity(), "Incorrect username and/or password", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }

    private boolean processResponseAndStoreCredentials(String jsonData) {
        try {

            if (!IDoCareJSONUtils.verifySuccessfulStatus(jsonData)) {
                Log.i(LOG_TAG, "Unsuccessful login attempt");
                return false;
            }

            JSONObject dataObj = IDoCareJSONUtils.extractDataJSONObject(jsonData);
            long userId = dataObj.getLong(FieldName.USER_ID.getValue());
            String publicKey = dataObj.getString(FieldName.USER_PUBLIC_KEY.getValue());

            SharedPreferences prefs =
                    getActivity().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

            prefs.edit().putLong(FieldName.USER_ID.getValue(), userId).apply();
            prefs.edit().putString(FieldName.USER_PUBLIC_KEY.getValue(), publicKey).apply();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
            // TODO: handle this error...
        }
    }

}
