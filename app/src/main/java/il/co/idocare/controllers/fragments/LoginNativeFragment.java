package il.co.idocare.controllers.fragments;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.activities.MainActivity;
import il.co.idocare.networking.ServerHttpRequest;
import il.co.idocare.utils.IDoCareJSONUtils;
import il.co.idocare.views.LoginNativeViewMVC;

/**
 * This fragment handles native login flow
 */
public class LoginNativeFragment extends AbstractFragment implements
        ServerHttpRequest.OnServerResponseCallback {


    private static final String LOG_TAG = LoginNativeFragment.class.getSimpleName();

    private final static String LOGIN_URL = Constants.ROOT_URL + "/api-04/user/login";

    private static final String KEY_ERROR_MSG = "key_error_msg";


    private LoginNativeViewMVC mLoginNativeViewMVC;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLoginNativeViewMVC = new LoginNativeViewMVC(inflater, container);

        return mLoginNativeViewMVC.getRootView();
    }

    @Override
    public boolean isTopLevelFragment() {
        return false;
    }

    @Override
    public Class<? extends Fragment> getNavHierParentFragment() {
        return LoginChooserFragment.class;
    }

    @Override
    public String getTitle() {
        return "Log in";
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(mLoginNativeViewMVC);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(mLoginNativeViewMVC);
    }

    @Override
    public void onResume() {
        super.onResume();
        UserStateManager userStateManager = new UserStateManager(getActivity());
        if (userStateManager.isLoggedIn()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("IDoCare does not currently support multiple accounts")
                    .setCancelable(false)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((LoginActivity) getActivity()).finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    public void onEvent(LoginNativeViewMVC.LoginButtonClickEvent event) {
        sendLoginRequest();
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



    /**
     * Initiate login server request
     */
    private void sendLoginRequest() {

        Bundle userDataBundle = mLoginNativeViewMVC.getViewState();

        byte[] usernameBytes;
        byte[] passwordBytes;
        try {
            usernameBytes = ("fuckyouhackers" + userDataBundle.getString(LoginNativeViewMVC.VIEW_STATE_USERNAME)).getBytes("UTF-8");
            passwordBytes = ("fuckyouhackers" + userDataBundle.getString(LoginNativeViewMVC.VIEW_STATE_PASSWORD)).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e ) {
            // Really? Not supporting UTF-8???
            return;
        }


        ServerHttpRequest serverRequest = new ServerHttpRequest(LOGIN_URL, null, null, this, LOGIN_URL);

        serverRequest.addHeader(Constants.HttpHeader.USER_USERNAME.getValue(),
                Base64.encodeToString(usernameBytes, Base64.NO_WRAP));

        serverRequest.addTextField(Constants.FIELD_NAME_USER_PASSWORD_LOGIN,
                Base64.encodeToString(passwordBytes, Base64.NO_WRAP));


        new Thread(serverRequest).start();

        EventBus.getDefault().post(new LoginNativeViewMVC.LoginRequestSentEvent());

    }



    @Override
    public void serverResponse(int statusCode, String reasonPhrase, String entityString,
                               Object asyncCompletionToken) {
        String url = (String) asyncCompletionToken;
        if (url.equals(LOGIN_URL)) {

            Bundle data = null;

            // TODO: use a responsehandler here just like in DataUploader/Downloader classes!

            if (statusCode / 100 == 2) {
                data = extractResponseData(entityString);
            } else {
                data = new Bundle();
                data.putString(KEY_ERROR_MSG, "Unsuccessful server response.\n" + "" +
                        "Status code: " + statusCode + "\n" +
                        "Reason phrase: " + reasonPhrase + "\n" +
                        "Response data: " + entityString);
            }

            finishLogin(data);

        } else {
            Log.e(LOG_TAG, "receiver serverResponse() callback for unrecognized URL: " + url);
        }

    }


    private void finishLogin(Bundle data) {


        if (data.containsKey(KEY_ERROR_MSG)) {
            getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_LONG).show();
                        }
                    }
            );

            Log.i(LOG_TAG, "Login failed. Error message:\n" +
                    data.get(KEY_ERROR_MSG));
            EventBus.getDefault().post(new LoginNativeViewMVC.LoginFailedEvent());
            return;
        }

        String accountName = data.getString(AccountManager.KEY_ACCOUNT_NAME);
        String accountType = data.getString(AccountManager.KEY_ACCOUNT_TYPE);
        String authToken = data.getString(AccountManager.KEY_AUTHTOKEN);


        UserStateManager userStateManager = new UserStateManager(getActivity());

        if (userStateManager.addNativeAccount(accountName, accountType, authToken)) {
            // Notify of successful login
            EventBus.getDefault().post(new LoginNativeViewMVC.LoginSuccessfulEvent());

        } else {
            EventBus.getDefault().post(new LoginNativeViewMVC.LoginFailedEvent());
            return;
        }

        final Intent result = new Intent();
        result.putExtras(data);

        // This code is crap, but no time to think of a better approach
        // TODO: find a solution without casting (or, at least, not at this stage)
        ((LoginActivity)getActivity()).setAccountAuthenticatorResult(data);
        ((LoginActivity)getActivity()).setResult(Activity.RESULT_OK, result);
        ((LoginActivity)getActivity()).finish();

        if (getArguments().containsKey(LoginActivity.ARG_LAUNCHED_FROM_STARTUP_ACTIVITY)) {
            // If the enclosing activity was launched from StartupActivity then switch to
            // MainActivity (otherwise rely on backstack)
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }



    private Bundle extractResponseData(String jsonData) {
        Bundle data = new Bundle();

        try {

            if (!IDoCareJSONUtils.verifySuccessfulStatus(jsonData)) {
                data.putString(KEY_ERROR_MSG, "Authentication failed");
                return data;
            }

            JSONObject dataObj = IDoCareJSONUtils.extractDataJSONObject(jsonData);
            long userId = dataObj.getLong(Constants.FIELD_NAME_USER_ID);
            String authToken = dataObj.getString(Constants.FIELD_NAME_USER_AUTH_TOKEN);

            data.putString(AccountManager.KEY_ACCOUNT_NAME, String.valueOf(userId));
            data.putString(AccountManager.KEY_ACCOUNT_TYPE,
                    getArguments().getString(LoginActivity.ARG_ACCOUNT_TYPE,
                            AccountAuthenticator.ACCOUNT_TYPE_DEFAULT));
            data.putString(AccountManager.KEY_AUTHTOKEN, authToken);

        } catch (JSONException e) {
            e.printStackTrace();
            data.putString(KEY_ERROR_MSG, "Couldn't parse server response");
        }

        return data;
    }
}
