package il.co.idocare.networking;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.database.Cursor;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import il.co.idocare.Constants;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.networking.interfaces.LegacyServerResponseHandler;
import il.co.idocare.networking.interfaces.LegacyServerResponseHandlerFactory;

/**
 * This class downloads data from the server and updates the local application's cache
 */
public class DataDownloader implements LegacyServerHttpRequest.OnServerResponseCallback {



    private static final String LOG_TAG = DataDownloader.class.getSimpleName();


    /*
     * This object will be used to synchronize access to ContentProviderClient. We don't sync on
     * the provider itself because it is originally passed to SyncAdapter.onPerformSync() and is
     * managed externally, therefore we can't know if it is safe to sync on it (well, the current
     * source code suggests that it is safe, but this can change in future releases of AOSP,
     * therefore we don't take chances).
     */
    private final Object CONTENT_PROVIDER_CLIENT_LOCK = new Object();

    private static final int SERVER_REQUEST_TIMEOUT_MILLIS = 60000;

    private Account mAccount;
    private String mAuthToken;
    private ContentProviderClient mProvider;
    private LegacyServerResponseHandlerFactory mServerResponseHandlerFactory;

    private ThreadPoolExecutor mExecutor;

    private List<Long> mUniqueUserIds;

    public DataDownloader(Account account, String authToken, ContentProviderClient provider,
                          LegacyServerResponseHandlerFactory serverResponseHandlerFactory) {
        mAccount = account;
        mAuthToken = authToken;
        mProvider = provider;
        mServerResponseHandlerFactory = serverResponseHandlerFactory;

        mUniqueUserIds = new ArrayList<>();

        int numOfCores = Runtime.getRuntime().availableProcessors();
        // TODO: consider using bound queue for executor - will be easier to debug and more efficient because the commands are stored in dispatcher
        mExecutor = new ThreadPoolExecutor(
                numOfCores+1,
                numOfCores+1,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );

    }

    public void downloadAll() {

        downloadRequestsData();

        downloadUsersData();

        performCleanup();

    }

    @Override
    public void serverResponse(int statusCode, String reasonPhrase, String entityString,
                               Object asyncCompletionToken) {

        String url = (String) asyncCompletionToken;

        LegacyServerResponseHandler responseHandler = mServerResponseHandlerFactory.newInstance(url);

        if (responseHandler != null) {
            try {
                synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
                    responseHandler.handleResponse(statusCode, reasonPhrase, entityString, mProvider);
                }
            } catch (LegacyServerResponseHandler.ServerResponseHandlerException e) {
                e.printStackTrace();
            }
        }

    }



    private void downloadRequestsData() {

        LegacyServerHttpRequest serverRequest = new LegacyServerHttpRequest(Constants.GET_ALL_REQUESTS_URL,
                mAccount, mAuthToken, this, Constants.GET_ALL_REQUESTS_URL);

        if (mAccount != null) serverRequest.addStandardHeaders();

        Thread workerThread = new Thread(serverRequest);
        workerThread.start();
        try {
            // Wait for the worker thread to complete
            // TODO: test what happens if the timeout is reached - does this code fail gracefully?
            workerThread.join(SERVER_REQUEST_TIMEOUT_MILLIS);
            if (workerThread.isAlive()) {
                workerThread.interrupt();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private void downloadUsersData() {

        mUniqueUserIds.clear();

        Cursor cursor = null;
        try {
            synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
                cursor = mProvider.query(IDoCareContract.UniqueUserIds.CONTENT_URI,
                        null, null, null, null);
            }

            long userId;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    userId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(IDoCareContract.UniqueUserIds.COL_USER_ID));

                    if (userId != 0) { // TODO: can we avoid 0 from being present here (maybe DEFAULT NULL for columns in DB?)
                        mUniqueUserIds.add(userId);

                        LegacyServerHttpRequest serverRequest = createUserServerRequest(userId);
                        mExecutor.execute(serverRequest);
                    }
                } while (cursor.moveToNext());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void performCleanup() {

        // Create a list of unique user ids that will be used in delete statement
        String idsForQuery = getIdsForQuery(mUniqueUserIds);

        // Delete users that do not appear in the list of unique referenced users
        try {
            mProvider.delete(
                    IDoCareContract.Users.CONTENT_URI,
                    IDoCareContract.Users.COL_USER_ID +
                            " NOT IN ( " + idsForQuery + " )",
                    null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private String getIdsForQuery(List<Long> idsList) {
        StringBuilder idsForQuery = new StringBuilder();
        boolean first = true;
        for (long id : idsList) {
            if (first) {
                first = false;
                idsForQuery.append("'").append(String.valueOf(id)).append("'");
            } else {
                idsForQuery.append(",'").append(String.valueOf(id)).append("'");
            }
        }
        return idsForQuery.toString();
    }

    private LegacyServerHttpRequest createUserServerRequest(long userId) {
        LegacyServerHttpRequest serverRequest = new LegacyServerHttpRequest(Constants.GET_NATIVE_USER_DATA_URL,
                mAccount, mAuthToken, this, Constants.GET_NATIVE_USER_DATA_URL);
        serverRequest.addTextField(Constants.FIELD_NAME_USER_ID, String.valueOf(userId));

        return serverRequest;
    }
}
