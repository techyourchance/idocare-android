package il.co.idocare.serversync.syncers;

import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.idocare.Constants;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.networking.ServerApi;
import il.co.idocare.networking.schemes.responses.GetUsersInfoResponseScheme;
import il.co.idocare.networking.schemes.responses.UserInfoScheme;
import il.co.idocare.serversync.SyncFailedException;
import il.co.idocare.users.UserEntity;
import il.co.idocare.users.UsersCacher;
import il.co.idocare.users.UsersRetriever;
import il.co.idocare.utils.Logger;
import retrofit2.Call;
import retrofit2.Response;

public class UsersSyncer {

    private static final String TAG = "UsersSyncer";

    private final UsersRetriever mUsersRetriever;
    private final UsersCacher mUsersCacher;
    private final LoginStateManager mLoginStateManager;
    private final ServerApi mServerApi;
    private final Logger mLogger;

    public UsersSyncer(UsersRetriever usersRetriever,
                       UsersCacher usersCacher,
                       LoginStateManager loginStateManager,
                       ServerApi serverApi,
                       Logger logger) {
        mUsersRetriever = usersRetriever;
        mUsersCacher = usersCacher;
        mLoginStateManager = loginStateManager;
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

        Call<GetUsersInfoResponseScheme> call = mServerApi.getUsersInfo(getUserIdsFieldMap(uniqueUserIds));

        try {
            Response<GetUsersInfoResponseScheme> response = call.execute();
            if (response.isSuccessful()) {
                processResponse(response.body().getUsersInfo());
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


    private void processResponse(List<UserInfoScheme> usersInfo) {
        if (usersInfo == null || usersInfo.isEmpty()) {
            mLogger.d(TAG, "empty users info list - deleting all users data");
            mUsersCacher.deleteAllUsers();
        } else {
            List<String> processedUserIds = updateOrInsertUsers(usersInfo);

            mUsersCacher.deleteAllUsersWithNonMatchingIds(processedUserIds);
        }
    }

    private List<String> updateOrInsertUsers(List<UserInfoScheme> usersInfo) {
        List<String> processedUsersIds = new ArrayList<>(usersInfo.size());

        for (UserInfoScheme userInfoScheme : usersInfo) {
            UserEntity user = convertSchemeToUser(userInfoScheme);
            mUsersCacher.updateOrInsertUserAndNotify(user);
            processedUsersIds.add(user.getUserId());
        }

        return processedUsersIds;
    }

    private UserEntity convertSchemeToUser(UserInfoScheme userInfoScheme) {
        return new UserEntity(
                userInfoScheme.getId(),
                userInfoScheme.getNickname(),
                userInfoScheme.getFirstName(),
                userInfoScheme.getLastName(),
                userInfoScheme.getReputation(),
                userInfoScheme.getPicture()
        );
    }

}
