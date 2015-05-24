package il.co.idocare.connectivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.utils.IDoCareHttpUtils;
import il.co.idocare.utils.IDoCareJSONUtils;

/**
 * Our sync adapter
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {


    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();

    private ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        Log.d(LOG_TAG, "onPerformSync() called");

        String authToken = getAuthToken(account);
        if (authToken == null) {
            Log.e(LOG_TAG, "Couldn't obtain auth token for the account" + account.name);
            return;
        }

        ServerRequest serverRequest = new ServerRequest(ServerRequest.GET_ALL_REQUESTS_URL);

        IDoCareHttpUtils.addStandardHeaders(serverRequest, account.name, authToken);


        try {
            serverRequest.blockingExecute();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String responseData = null;
        try {
            responseData = serverRequest.getResponseData();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(responseData)) {
            Log.e(LOG_TAG, "got an empty response data or no response at all");
            return;
        }


        if (!IDoCareJSONUtils.verifySuccessfulStatus(responseData)) {
            Log.e(LOG_TAG, "server response with unsuccessful status:\n" + responseData);
            return;
        }


        // TODO: decide how to handle JSON parsing exceptions. Maybe rerun server request?

        JSONArray requestsArray = null;
        try {
            requestsArray = IDoCareJSONUtils.extractDataJSONArray(responseData);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        List<RequestItem> requestsList = null;
        try {
            requestsList = IDoCareJSONUtils.extractRequestItemsFromJSONArray(requestsArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }


        // TODO: replace the data in the DB
        int deleted = 0;
        try {
            deleted = provider.delete(IDoCareContract.Requests.CONTENT_URI, "ALL", null);
            Log.v(LOG_TAG, "deleted " + deleted + " entries from content provider");
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        for(RequestItem item : requestsList) {
            // TODO: optimize this loop with batch actions
            try {
                provider.insert(IDoCareContract.Requests.CONTENT_URI, item.toContentValues());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }


    }

    private String getAuthToken(Account account) {
        String authToken = null;
        try {
            authToken = AccountManager.get(this.getContext()).blockingGetAuthToken(
                    account,
                    AccountAuthenticator.AUTH_TOKEN_TYPE_DEFAULT,
                    true);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }

        return authToken;
    }

}
