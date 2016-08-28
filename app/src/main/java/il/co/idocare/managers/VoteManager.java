package il.co.idocare.managers;

import android.support.annotation.NonNull;

import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.entities.UserActionEntity;
import il.co.idocare.entities.cachers.UserActionCacher;
import il.co.idocare.multithreading.BackgroundThreadPoster;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.utils.Logger;

/**
 * This manager encapsulates functionality related to "vote" action
 */
public class VoteManager {

    private static final String TAG = "VoteManager";

    public static final int VOTE_UP = 1;
    public static final int VOTE_DOWN = 2;

    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final LoginStateManager mLoginStateManager;
    private final UserActionCacher mUserActionCacher;
    private final Logger mLogger;
    private final ServerSyncController mServerSyncController;

    public VoteManager(@NonNull BackgroundThreadPoster backgroundThreadPoster,
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

    public void voteForRequest(final long requestId, final int voteType, final boolean voteForClosed) {

        mLogger.d(TAG, "voteForRequest(); request ID: " + requestId + "; vote type: " + voteType
                + "; vote for closed: " + voteForClosed);

        final String voteActionParam;
        switch (voteType) {
            case VOTE_UP:
                voteActionParam = "1";
                break;
            case VOTE_DOWN:
                voteActionParam = "-1";
                break;
            default:
                throw new IllegalArgumentException("vote type must be either VOTE_UP or VOTE_DOWN");
        }

        final String activeUserId = mLoginStateManager.getActiveAccountUserId();

        if (activeUserId == null || activeUserId.isEmpty()) {
            mLogger.e(TAG, "no logged in user - vote ignored");
            return;
        }

        final UserActionEntity userActionEntity = new UserActionEntity(
                System.currentTimeMillis(),
                IDoCareContract.UserActions.ENTITY_TYPE_REQUEST,
                requestId,
                voteForClosed ?
                        IDoCareContract.UserActions.ENTITY_PARAM_REQUEST_CLOSED :
                        IDoCareContract.UserActions.ENTITY_PARAM_REQUEST_CREATED,
                IDoCareContract.UserActions.ACTION_TYPE_VOTE_FOR_REQUEST,
                voteActionParam
        );

        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                if (mUserActionCacher.cacheUserAction(userActionEntity)) {
                    mServerSyncController.syncUserDataImmediate(activeUserId);
                }
            }
        });
    }
}
