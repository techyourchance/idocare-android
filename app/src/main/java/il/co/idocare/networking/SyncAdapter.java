package il.co.idocare.networking;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.location.OpenStreetMapsReverseGeocoderFactory;
import il.co.idocare.location.ReverseGeocoderFactory;
import il.co.idocare.networking.interfaces.ServerResponseHandlerFactory;

/**
 * Our sync adapter
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {


    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();


    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
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
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        Log.d(LOG_TAG, "onPerformSync() called");


        String authToken = getAuthToken(account);
        if (authToken == null) {
            Log.e(LOG_TAG, "Couldn't obtain auth token for the account" + account.name);
            // TODO: what do we do in that case? Need to use "open" APIs
            return;
        }

        DataUploader dataUploader =
                new DataUploader(account, authToken, provider);

        // This call will block until all local actions will be synchronized to the server
        // and the respective ContentProvider will be updated
        dataUploader.uploadAll();

        ReverseGeocoderFactory reverseGeocoderFactory =
                new OpenStreetMapsReverseGeocoderFactory();

        ServerResponseHandlerFactory serverResponseHandlerFactory =
                new SimpleServerResponseHandlerFactory(reverseGeocoderFactory);

        DataDownloader dataDownloader =
                new DataDownloader(account, authToken, provider, serverResponseHandlerFactory);

        // This call will block until all relevant data will be synchronized from the server
        // and the respective ContentProvider will be updated
        dataDownloader.downloadAll();


        Log.d(LOG_TAG, "onPerformSync() returned");

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
