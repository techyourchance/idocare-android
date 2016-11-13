package il.co.idocare.managers;

import android.support.annotation.NonNull;

import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.entities.cachers.UserActionCacher;
import il.co.idocare.entities.useractions.UserActionEntity;
import il.co.idocare.entities.useractions.VoteForRequestUserActionEntity;
import il.co.idocare.multithreading.BackgroundThreadPoster;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.utils.Logger;

/**
 * This manager encapsulates functionality related to "requests"
 */
public class RequestsManager {

    private static final String TAG = "RequestsManager";

    public static final int VOTE_UP_CREATED = VoteForRequestUserActionEntity.VOTE_UP_CREATED;
    public static final int VOTE_DOWN_CREATED = VoteForRequestUserActionEntity.VOTE_DOWN_CREATED;
    public static final int VOTE_UP_CLOSED = VoteForRequestUserActionEntity.VOTE_UP_CLOSED;
    public static final int VOTE_DOWN_CLOSED = VoteForRequestUserActionEntity.VOTE_DOWN_CLOSED;

    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final LoginStateManager mLoginStateManager;
    private final UserActionCacher mUserActionCacher;
    private final Logger mLogger;
    private final ServerSyncController mServerSyncController;

    public RequestsManager(@NonNull BackgroundThreadPoster backgroundThreadPoster,
                           @NonNull LoginStateManager loginStateManager,
                           @NonNull UserActionCacher userActionCacher,
                           @NonNull Logger logger,
                           @NonNull ServerSyncController serverSyncController) {
        mBackgroundThreadPoster = backgroundThreadPoster;
        mLoginStateManager = loginStateManager;
        mUserActionCacher = userActionCacher;
        mLogger = logger;
        mServerSyncController = serverSyncController;
    }

    /**
     * Vote for request
     * @param requestId ID of the request to vote for
     * @param voteType either one of: {@link #VOTE_UP_CREATED}, {@link #VOTE_DOWN_CREATED},
     *                 {@link #VOTE_UP_CLOSED}, {@link #VOTE_DOWN_CLOSED}
     */
    public void voteForRequest(final long requestId, final int voteType) {

        mLogger.d(TAG, "voteForRequest(); request ID: " + requestId + "; vote type: " + voteType);

        final String activeUserId = mLoginStateManager.getActiveAccountUserId();

        if (activeUserId == null || activeUserId.isEmpty()) {
            mLogger.e(TAG, "no logged in user - vote ignored");
            return;
        }

        final UserActionEntity userActionEntity = new VoteForRequestUserActionEntity(
                System.currentTimeMillis(),
                requestId,
                voteType
        );

        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                mUserActionCacher.cacheUserAction(userActionEntity);
            }
        });
    }
}
