package il.co.idocare.connectivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;

import ch.boye.httpclientandroidlib.client.utils.HttpClientUtils;
import il.co.idocare.Constants;
import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.utils.IDoCareHttpUtils;

/**
 * Our sync adapter
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter implements ServerRequest.OnServerResponseCallback {


    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();

    private AccountManager mAccountManager;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        String authToken = getAccountAuthToken(account);

        if (authToken == null) {
            Log.e(LOG_TAG, "Couldn't obtain auth token for the account" + account.name);
            return;
        }

        ServerRequest serverRequest = new ServerRequest(ServerRequest.GET_ALL_REQUESTS_URL,
                ServerRequest.ServerRequestTag.GET_ALL_REQUESTS, this);

        IDoCareHttpUtils.addStandardHeaders(this, serverRequest);


        serverRequest.execute();

    }


    @Override
    public void serverResponse(boolean responseStatusOk, ServerRequest.ServerRequestTag tag, String responseData) {

    }
}
