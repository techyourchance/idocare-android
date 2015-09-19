package il.co.idocare.networking;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import il.co.idocare.datamodels.functional.UserActionItem;
import il.co.idocare.networking.interfaces.LegacyServerResponseHandler;
import il.co.idocare.networking.responsehandlers.LegacyUserActionsServerResponseHandler;
import il.co.idocare.contentproviders.IDoCareContract;

/**
 * This class synchronizes the local modifications made by the user to the server.
 */
public class DataUploader implements LegacyServerHttpRequest.OnServerResponseCallback{

    private static final String LOG_TAG = DataUploader.class.getSimpleName();


    /*
     * This object will be used to synchronize access to ContentProviderClient. We don't sync on
     * the provider itself because it is originally passed to SyncAdapter.onPerformSync() and is
     * managed externally, therefore we can't know if it is safe to sync on it (well, the current
     * source code suggests that it is safe, but this can change in future releases of AOSP,
     * therefore we don't take chances).
     */
    private final Object CONTENT_PROVIDER_CLIENT_LOCK = new Object();


    private String mUserId;
    private String mAuthToken;
    private UserActionsDispatcher mDispatcher;
    private ThreadPoolExecutor mExecutor;

    /*
    Documentation for ContentProviderClient states that:

    "Note that you should generally create a new ContentProviderClient instance for each thread
    that will be performing operations. Unlike ContentResolver, the methods here such as
    query(Uri, String[], String, String[], String) and openFile(Uri, String) are not thread
    safe -- you must not call release() on the ContentProviderClient those calls are made from
    until you are finished with the data they have returned."

    Only they know what did they mean by that, therefore we will sync any access to
    ContentProviderClient....

     TODO: explore ContentProviderClient's source code and reconsider the sync techniques employed here
      */
    private ContentProviderClient mProvider;

    private DataUploaderAssistant mAssistant;


    public DataUploader(String userId, String authToken,
                        ContentProviderClient provider) {
        mUserId = userId;
        mAuthToken = authToken;
        mProvider = provider;

        mAssistant = new DataUploaderAssistant(mUserId, authToken, provider);


        int numOfCores = Runtime.getRuntime().availableProcessors();
        // TODO: consider using bound queue for executor - will be easier to debug and more efficient because the commands are stored in dispatcher
        mExecutor = new ThreadPoolExecutor(
                numOfCores+1,
                numOfCores+1,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );

        mDispatcher = new UserActionsDispatcher();


    }


    public void uploadAll() {
        // Get a mapping between modified requests and the corresponding user's actions and
        // pass user actions mapping to the dispatcher for binding and sorting
        mDispatcher.prepareUserActionsForDispatching(queryForUserActions(mProvider));

        Runnable serverRequest;
        UserActionItem userAction;

        while (!mDispatcher.isEmpty()) {

            // This call will block until dispatcher releases the next command
            userAction = mDispatcher.dispatchNextUserAction();

            if (userAction != null) {
                synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
                    // UserActionItem will be used to determine the type of the request, as well as
                    // Asynchronous Completion Token for the callback
                    serverRequest = mAssistant.createUserActionServerRequest(userAction,
                            this, userAction);
                }

                mExecutor.execute(serverRequest);
            }
        }

        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        performCleanup();

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
        // TODO: the readUserActionsTable-ensureNoActions-clearModifiedFlag operations (below) must execute atomically!!!! Find a way!
        Cursor cursor = null;
        List<Long> completedEntitiesIds = mDispatcher.getCompletedEntityIds();
        synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
            for (long entityId : completedEntitiesIds) {
                try {
                    cursor = mProvider.query(
                            IDoCareContract.UserActions.CONTENT_URI,
                            new String[] {IDoCareContract.UserActions._ID},
                            IDoCareContract.UserActions.COL_ENTITY_ID + " = ?",
                            new String[] {String.valueOf(entityId)},
                            IDoCareContract.UserActions.SORT_ORDER_DEFAULT
                    );

                    if (!cursor.moveToFirst() || cursor.getCount() == 0) {
                        // No user actions for that ENTITY_ID - clear locally modified flag
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG, "0");
                        mProvider.update(
                                ContentUris.withAppendedId(IDoCareContract.Requests.CONTENT_URI, entityId),
                                contentValues,
                                null,
                                null
                        );
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) cursor.close();
                }

            }
        }
    }

    /**
     * This method removes all temp IDs mappings from DB
     */
    private void clearTempIdMappings() {
        synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
            try {
                mProvider.delete(
                        IDoCareContract.TempIdMappings.CONTENT_URI,
                        null,
                        null
                );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Please note that this callback will be called from whatever thread the corresponding
    ServerRequest happens to run on.
    */
    @Override
    public void serverResponse(int statusCode, String reasonPhrase, String entityString,
                               Object asyncCompletionToken) {

        UserActionItem userAction = (UserActionItem) asyncCompletionToken;

        // First of all - save the response data to the DB. This allows to process the data
        // "offline" and recover from e.g. crashes and process kills by OS
        cacheServerResponse(userAction.mId, statusCode, reasonPhrase, entityString);


        // Create an appropriate response handler
        LegacyServerResponseHandler responseHandler =
                new LegacyUserActionsServerResponseHandler(userAction);

        try {
            // When handling responses the response handler can use ContentProviderClient, therefore
            // this call must be synchronized
            // TODO: this method might take a long time to execute. Try to find approach that will quire lock for less time
            synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
                responseHandler.handleResponse(statusCode, reasonPhrase, entityString, mProvider);
            }

            // Notify the dispatcher that the user's action has been uploaded
            mDispatcher.dispatchedUserActionUploaded(userAction);

        } catch (LegacyServerResponseHandler.ServerResponseHandlerException e) {
            e.printStackTrace();
            // Let dispatcher know that the action completed, otherwise it will be waiting there
            // forever.
            // TODO: add some error handling here.... Let the dispatcher know of error for retry
            mDispatcher.dispatchedUserActionUploaded(userAction);
            return;
        }


        int actionsDeletedFromDB = -1;
        // Remove the uploaded user's action from the DB
        synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
            try {
                actionsDeletedFromDB = mProvider.delete(
                        ContentUris.withAppendedId(IDoCareContract.UserActions.CONTENT_URI, userAction.mId),
                        null,
                        null
                );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (actionsDeletedFromDB != 1) {
            Log.e(LOG_TAG, "error when removing user's action from DB. Actions removed: " + actionsDeletedFromDB);
        }
    }

    /**
     * Cache the data obtained with a response from the server in the local DB. The cached data can
     * be used in order to recover from situations when the processing of the actual response
     * hasn't been completed due to e.g. a crash or a kill by OS...
     */
    private void cacheServerResponse(long id, int statusCode, String reasonPhrase,
                                     String entityString) {
        try {
            ContentValues contentValues = new ContentValues(3);
            contentValues.put(IDoCareContract.UserActions.COL_SERVER_RESPONSE_STATUS_CODE, statusCode);
            contentValues.put(IDoCareContract.UserActions.COL_SERVER_RESPONSE_REASON_PHRASE, reasonPhrase);
            contentValues.put(IDoCareContract.UserActions.COL_SERVER_RESPONSE_ENTITY, entityString);
            synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
                mProvider.update(
                        ContentUris.withAppendedId(IDoCareContract.UserActions.CONTENT_URI, id),
                        contentValues,
                        null,
                        null);
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "couldn't save server response to the DB");
            e.printStackTrace();
        }
    }


    /**
     * This method creates mapping between request ID's and user actions performed on these
     * requests.
     * @param provider ContentProviderClient to query for user actions
     * @return a mapping from request ID's to local user's actions performed
     *         on these requests, or null in case of an error
     */
    private Map<Long, List<UserActionItem>> queryForUserActions(
            ContentProviderClient provider) {

        // Get a list of locally modified requests
        List<Long> modRequests = queryForLocallyModifiedRequests(provider);


        if (modRequests == null) {
            Log.e(LOG_TAG, "Couldn't obtain a list of locally modified requests");
            return null;
        }

        Map<Long, List<UserActionItem>> map =
                new HashMap<>(modRequests.size());

        Cursor cursor = null;
        List<UserActionItem> userActions = null;
        UserActionItem userActionItem = null;

        for (long requestId : modRequests) {

            try {
                // This sync is precaution - it will most probably be an overkill
                synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
                    // Get all user actions for this particular request ID which do not have
                    // non-default server response status codes (which means that server has already
                    // processed these commands)
                    cursor = provider.query(
                            IDoCareContract.UserActions.CONTENT_URI,
                            IDoCareContract.UserActions.PROJECTION_ALL,
                            IDoCareContract.UserActions.COL_ENTITY_ID + " = ? AND "
                            + IDoCareContract.UserActions.COL_SERVER_RESPONSE_STATUS_CODE + " = ?",
                            new String[] {String.valueOf(requestId), "0"},
                            IDoCareContract.UserActions.SORT_ORDER_DEFAULT
                    );
                }

                // Create a list of UserActionItems
                if (cursor != null && cursor.moveToFirst()) {
                    userActions = new ArrayList<>();
                    do {
                        userActionItem = UserActionItem.create(cursor);
                        if (userActionItem != null) userActions.add(userActionItem);
                        userActionItem = null;
                    } while(cursor.moveToNext());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                continue;
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            if (userActions != null && !userActions.isEmpty()) {
                map.put(requestId, userActions);
            }

            userActions = null;
        }

        return map;
    }



    /**
     * This method queries the DB for ID's of all requests which are marked as "locally modified"
     * @return a list containing ID's of all "locally modified" requests, or null in case of an error.
     */
    private List<Long> queryForLocallyModifiedRequests(ContentProviderClient provider) {
        List<Long> modRequests;
        Cursor cursor = null;

        try {
            // This sync is precaution - it will most probably be an overkill
            synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
                cursor = provider.query(
                        IDoCareContract.Requests.CONTENT_URI,
                        new String[] {IDoCareContract.Requests.COL_REQUEST_ID},
                        IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG + " > 0",
                        null,
                        null
                );
            }

            modRequests = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    modRequests.add(cursor.getLong(
                            cursor.getColumnIndex(IDoCareContract.Requests.COL_REQUEST_ID)));
                } while (cursor.moveToNext());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }

        return modRequests;
    }


    /**
     * This class helps in the ordering of the user's actions that need to be uploaded and handles
     * the dependencies that must be enforced (e.g. new entities must be synced to the server, and
     * only then other actions on these entities can be performed)...
     */
    private class UserActionsDispatcher {

        private final Object DISPATCHER_LOCK = new Object();

        private Map<Long, ArrayList<UserActionItem>> mNonDispatchedUserActions;
        private Map<Long, ArrayList<UserActionItem>> mDispatchedUserActions;
        private Map<Long, Boolean> mWaitForResponseBeforeProceed;
        private List<Long> mCompletedEntityIds;

        public UserActionsDispatcher() {}

        public void prepareUserActionsForDispatching(Map<Long, List<UserActionItem>> userActions) {

            // This method should be run before multi-threading is initiated, but just as a
            // precaution for further modifications
            synchronized (DISPATCHER_LOCK) {

                mNonDispatchedUserActions = new HashMap<>(userActions.size());

                // We need to keep track of dispatched actions until we get confirmation on success or
                // failure (in which case we might need to handle the error).
                mDispatchedUserActions = new HashMap<>(userActions.size());

                // This map contains "stall" indicators - when any of them is TRUE, the actions for the
                // respective entity should not be dispatched until the indicator is cleared
                mWaitForResponseBeforeProceed = new HashMap<>(userActions.size());

                for (long id : userActions.keySet()) {
                    // Initialize the "stall" indicator for this entity
                    mWaitForResponseBeforeProceed.put(id, false);

                    // Initialize the "dispatched" list for this entity
                    mDispatchedUserActions.put(id, new ArrayList<UserActionItem>());

                    // Initialize the "non-dispatched" list for this entity by sorting the
                    // provided list of user actions
                    Collections.sort(userActions.get(id), new UserActionsComparator());
                    mNonDispatchedUserActions.put(id,
                            new ArrayList<UserActionItem>(userActions.get(id)));
                }
            }

            mCompletedEntityIds = new ArrayList<>(userActions.size());
        }

        /**
         * TODO: the logic in this method should change if we'll allow dispatched actions to be returned to non-dispatched state (e.g. in case of error)
         * @return true if all actions had been dispatched
         */
        public boolean isEmpty() {
            synchronized (DISPATCHER_LOCK) {
                for (long id : mNonDispatchedUserActions.keySet()) {
                    if (!mNonDispatchedUserActions.get(id).isEmpty())
                        return false;
                }
            }
            return true;
        }


        /**
         * Get the next UserActionItem that need to be uploaded to the server.<br>
         *
         * Call to this method will block if there are non-dispatched actions, but they are
         * waiting for some already dispatched actions to complete. Once any dispatched "stalling"
         * action completes, this method will unblock and return one of the released UserActionItem's.<br>
         *
         * In case this method returns null the clients might retry to call this method, but should
         * ensure that this dispatcher is not empty beforehand.
         * @return UserActionItem object that might be uploaded to the server, or null in case of
         * an error.
         */
        public UserActionItem dispatchNextUserAction() {

            /*
            The dispatcher sorts user's actions for each entity, but it does not mix them among
            entities. This way, upvotes for one entity can be uploaded before pickups for another
            entity.
            While the above is not an error, some user's actions (like PICKUP) should be uploaded
            ASAP, therefore it makes sense to add logic that can prioritize actions from different
            entities.
             TODO: try to come up with a scheme that achieves the above aim
             */

            /*
             * The below sync is required not only for thread-safety, and blocking of threads,
             * but also for atomicity - three distinct maps are referenced in this code, and this
             * is the way to ensure that they stay consistent during execution.
             */
            synchronized (DISPATCHER_LOCK) {

                // Limit the number of times each thread will attempt to get a new user action
                // TODO: reevaluate this limit: do we need it at all, and if we do, is the limit sane?
                for (int i=0; i < 100; i ++) {

                    boolean moreNonDispatchedActions = false;

                    for (long id : mNonDispatchedUserActions.keySet()) {

                        if (!mNonDispatchedUserActions.get(id).isEmpty()) {

                            if (mWaitForResponseBeforeProceed.get(id)) {
                                // If we got here - there are non-dispatched actions for this entity,
                                // but they are stalled. We need to skip this entity's actions,
                                // but "remember" that they haven't been dispatched...
                                moreNonDispatchedActions = true;
                                continue;
                            }

                            // TODO: removing from ArrayList at index 0 is the most expensive. Try to find a more efficient solution
                            UserActionItem nextAction = mNonDispatchedUserActions.get(id).remove(0);

                            // Prevent other actions for this entity from being dispatched if server
                            // response for this action must be received before proceeding
                            if (isStallingAction(nextAction)) {
                                mWaitForResponseBeforeProceed.put(id, true);
                            }

                            // Mark user's action as being dispatched
                            mDispatchedUserActions.get(id).add(nextAction);

                            return nextAction;
                        }
                    }

                    // If we got here - none of the actions could be dispatched. If there are
                    // additional non-dispatched actions - these actions weren't dispatched
                    // because they are "stalled", and the calling thread should block until these
                    // actions are "un-stalled"
                    if (moreNonDispatchedActions) {
                        try {
                            DISPATCHER_LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return null;
                    }
                }

                // If we got here - there were too many attempts to dispatch the next user action
                Log.e(LOG_TAG, "too many attempts to dispatch the next user action.");
                return null;
            }
        }

        /**
         * Let the dispatcher know that one of the dispatched actions have been uploaded to the
         * server
         */
        public void dispatchedUserActionUploaded(UserActionItem userAction) {

            long entityId = userAction.mEntityId;

            synchronized (DISPATCHER_LOCK) {

                if (!mDispatchedUserActions.get(entityId).contains(userAction)) {
                    throw new IllegalArgumentException("this user's action is not in the 'dispatched' "
                            + "pool:\n" + userAction.toString());
                }

                // Remove the action from the dispatched list
                mDispatchedUserActions.get(entityId).remove(userAction);


                if (mDispatchedUserActions.get(entityId).isEmpty() &&
                        mNonDispatchedUserActions.get(entityId).isEmpty()) {
                    // If both the dispatched and the non-dispatched lists for this entity are
                    // empty - remove this entity from all maps and add it to the list of completed
                    // entities
                    mNonDispatchedUserActions.remove(entityId);
                    mDispatchedUserActions.remove(entityId);
                    mWaitForResponseBeforeProceed.remove(entityId);
                    mCompletedEntityIds.add(entityId);
                }
                else if (isStallingAction(userAction)) {
                    // If the action was of "stalling" type - release entity's  stall indicator
                    mWaitForResponseBeforeProceed.put(entityId, false);
                    /*
                     Notify all waiting threads. Maybe we could not notify all threads, or the
                     notification could be subject to some condition, but since a mistake here
                     would lead to deadlock I prefer to be on the safe side.
                     */
                    DISPATCHER_LOCK.notifyAll();
                }



                // There are user actions that their uploading changes IDs of entities (e.g. new requests
                // get permanent IDs assigned by the server) - account for this change
                ensureConsistentEntityIds(userAction);
            }


        }

        /**
         * Some user actions, when uploaded to the server, change entities IDs (e.g. newly created
         * requests assigned permanent IDs during uploading). This method ensures that the internal
         * IDs managed by this dispatcher stay in sync with these changes
         */
        private void ensureConsistentEntityIds(UserActionItem userAction) {
            if (userAction.mEntityType.equals(IDoCareContract.UserActions.ENTITY_TYPE_REQUEST) &&
                    userAction.mActionType.equals(IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST)) {
                // The uploaded action was of type "create request", which means that the ID of this
                // request has changed...
                long oldId = userAction.mEntityId;
                long newId = 0;
                // There should be mapping from old entity ID to the new entity ID...
                /*
                  IMPORTANT: it is kind of dangerous to use the lock from the enclosing class here,
                  but I do it in order to simplify things. If the app will hang in the future -
                  this is one of the first places to check for deadlocks.
                  */
                synchronized (CONTENT_PROVIDER_CLIENT_LOCK) {
                    Cursor cursor = null;
                    try {
                        cursor = mProvider.query(
                                ContentUris.withAppendedId(IDoCareContract.TempIdMappings.CONTENT_URI, oldId),
                                new String[] {IDoCareContract.TempIdMappings.COL_PERMANENT_ID},
                                null,
                                null,
                                null
                        );

                        if (cursor != null && cursor.moveToFirst()) {
                            newId = cursor.getLong(cursor.getColumnIndexOrThrow(
                                    IDoCareContract.TempIdMappings.COL_PERMANENT_ID));
                        } else {
                            throw new IllegalStateException("while ensuring IDs consistency after " +
                                    "a user action of type " + userAction.mActionType + " for entity " +
                                    userAction.mEntityType + ", could not find a mapping from old ID to a new ID");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        throw new IllegalStateException("couldn't ensure consistent mapping from" +
                                "old ID to the new ID");
                    } finally {
                        if (cursor != null) cursor.close();
                    }

                    if (newId == 0)
                        Log.e(LOG_TAG, "while ensuring a consistent ID the new ID is 0, which is " +
                                "most probably means some error happened and consistency lost");
                }

                // Now change IDs in all the data structures... huh
                changeIdsInMap(mNonDispatchedUserActions, oldId, newId);
                changeIdsInMap(mDispatchedUserActions, oldId, newId);
                changeIdsInMap(mWaitForResponseBeforeProceed, oldId, newId);
                if (mCompletedEntityIds.contains(oldId)) {
                    mCompletedEntityIds.remove(Long.valueOf(oldId));
                    mCompletedEntityIds.add(newId);
                }

            }
        }

        private <T> void changeIdsInMap(Map<Long, T> map,
                                    long oldId, long newId) {
            if (map.containsKey(oldId)) {

                map.put(newId, map.remove(oldId));

                // Check whether the element of the map is a List
                if (List.class.isAssignableFrom(map.get(newId).getClass())) {
                    for (Object item : (List)map.get(newId)) {
                        // Check whether the element of the list is UserActionItem
                        if (UserActionItem.class.isAssignableFrom(item.getClass())) {
                            ((UserActionItem)item).mEntityId = newId;
                        }
                    }
                }
            }
        }

        public List<Long> getCompletedEntityIds() {
            synchronized (DISPATCHER_LOCK) {
                return new ArrayList<>(mCompletedEntityIds);
            }
        }

        private boolean isStallingAction(UserActionItem userAction) {
            String actionType = userAction.mActionType;

            if (actionType.equals(IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST) ||
                    actionType.equals(IDoCareContract.UserActions.ACTION_TYPE_PICKUP_REQUEST)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * This comparator will be used to sort user's actions according to the order they need to
     * be uploaded to the server.
     */
    private class UserActionsComparator implements Comparator<UserActionItem> {

        @Override
        public int compare(UserActionItem lhs, UserActionItem rhs) {

        /*
         * The order of uploads of user's actions to the server is important.
         *
         * The following considerations should be taken into account:
         * 1. Newly added requests do not have ID's (which are assigned by the server). Therefore,
         *    the addition of new requests should be uploaded successfully before attempting to sync
         *    any other action on these requests.
         * 2.
         */



            // TODO: find a way to write this method in a cleaner manner (maybe use enums' ordinals?)

            if (!lhs.mEntityType.equals(rhs.mEntityType)) {
                // Sort by entity type
                if (lhs.mEntityType.equals(IDoCareContract.UserActions.ENTITY_TYPE_REQUEST)) {
                    if (rhs.mEntityType.equals(IDoCareContract.UserActions.ENTITY_TYPE_ARTICLE))
                        return -1;
                    else
                        throw new IllegalArgumentException("unrecognized ENTITY_TYPE: " + rhs.mEntityType);
                }
                else if (lhs.mEntityType.equals(IDoCareContract.UserActions.ENTITY_TYPE_ARTICLE)) {
                    if (rhs.mEntityType.equals(IDoCareContract.UserActions.ENTITY_TYPE_REQUEST))
                        return 1;
                    else
                        throw new IllegalArgumentException("unrecognized ENTITY_TYPE: " + rhs.mEntityType);
                }
                else {
                    throw new IllegalArgumentException("unrecognized ENTITY_TYPE: " + lhs.mEntityType);
                }
            }
            else if (lhs.mEntityId != rhs.mEntityId) {
                // Sort by entity id
                return lhs.mEntityId < rhs.mEntityId ? -1 : 1;
            }
            else if (!lhs.mActionType.equals(rhs.mActionType)) {
                if (lhs.mEntityType.equals(IDoCareContract.UserActions.ENTITY_TYPE_REQUEST)) {
                    if (lhs.mActionType.equals(IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST)) {
                        // Create request action has highest priority
                        return -1;
                    }
                    else if (lhs.mActionType.equals(IDoCareContract.UserActions.ACTION_TYPE_PICKUP_REQUEST)) {
                        // Pickup request action has second highest priority
                        return rhs.mActionType.equals(IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST) ?
                                1 : -1;
                    }
                    else if (lhs.mActionType.equals(IDoCareContract.UserActions.ACTION_TYPE_CLOSE_REQUEST)) {
                        return rhs.mActionType.equals(IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST) ||
                                rhs.mActionType.equals(IDoCareContract.UserActions.ACTION_TYPE_PICKUP_REQUEST) ?
                                1 : -1;
                    }
                    else if (lhs.mActionType.equals(IDoCareContract.UserActions.ACTION_TYPE_VOTE)) {
                        // Voting has lowest priority
                        return 1;
                    }
                    else {
                        throw new IllegalArgumentException("unrecognized ACTION_TYPE: " + lhs.mActionType);
                    }

                }
                else if (lhs.mEntityType.equals(IDoCareContract.UserActions.ENTITY_TYPE_ARTICLE)) {
                    throw new UnsupportedOperationException("sorting actions on an article is not supported yet");
                }
                else {
                    throw new IllegalArgumentException("unrecognized ENTITY_TYPE: " + lhs.mEntityType);
                }
            }
            else {
                // These actions are "equal"
                return 0;
            }
        }
    }



}
