package il.co.idocarecore.requests.retrievers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.requests.RequestsByLatestActivityComparator;
import il.co.idocarecore.useractions.entities.UserActionEntity;
import il.co.idocarecore.useractions.retrievers.UserActionsRetriever;
import il.co.idocarecore.requests.RequestsByIdComparator;
import il.co.idocarecore.useractions.UserActionsToRequestsApplier;

/**
 * This class ca be user in order to retrieve information about requests. In contrast to
 * {@link RawRequestRetriever}, information returned by this class reflects the state of requests
 * after potential local modifications by the user were accounted for.
 */

public class RequestsRetriever {

    private RawRequestRetriever mRawRequestsRetriever;
    private UserActionsRetriever mUserActionsRetriever;
    private UserActionsToRequestsApplier mUserActionsToRequestsApplier;

    private RequestsByIdComparator mComparator = new RequestsByIdComparator();

    public RequestsRetriever(@NonNull RawRequestRetriever rawRequestsRetriever,
                             @NonNull UserActionsRetriever userActionsRetriever,
                             @NonNull UserActionsToRequestsApplier userActionsToRequestsApplier) {
        mRawRequestsRetriever = rawRequestsRetriever;
        mUserActionsRetriever = userActionsRetriever;
        mUserActionsToRequestsApplier = userActionsToRequestsApplier;
    }

    public @Nullable RequestEntity getRequestById(String requestId) {
        RequestEntity rawRequest = mRawRequestsRetriever.getRequestById(requestId);

        if (rawRequest == null) return null;

        List<RequestEntity> requests = new ArrayList<>(1);
        requests.add(rawRequest);

        return applyUserActionsToRequests(requests).get(0);
    }

    public List<RequestEntity> getAllRequests() {
        List<RequestEntity> rawRequests = mRawRequestsRetriever.getAllRequests();

        List<RequestEntity> requests = applyUserActionsToRequests(rawRequests);

        return orderRequestsByLatestActivity(requests);
    }

    /**
     * Get information about requests assigned to the user.
     * @param userId ID of the user
     * @return a list of requests assigned to the user
     */
    @WorkerThread
    public List<RequestEntity> getRequestsAssignedToUser(String userId) {
        List<RequestEntity> rawRequests = mRawRequestsRetriever.getRequestsAssignedToUser(userId);

        List<RequestEntity> requests = applyUserActionsToRequests(rawRequests);

        return orderRequestsByLatestActivity(requests);
    }


    private List<RequestEntity> applyUserActionsToRequests(List<RequestEntity> requests) {

        List<UserActionEntity> userActions = mUserActionsRetriever.getAllUserActions();

        if (userActions.isEmpty() || requests.isEmpty()) return requests;

        Collections.sort(requests, mComparator); // sort in order to be able to use binary search

        List<RequestEntity> updatedRequests = new ArrayList<>(requests);

        RequestEntity updatedRequest;

        for (UserActionEntity userAction : userActions) {
            if (mUserActionsToRequestsApplier.isUserActionAffectingRequest(userAction)) {

                // find the request affected by user action, and if it is found - apply the action to it
                int affectedEntityPos =
                        Collections.binarySearch(
                                requests,
                                RequestEntity.getBuilder().setId(userAction.getEntityId()).build(),
                                mComparator);

                if (affectedEntityPos >= 0) {
                    updatedRequest = mUserActionsToRequestsApplier.applyUserActionToRequest(
                            userAction, requests.get(affectedEntityPos));
                    updatedRequests.set(affectedEntityPos, updatedRequest);
                }

            }
        }

        return updatedRequests;

    }

    private List<RequestEntity> orderRequestsByLatestActivity(List<RequestEntity> requests) {
        Collections.sort(requests, new RequestsByLatestActivityComparator());
        return requests;
    }

}
