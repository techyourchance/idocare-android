package il.co.idocare.serversync.syncers;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.networking.newimplementation.ServerApi;
import il.co.idocare.networking.newimplementation.schemes.RequestsScheme;
import il.co.idocare.networking.newimplementation.schemes.RequestScheme;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.requests.RequestsChangedEvent;
import il.co.idocare.requests.cachers.RequestsCacher;
import il.co.idocare.requests.retrievers.RequestsRetriever;
import il.co.idocare.utils.Logger;
import retrofit2.Call;
import retrofit2.Response;

/**
 * This class handles synchronization of requests to/from the server.<br>
 * NOTE: although methods in this class should be called from background threads, the
 * implementation is not thread-safe.
 */
public class RequestsSyncer {

    private static final String TAG = "RequestsSyncer";

    private final RequestsCacher mRequestsCacher;
    private final RequestsRetriever mRequestsRetriever;
    private final ServerApi mServerApi;
    private final EventBus mEventBus;
    private Logger mLogger;

    private List<RequestEntity> mCurrentlyCachedRequests;

    public RequestsSyncer(RequestsCacher requestsCacher,
                          RequestsRetriever requestsRetriever,
                          ServerApi serverApi,
                          EventBus eventBus,
                          Logger logger) {
        mRequestsCacher = requestsCacher;
        mRequestsRetriever = requestsRetriever;
        mServerApi = serverApi;
        mEventBus = eventBus;
        mLogger = logger;
    }

    @WorkerThread
    public void syncAllRequests() {
        mLogger.d(TAG, "syncAllRequests()");

        Call<RequestsScheme> call = mServerApi.requestsList();

        try {
            Response<RequestsScheme> response = call.execute();

            if (response.isSuccessful()) {
                processResponse(response.body().getRequestSchemes());
            } else {
                mLogger.e(TAG, "couldn't fetch requests from the server; response code: " + response.code());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processResponse(@Nullable List<RequestScheme> requests) {
        // TODO: ensure the actions performed atomically

        deleteAllNonModifiedLocallyRequestsFromCache();

        if (requests == null) return;

        cacheRequests(requests);

        notifyRequestsChanged();

    }

    private void deleteAllNonModifiedLocallyRequestsFromCache() {
        mCurrentlyCachedRequests = mRequestsRetriever.getAllRequests();

        List<RequestEntity> currentlyCachedModifiedRequests = new ArrayList<>(0);

        for (RequestEntity cachedRequest : mCurrentlyCachedRequests) {
            if (!cachedRequest.isModifiedLocally()) {
                mRequestsCacher.delete(cachedRequest);
            } else {
                currentlyCachedModifiedRequests.add(cachedRequest);
            }
        }

        // after this method returns, mCurrentlyCachedRequests will contain only locally modified requests
        mCurrentlyCachedRequests = currentlyCachedModifiedRequests;
    }


    private void cacheRequests(List<RequestScheme> requestSchemes) {
        for (RequestScheme requestScheme : requestSchemes) {
            if (!isRequestCurrentlyCached(requestScheme.getId())) {
                RequestEntity request = convertSchemeToRequest(requestScheme);
                mRequestsCacher.updateOrInsert(request);
                mCurrentlyCachedRequests.add(request);
            }
        }
    }

    private boolean isRequestCurrentlyCached(String requestId) {
        for (RequestEntity request : mCurrentlyCachedRequests) {
            if (request.getId().equals(requestId)) {
                return true;
            }
        }
        return false;
    }

    private RequestEntity convertSchemeToRequest(RequestScheme requestScheme) {
        return new RequestEntity(
                requestScheme.getId(),
                requestScheme.getCreatedBy(),
                requestScheme.getCreatedAt(),
                requestScheme.getCreatedComment(),
                parsePicturesList(requestScheme.getCreatedPictures()),
                requestScheme.getCreatedReputation(),
                requestScheme.getLatitude(),
                requestScheme.getLongitude(),
                requestScheme.getPickedUpBy(),
                requestScheme.getPickedUpAt(),
                requestScheme.getClosedBy(),
                requestScheme.getClosedAt(),
                requestScheme.getClosedComment(),
                parsePicturesList(requestScheme.getClosedPictures()),
                requestScheme.getClosedReputation(),
                "",
                false);
    }

    private List<String> parsePicturesList(String picturesListString) {
        if (picturesListString == null || picturesListString.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            return Arrays.asList(picturesListString.split(Constants.PICTURES_LIST_SEPARATOR));
        }
    }

    private void notifyRequestsChanged() {
        mEventBus.post(new RequestsChangedEvent());
    }


}
