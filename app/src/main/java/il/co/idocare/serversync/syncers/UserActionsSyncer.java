package il.co.idocare.serversync.syncers;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.networking.newimplementation.ServerApi;
import il.co.idocare.requests.retrievers.TempIdRetriever;
import il.co.idocare.serversync.SyncFailedException;
import il.co.idocare.useractions.cachers.UserActionCacher;
import il.co.idocare.useractions.entities.PickUpRequestUserActionEntity;
import il.co.idocare.useractions.entities.UserActionEntity;
import il.co.idocare.useractions.retrievers.UserActionsRetriever;
import il.co.idocare.utils.Logger;
import il.co.idocare.utils.multithreading.BackgroundThreadPoster;
import retrofit2.Call;
import retrofit2.Response;

/**
 * This class handles synchronization of user actions to/from the server.<br>
 * NOTE: although methods in this class should be called from background threads, the
 * implementation is not thread-safe.
 */
public class UserActionsSyncer {

    private static final String TAG = "UserActionsSyncer";

    // TODO: implement full "event sourcing" and remove dependency on request syncer
    private final RequestsSyncer mRequestsSyncer;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UserActionsRetriever mUserActionsRetriever;
    private final UserActionCacher mUserActionCacher;
    private final TempIdRetriever mTempIdRetriever;
    private final ContentResolver mContentResolver;
    private final ServerApi mServerApi;
    private final Logger mLogger;

    private UserActionsDispatcher mDispatcher = new UserActionsDispatcher();

    public UserActionsSyncer(RequestsSyncer requestsSyncer,
                             BackgroundThreadPoster backgroundThreadPoster,
                             UserActionsRetriever userActionsRetriever,
                             UserActionCacher userActionCacher,
                             TempIdRetriever tempIdRetriever,
                             ContentResolver contentResolver,
                             ServerApi serverApi,
                             Logger logger) {
        mRequestsSyncer = requestsSyncer;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mUserActionsRetriever = userActionsRetriever;
        mUserActionCacher = userActionCacher;
        mTempIdRetriever = tempIdRetriever;
        mContentResolver = contentResolver;
        mServerApi = serverApi;
        mLogger = logger;
    }

    @WorkerThread
    public void syncUserActions() {
        mLogger.d(TAG, "syncUserActions()");

        mDispatcher.prepareUserActionsForDispatching(mUserActionsRetriever.getAllUserActions());

        UserActionEntity userAction;

        while (mDispatcher.hasMoreActionsToDispatch()) {

            // This call will block until dispatcher releases the next command
            userAction = mDispatcher.dispatchNextUserAction();

            if (userAction != null) {
                syncUserActionOnNewThread(userAction);
            }
        }

        mDispatcher.waitUntilAllActionsSynced();


        performCleanup();

    }

    private void syncUserActionOnNewThread(final UserActionEntity userAction) {
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                syncUserActionAndNotifyDispatcher(userAction);
            }
        });
    }

    private void syncUserActionAndNotifyDispatcher(UserActionEntity userAction) {
        mLogger.d(TAG, "syncUserActionAndNotifyDispatcher(); user action: " + userAction);

        String entityType = userAction.getEntityType();
        String actionType = userAction.getActionType();

        boolean userActionSyncedSuccessfully = true;

        try {
            switch (entityType) {
                case IDoCareContract.UserActions.ENTITY_TYPE_REQUEST:
                    switch (actionType) {
                        case IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST:
                            mRequestsSyncer.syncRequestCreated(userAction.getEntityId());
                            break;

                        case IDoCareContract.UserActions.ACTION_TYPE_PICKUP_REQUEST:
                            syncRequestPickedUpAction(PickUpRequestUserActionEntity.fromUserAction(userAction));
                            break;

//                    case IDoCareContract.UserActions.ACTION_TYPE_CLOSE_REQUEST:
//                        addCloseRequestSpecificInfo(serverHttpRequest, userAction);
//                        break;
//
//                    case IDoCareContract.UserActions.ACTION_TYPE_VOTE_FOR_REQUEST:
//                        addVoteSpecificInfo(serverHttpRequest, userAction);
//                        break;

                        default:
                            throw new RuntimeException("unsupported action type: " + actionType);
                    }
                    break;
                default:
                    throw new RuntimeException("unsupported entity type: " + entityType);
            }

            mUserActionCacher.deleteUserAction(userAction);

        } catch (SyncFailedException e) {
            e.printStackTrace();
            userActionSyncedSuccessfully = false;
        }

        mDispatcher.notifyUserActionSyncComplete(userAction, userActionSyncedSuccessfully);
    }

    private void syncRequestPickedUpAction(PickUpRequestUserActionEntity userAction) {
        Call<Void> call = mServerApi.pickupRequest(userAction.getEntityId());

        try {
            Response<Void> response = call.execute();
            if (!response.isSuccessful()) {
                throw new SyncFailedException("pickup request call failed; response code: " + response.code());
            }
        } catch (IOException e) {
            throw new SyncFailedException(e);
        }
    }

    /**
     * This method performs all the necessary cleanup when users' actions have been uploaded
     */
    private void performCleanup() {
        clearLocallyModifiedFlags();

        // TODO: we need these mappings to survive in order to be used to re-create loaders in fragments - try to find workaround!
        // clearTempIdMappings();

    }


    /**
     * This method clears LOCALLY_MODIFIED flags from requests if there are no more user actions
     * pending for them
     */
    private void clearLocallyModifiedFlags() {
        // TODO: ensure atomicity
        // TODO: remove dependency on ContentResolver

        List<String> completedEntitiesIds = mDispatcher.getCompletedEntityIds();

        // TODO: probably there is a better way of clearing the flags (maybe single SQL statement?)

        for (String entityId : completedEntitiesIds) {
            List<UserActionEntity> userActionsAffectingEntity =
                    mUserActionsRetriever.getUserActionsAffectingEntity(entityId);

            if (userActionsAffectingEntity.isEmpty()) {


                // No user actions for that ENTITY_ID - clear locally modified flag
                ContentValues contentValues = new ContentValues();
                contentValues.put(IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG, "0");
                mContentResolver.update(
                        IDoCareContract.Requests.CONTENT_URI,
                        contentValues,
                        IDoCareContract.Requests.COL_REQUEST_ID + " = ?",
                        new String[] {entityId}
                );
            }
        }
    }

    /**
     * This class helps in the ordering of the user's actions that need to be uploaded and handles
     * the dependencies that must be enforced (e.g. new entities must be synced to the server, and
     * only then other actions on these entities can be performed)...
     * TODO: convert to a regular class instead of inner
     */
    private class UserActionsDispatcher {

        private final Object DISPATCHER_LOCK = new Object();

        private Map<String, List<UserActionEntity>> mEntityIdToNonDispatchedUserActionsMap;

        // We need to keep track of dispatched actions until we get confirmation on success or
        // failure (in which case we might need to handle the error).
        private Map<String, List<UserActionEntity>> mEntityIdToDispatchedUserActionsMap;

        // This map contains "stall" indicators - when any of them is TRUE, the actions for the
        // respective entity should not be dispatched until the indicator is cleared
        private Map<String, AtomicBoolean> mEntityIdToStallFlagMap;

        private List<String> mCompletedEntityIds;

        private int mTotalUserActionsCount;
        private int mDispatchedUserActionsCount;
        private int mTotalAffectedEntitiesCount;

        public UserActionsDispatcher() {}

        public void prepareUserActionsForDispatching(List<UserActionEntity> userActions) {

            mTotalUserActionsCount = userActions.size();
            mDispatchedUserActionsCount = 0;

            mEntityIdToNonDispatchedUserActionsMap = mapEntityIdsToSortedUserActions(userActions);

            mTotalAffectedEntitiesCount = mEntityIdToNonDispatchedUserActionsMap.keySet().size();

            mEntityIdToDispatchedUserActionsMap = new HashMap<>(mTotalAffectedEntitiesCount);

            mEntityIdToStallFlagMap = new HashMap<>(mTotalAffectedEntitiesCount);

            for (String entityId : mEntityIdToNonDispatchedUserActionsMap.keySet()) {
                // Initialize the "stall" indicator for this entity
                mEntityIdToStallFlagMap.put(entityId, new AtomicBoolean(false));

                // Initialize the "dispatched" list for this entity
                mEntityIdToDispatchedUserActionsMap.put(
                        entityId,
                        new ArrayList<UserActionEntity>(mEntityIdToNonDispatchedUserActionsMap.get(entityId).size())
                );

            }

            mCompletedEntityIds = new ArrayList<>(mTotalAffectedEntitiesCount);
        }


        private Map<String, List<UserActionEntity>> mapEntityIdsToSortedUserActions(List<UserActionEntity> userActions) {

            Map<String, List<UserActionEntity>> resultMap = new HashMap<>();

            String entityId;

            // map entities to user actions that affect them
            for (UserActionEntity userAction : userActions) {
                entityId = userAction.getEntityId();
                if (!resultMap.containsKey(entityId)) {
                    resultMap.put(entityId, new ArrayList<UserActionEntity>(1));
                }
                resultMap.get(entityId).add(userAction);
            }

            // independently sort user actions for each entity
            for (String sortedEntityId : resultMap.keySet()) {
                Collections.sort(resultMap.get(sortedEntityId), new UserActionsComparator());
            }

            return resultMap;
        }


        public boolean hasMoreActionsToDispatch() {
            synchronized (DISPATCHER_LOCK) {
                return mDispatchedUserActionsCount < mTotalUserActionsCount;
            }
        }


        /**
         * Get the next user action that need to be uploaded to the server.<br>
         *
         * Call to this method will block if there are non-dispatched actions, but they are
         * waiting for some already dispatched actions to complete. Once any dispatched "stalling"
         * action completes, this method will unblock and return one of the released user actions.<br>
         *
         * In case this method returns null the clients might retry to call this method, but should
         * ensure that this dispatcher is not empty beforehand.
         * @return user action that should be uploaded to the server, or null if all actions
         *         have already been dispatched
         */
        public @Nullable UserActionEntity dispatchNextUserAction() {
            synchronized (DISPATCHER_LOCK) {
                while (hasMoreActionsToDispatch()) {

                    UserActionEntity userAction = removeNextNonStalledNonDispatchedUserAction();

                    if (userAction != null) {
                        String entityId = userAction.getEntityId();

                        if (isStallingAction(userAction)) {
                            stallActionsForEntity(entityId);
                        }

                        handleUserActionDispatched(userAction);

                        return userAction;
                    } else {
                        // if we got here it means that all non-dispatched actions are stalled
                        try {
                            DISPATCHER_LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        }

        private void stallActionsForEntity(String entityId) {
            mEntityIdToStallFlagMap.get(entityId).set(true); // set "stalled" flag
        }

        private void handleUserActionDispatched(UserActionEntity userAction) {
            String entityId = userAction.getEntityId();

            // Mark user's action as being dispatched
            mEntityIdToDispatchedUserActionsMap.get(entityId).add(userAction);

            mDispatchedUserActionsCount++;
        }

        private UserActionEntity removeNextNonStalledNonDispatchedUserAction() {
            for (String entityId : mEntityIdToNonDispatchedUserActionsMap.keySet()) {

                if (hasNonDispatchedActionsForEntity(entityId) && !isEntityActionsStalled(entityId)) {
                    // TODO: removing from 0 position is not efficient - reverse sort order and remove from end
                    return mEntityIdToNonDispatchedUserActionsMap.get(entityId).remove(0);
                }

            }
            return null; // all non-dispatched actions are stalled
        }

        private boolean isEntityActionsStalled(String entityId) {
            return mEntityIdToStallFlagMap.get(entityId).get();
        }

        private boolean hasNonDispatchedActionsForEntity(String entityId) {
            return mEntityIdToNonDispatchedUserActionsMap.get(entityId).size() > 0;
        }

        /**
         * Let the dispatcher know that one of the dispatched actions have been uploaded to the
         * server
         */
        public void notifyUserActionSyncComplete(UserActionEntity userAction, boolean isSuccess) {
            mLogger.d(TAG, "notifyUserActionSyncComplete(); user action: " + userAction
                    + "; is success: " + isSuccess);

            synchronized (DISPATCHER_LOCK) {

                String entityId = userAction.getEntityId();

                if (!mEntityIdToDispatchedUserActionsMap.get(entityId).contains(userAction)) {
                    throw new IllegalStateException("user action is not in the dispatched list");
                }

                // Remove the action from the dispatched list
                mEntityIdToDispatchedUserActionsMap.get(entityId).remove(userAction);

                if (isStallingAction(userAction)) {
                    // if the action was of "stalling" type - clear entity's stall flag
                    mEntityIdToStallFlagMap.get(entityId).set(false);
                    // there might be threads waiting for stalled action to complete - notify them
                    DISPATCHER_LOCK.notifyAll();
                }

                cleanIfNoMoreActionsForEntity(entityId);

                if (isSuccess) {
                    // There are user actions that their uploading changes IDs of entities (e.g. new requests
                    // get permanent IDs assigned by the server) - account for this change
                    ensureConsistentEntityIds(userAction);
                }
            }


        }


        public void waitUntilAllActionsSynced() {
            synchronized (DISPATCHER_LOCK) {
                while (mCompletedEntityIds.size() < mTotalAffectedEntitiesCount) {
                    try {
                        DISPATCHER_LOCK.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void cleanIfNoMoreActionsForEntity(String entityId) {
            synchronized (DISPATCHER_LOCK) {
                if (isNoMoreActionsForEntity(entityId)) {
                    handleEntitySyncCompleted(entityId);
                }
            }
        }

        private void handleEntitySyncCompleted(String entityId) {
            synchronized (DISPATCHER_LOCK) {
                mEntityIdToNonDispatchedUserActionsMap.remove(entityId);
                mEntityIdToDispatchedUserActionsMap.remove(entityId);
                mEntityIdToStallFlagMap.remove(entityId);

                mCompletedEntityIds.add(entityId);

                // there might be threads waiting for entities sync to complete
                DISPATCHER_LOCK.notifyAll();
            }

        }

        private boolean isNoMoreActionsForEntity(String entityId) {
            return mEntityIdToDispatchedUserActionsMap.get(entityId).isEmpty() &&
                    mEntityIdToNonDispatchedUserActionsMap.get(entityId).isEmpty();
        }

        /**
         * Some user actions, when uploaded to the server, change entities IDs (e.g. newly created
         * requests assigned permanent IDs during uploading). This method ensures that the internal
         * IDs managed by this dispatcher stay in sync with these changes
         */
        private void ensureConsistentEntityIds(UserActionEntity userAction) {
            synchronized (DISPATCHER_LOCK) {
                if (isCreateRequestUserAction(userAction)) {
                    // The uploaded action was of type "create request", which means that the ID of this
                    // request has changed...

                    String oldId = userAction.getEntityId();

                    // There should be mapping from old entity ID to the new entity ID
                    String newId = getPermanentEntityId(userAction.getEntityId());

                    // Now change IDs in all the data structures
                    changeIdsInMap(mEntityIdToNonDispatchedUserActionsMap, oldId, newId);
                    changeIdsInMap(mEntityIdToDispatchedUserActionsMap, oldId, newId);

                    if (mEntityIdToStallFlagMap.containsKey(oldId)) {
                        mEntityIdToStallFlagMap.put(newId, mEntityIdToStallFlagMap.remove(oldId));
                    }

                    if (mCompletedEntityIds.contains(oldId)) {
                        mCompletedEntityIds.remove(oldId);
                        mCompletedEntityIds.add(newId);
                    }

                    // TODO: also need to change IDs in database!!!!
                }
            }
        }

        private String getPermanentEntityId(String tempEntityId) {
            return mTempIdRetriever.getNewIdForTempId(tempEntityId);
        }

        private boolean isCreateRequestUserAction(UserActionEntity userAction) {
            return userAction.getEntityType().equals(IDoCareContract.UserActions.ENTITY_TYPE_REQUEST) &&
                    userAction.getActionType().equals(IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST);
        }

        private void changeIdsInMap(Map<String, List<UserActionEntity>> map,
                                        String oldId, String newId) {
            if (map.containsKey(oldId)) {

                // obtain the list of actions mapped by old ID
                List<UserActionEntity> userActions = map.remove(oldId);

                // update IDs for all actions in the list
                for (ListIterator<UserActionEntity> iter = userActions.listIterator(); iter.hasNext(); ) {
                    UserActionEntity userAction = iter.next();
                    UserActionEntity updatedUserAction = UserActionEntity.newBuilder(userAction)
                            .setEntityId(newId)
                            .build();
                    iter.set(updatedUserAction);
                }

                // return the list to the map under new ID
                map.put(newId, userActions);
            }
        }

        public List<String> getCompletedEntityIds() {
            synchronized (DISPATCHER_LOCK) {
                return new ArrayList<>(mCompletedEntityIds);
            }
        }

        private boolean isStallingAction(UserActionEntity userAction) {
            switch (userAction.getActionType()) {
                case IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST:
                case IDoCareContract.UserActions.ACTION_TYPE_PICKUP_REQUEST:
                case IDoCareContract.UserActions.ACTION_TYPE_CLOSE_REQUEST:
                    return true;
                default:
                    return false;
            }
        }

    }

    /**
     * This comparator will be used to sort user's actions according to the order they need to
     * be uploaded to the server.
     */
    private class UserActionsComparator implements Comparator<UserActionEntity> {

        @Override
        public int compare(UserActionEntity lhs, UserActionEntity rhs) {
            return lhs.getTimestamp().compareTo(rhs.getTimestamp());
        }
    }


}
