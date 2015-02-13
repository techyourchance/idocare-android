package il.co.idocare.controllers.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import il.co.idocare.Constants;
import il.co.idocare.Constants.MessageType;
import il.co.idocare.IDoCareApplication;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.UtilMethods;
import il.co.idocare.views.LoginViewMVC;

public class FragmentLogin extends AbstractFragment implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "FragmentLogin";

    LoginViewMVC mViewMVCLogin;

    Bundle mLoginBundle;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewMVCLogin = new LoginViewMVC(inflater, container);
        // Provide inbox Handler to the MVC View
        mViewMVCLogin.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mViewMVCLogin.getInboxHandler());

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

        // TODO: complete this method
        switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {
            case V_LOGIN_BUTTON_CLICK:
                getRequestsFromServer();
                break;
            default:
                Log.w(LOG_TAG, "Message of type "
                        + Constants.MESSAGE_TYPE_VALUES[msg.what].toString() + " wasn't consumed");
        }

    }

    /**
     * Create a new server request asking to fetch all requests and set its credentials
     * // TODO: this should be removed in favor of complete auth mechanism
     */
    private void getRequestsFromServer() {

        mLoginBundle = mViewMVCLogin.getViewState();

        ServerRequest serverRequest = new ServerRequest(Constants.GET_ALL_REQUESTS_URL,
                Constants.ServerRequestTag.GET_ALL_REQUESTS, this);

        serverRequest.addTextField("username", mLoginBundle.getString("username"));
        serverRequest.addTextField("password", mLoginBundle.getString("password"));
        serverRequest.execute();

        notifyOutboxHandlers(MessageType.C_AUTHENTICATION_INITIATED.ordinal(), 0, 0, null);

    }


    // TODO: this should be removed in favor of complete auth mechanism
    @Override
    public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData) {
        if (tag == Constants.ServerRequestTag.GET_ALL_REQUESTS) {

            if (responseStatusOk) {
                // Set the obtained requests such that FragmentHome will be able to use them
                IDoCareApplication app = (IDoCareApplication) getActivity().getApplication();
                app.setRequests(UtilMethods.extractRequestsFromJSON(responseData));

                storeCredentials();

                // Show the action bar
                if (getActivity().getActionBar() != null) getActivity().getActionBar().show();

                // Switch to FragmentHome
                replaceFragment(FragmentHome.class, false, null);
            } else {
                Toast.makeText(getActivity(), "Incorrect username and/or password", Toast.LENGTH_LONG).show();

            }
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }

    private void storeCredentials() {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

        prefs.edit().putString("username", mLoginBundle.getString("username")).apply();
        prefs.edit().putString("password", mLoginBundle.getString("password")).apply();

        mLoginBundle = null;
    }

}
