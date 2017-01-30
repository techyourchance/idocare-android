package il.co.idocare.requests;

import android.support.annotation.NonNull;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import il.co.idocare.common.BaseManager;
import il.co.idocare.networking.ManualSyncCompletedEvent;
import il.co.idocare.utils.eventbusregistrator.EventBusRegistrable;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import il.co.idocare.utils.multithreading.MainThreadPoster;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.requests.retrievers.RequestsRetriever;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.useractions.entities.VoteForRequestUserActionEntity;
import il.co.idocare.useractions.cachers.UserActionCacher;
import il.co.idocare.utils.Logger;

/**
 * This manager encapsulates functionality related to "requests"
 */
@EventBusRegistrable
public class RequestsManager extends BaseManager<RequestsManager.RequestsManagerListener> {

    private static final String TAG = "RequestsManager";


    public interface RequestsManagerListener {
        public void onRequestsFetched(@NonNull List<RequestEntity> requests);
    }

    public static final int VOTE_UP_CREATED = VoteForRequestUserActionEntity.VOTE_UP_CREATED;
    public static final int VOTE_DOWN_CREATED = VoteForRequestUserActionEntity.VOTE_DOWN_CREATED;
    public static final int VOTE_UP_CLOSED = VoteForRequestUserActionEntity.VOTE_UP_CLOSED;
    public static final int VOTE_DOWN_CLOSED = VoteForRequestUserActionEntity.VOTE_DOWN_CLOSED;

    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final MainThreadPoster mMainThreadPoster;
    private final UserActionCacher mUserActionCacher;
    private final RequestsRetriever mRequestsRetriever;
    private final Logger mLogger;
    private final ServerSyncController mServerSyncController;

    public RequestsManager(@NonNull BackgroundThreadPoster backgroundThreadPoster,
                           @NonNull MainThreadPoster mainThreadPoster,
                           @NonNull UserActionCacher userActionCacher,
                           @NonNull RequestsRetriever requestsRetriever,
                           @NonNull Logger logger,
                           @NonNull ServerSyncController serverSyncController) {
        mBackgroundThreadPoster = backgroundThreadPoster;
        mMainThreadPoster = mainThreadPoster;
        mUserActionCacher = userActionCacher;
        mRequestsRetriever = requestsRetriever;
        mLogger = logger;
        mServerSyncController = serverSyncController;
    }

    /**
     * Vote for request
     * @param requestId ID of the request to vote for
     * @param activeUserId ID of the current user
     * @param voteType either one of: {@link #VOTE_UP_CREATED}, {@link #VOTE_DOWN_CREATED},
     *                 {@link #VOTE_UP_CLOSED}, {@link #VOTE_DOWN_CLOSED}
     */
    public void voteForRequest(final String requestId, final String activeUserId, final int voteType) {

        mLogger.d(TAG, "voteForRequest(); request ID: " + requestId + "; vote type: " + voteType);


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

    public void fetchAllRequests() {
        mLogger.d(TAG, "fetchAllRequests() called");

        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                final List<RequestEntity> requests = mRequestsRetriever.getAllRequests();
                notifyListenersWithRequests(requests);
                mServerSyncController.requestImmediateSync();
            }
        });
    }

    public void fetchRequestsAssignedToUser(final String userId) {
        mLogger.d(TAG, "getRequestsAssignedToUser() called; user ID: " + userId);

        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                final List<RequestEntity> requests = mRequestsRetriever.getRequestsAssignedToUser(userId);
                notifyListenersWithRequests(requests);
                mServerSyncController.requestImmediateSync();
            }
        });

    }

    private void notifyListenersWithRequests(final List<RequestEntity> requests) {
        mMainThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                for (RequestsManagerListener listener : getListeners()) {
                    listener.onRequestsFetched(requests);
                }
            }
        });
    }



}
