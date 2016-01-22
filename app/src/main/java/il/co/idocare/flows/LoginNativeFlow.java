package il.co.idocare.flows;

import android.os.Bundle;

import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import il.co.idocare.Constants;
import il.co.idocare.URLs;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.networking.ServerHttpRequest;
import il.co.idocare.networking.responseparsers.ServerHttpResponseParser;
import il.co.idocare.networking.responseparsers.ServerResponseParsersFactory;
import il.co.idocare.utils.SecurityUtils;

/**
 * Created by Vasiliy on 1/22/2016.
 */
public class LoginNativeFlow extends AbstractFlow {

    private static final String TAG = "LoginNativeFlow";

    private final String mUsername;
    private final String mPassword;

    public LoginNativeFlow(String username, String password) {
        mUsername = username;
        mPassword = password;
    }

    @Override
    protected String getName() {
        return TAG;
    }

    @Override
    protected void doWork() {

        Bundle loginResult = new Bundle();

        String encodedUsername = SecurityUtils.encodeStringAsCredential(mUsername);
        String encodedPassword = SecurityUtils.encodeStringAsCredential(mPassword);

        ServerHttpRequest request = new ServerHttpRequest(URLs.getUrl(URLs.RESOURCE_LOGIN));

        // Add encoded header
        request.addHeader(Constants.HttpHeader.USER_USERNAME.getValue(), encodedUsername);

        // Add encoded parameter
        request.addTextField(Constants.FIELD_NAME_USER_PASSWORD_LOGIN, encodedPassword);

        // TODO: maybe too wasteful to build a new client for each flow?
        CloseableHttpResponse response = request.execute(HttpClientBuilder.create().build());

        if (response == null) {
            loginFailed(loginResult);
        }

        // Parse the response
        loginResult = LoginStateManager.handleResponse(response,
                ServerResponseParsersFactory.newInstance(URLs.RESOURCE_LOGIN));

        // Check for common errors
        LoginStateManager.checkForCommonErrors(loginResult);

        if (loginResult.containsKey(LoginStateManager.KEY_ERROR_MSG)) {
            loginFailed(loginResult);
        }

        // Account name should be added manually because the response does not contain this data
        // TODO: seems awkward that we need to store the account name and add it manually - try to resolve
        loginResult.putString(ServerHttpResponseParser.KEY_USERNAME, mUsername);

        addNativeAccount(loginResult);
        if (loginResult.containsKey(KEY_ERROR_MSG))
            loginFailed(loginResult);


        EventBus.getDefault().post(new UserLoggedInEvent());

    }

    private void loginFailed(Bundle result) {
        // TODO: complete
    }


}
