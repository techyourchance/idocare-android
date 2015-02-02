package il.co.idocare.www.idocare;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class FragmentLogin extends Fragment implements ServerRequest.OnServerResponseCallback {

    private final static String LOG_TAG = "FragmentLogin";

    private boolean mFetchingFromServer = false;

    Button mBtnLogin;
    EditText mEdtUsername;
    EditText mEdtPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);


        mBtnLogin = (Button) view.findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtnLogin.setEnabled(false);
                getRequestsFromServer();
            }
        });

        mEdtUsername = (EditText) view.findViewById(R.id.edt_login_username);
        mEdtPassword = (EditText) view.findViewById(R.id.edt_login_password);

        return view;
    }


    /**
     * Create a new server request asking to fetch all requests and set its credentials
     * // TODO: this should be removed in favor of complete auth mechanism
     */
    private void getRequestsFromServer() {
        ServerRequest serverRequest = new ServerRequest(Constants.GET_ALL_REQUESTS_URL,
                Constants.ServerRequestTag.GET_ALL_REQUESTS, this);


        serverRequest.addTextField("username", mEdtUsername.getText().toString());
        serverRequest.addTextField("password", mEdtPassword.getText().toString());
        serverRequest.execute();

        // Disable UI components
        mEdtUsername.setTag(mEdtUsername.getKeyListener());
        mEdtUsername.setKeyListener(null);

        mEdtPassword.setTag(mEdtPassword.getKeyListener());
        mEdtPassword.setKeyListener(null);

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
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frame_contents, new FragmentHome());
                ft.commit();
            } else {
                Toast.makeText(getActivity(), "Incorrect username and/or password", Toast.LENGTH_LONG).show();

                mEdtUsername.setKeyListener((KeyListener) mEdtUsername.getTag());
                mEdtUsername.clearComposingText();
                mEdtPassword.setKeyListener((KeyListener) mEdtPassword.getTag());
                mEdtPassword.clearComposingText();
                mBtnLogin.setEnabled(true);
            }
        } else {
            Log.e(LOG_TAG, "serverResponse was called with unrecognized tag: " + tag.toString());
        }
    }

    private void storeCredentials() {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

        prefs.edit().putString("username", mEdtUsername.getText().toString()).apply();
        prefs.edit().putString("password", mEdtPassword.getText().toString()).apply();
    }
}
