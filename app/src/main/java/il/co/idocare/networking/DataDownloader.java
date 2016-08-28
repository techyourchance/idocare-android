package il.co.idocare.networking;

import android.content.ContentProviderClient;
import android.database.Cursor;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import il.co.idocare.Constants;
import il.co.idocare.URLs;
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

    private String mActiveUserId;
    private String mAuthToken;
    private ContentProviderClient mProvider;
    private LegacyServerResponseHandlerFactory mServerResponseHandlerFactory;

    private ThreadPoolExecutor mExecutor;


    public DataDownloader(String userId, String authToken, ContentProviderClient provider,
                          LegacyServerResponseHandlerFactory serverResponseHandlerFactory) {
        mActiveUserId = userId;
        mAuthToken = authToken;
        mProvider = provider;
        mServerResponseHandlerFactory = serverResponseHandlerFactory;

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

        List<Long> uniqueUserIds = getUniqueUserIds();

        for (long userId : uniqueUserIds) {
            downloadUserData(String.valueOf(userId));
        }

        performCleanup(uniqueUserIds);

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

        LegacyServerHttpRequest serverRequest = new LegacyServerHttpRequest(
                URLs.getUrl(URLs.RESOURCE_ALL_REQUESTS_DATA),
                mActiveUserId, mAuthToken, this, URLs.getUrl(URLs.RESOURCE_ALL_REQUESTS_DATA));

        if (!TextUtils.isEmpty(mActiveUserId) && !TextUtils.isEmpty(mAuthToken))
            serverRequest.addStandardHeaders();

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



    public void downloadUserData(String userId) {
            LegacyServerHttpRequest serverRequest = createUserServerRequest(userId);
            mExecutor.execute(serverRequest);
    }

    private List<Long> getUniqueUserIds() {
        Cursor cursor = null;
        try {
            synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
                cursor = mProvider.query(IDoCareContract.UniqueUserIds.CONTENT_URI,
                        null, null, null, null);
            }

            List<Long> uniqueUserIds;
            long userId;
            if (cursor != null && cursor.moveToFirst()) {
                uniqueUserIds = new ArrayList<>(cursor.getCount());
                do {
                    userId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(IDoCareContract.UniqueUserIds.COL_USER_ID));

                    if (userId != 0) { // TODO: can we avoid 0 from being present here (maybe DEFAULT NULL for columns in DB?)
                        uniqueUserIds.add(userId);
                    }
                } while (cursor.moveToNext());
            } else {
                uniqueUserIds = new ArrayList<>(1);
            }

            // Make sure the currently logged in user is counted
            if (!TextUtils.isEmpty(mActiveUserId) && !uniqueUserIds.contains(mActiveUserId)) {
                uniqueUserIds.add(Long.valueOf(mActiveUserId));
            }

            return uniqueUserIds;
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        } finally {
            if (cursor != null) cursor.close();
        }
    }


    private void performCleanup(List<Long> uniqueUserIds) {

//        mExecutor.shutdown();
//        try {
//            mExecutor.awaitTermination(30, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // Create a list of unique user ids that will be used in delete statement
        String idsForQuery = getIdsForQuery(uniqueUserIds);

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

    private LegacyServerHttpRequest createUserServerRequest(String userId) {
        LegacyServerHttpRequest serverRequest = new LegacyServerHttpRequest(
                URLs.getUrl(URLs.RESOURCE_USERS_DATA),
                mActiveUserId, mAuthToken, this, URLs.getUrl(URLs.RESOURCE_USERS_DATA));
        serverRequest.addTextField(Constants.FIELD_NAME_USER_ID, userId);

        return serverRequest;
    }

}
