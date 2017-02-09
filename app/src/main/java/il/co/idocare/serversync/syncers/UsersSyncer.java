package il.co.idocare.serversync.syncers;

import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.idocare.Constants;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.networking.newimplementation.ServerApi;
import il.co.idocare.networking.newimplementation.schemes.responses.GetUserResponseScheme;
import il.co.idocare.serversync.SyncFailedException;
import il.co.idocare.users.UsersRetriever;
import il.co.idocare.utils.Logger;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import retrofit2.Call;
import retrofit2.Response;

public class UsersSyncer {

    private static final String TAG = "UsersSyncer";

    private final UsersRetriever mUsersRetriever;
    private final LoginStateManager mLoginStateManager;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final ServerApi mServerApi;
    private final Logger mLogger;

    public UsersSyncer(UsersRetriever usersRetriever,
                       LoginStateManager loginStateManager,
                       BackgroundThreadPoster backgroundThreadPoster,
                       ServerApi serverApi,
                       Logger logger) {
        mUsersRetriever = usersRetriever;
        mLoginStateManager = loginStateManager;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mServerApi = serverApi;
        mLogger = logger;
    }

    @WorkerThread
    public void syncUsers() {
        mLogger.d(TAG, "syncUsers() called");

        List<String> uniqueUserIds =
                mUsersRetriever.getAllUniqueUsersIdsRelatedToRequests();

        String loggedInUserId = mLoginStateManager.getLoggedInUser().getUserId();

        // if user logged in and his ID is not in the list - add it
        if (loggedInUserId != null && !loggedInUserId.isEmpty()
                && !uniqueUserIds.contains(loggedInUserId)) {
            uniqueUserIds.add(loggedInUserId);
        }

        syncUsersByIds(uniqueUserIds);
    }

    private void syncUsersByIds(List<String> uniqueUserIds) {
        if (uniqueUserIds.isEmpty()) return;

        Call<Void> call = mServerApi.getUsersInfo(getUserIdsFieldMap(uniqueUserIds));

        try {
            Response<Void> response = call.execute();
            if (response.isSuccessful()) {
                // TODO: cache users
            } else {
                throw new SyncFailedException("get users call failed");
            }
        } catch (IOException e) {
            throw new SyncFailedException(e);
        }
    }

    private Map<String, String> getUserIdsFieldMap(List<String> uniqueUserIds) {
        Map<String, String> fieldMap = new HashMap<>(uniqueUserIds.size());

        for (int i = 0; i < uniqueUserIds.size(); i++) {
            fieldMap.put(Constants.FIELD_NAME_USER_ID + "[" + i + "]", uniqueUserIds.get(i));
        }

        return fieldMap;
    }


}
