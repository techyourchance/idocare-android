package il.co.idocare.serversync.syncers;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.networking.newimplementation.ServerApi;
import il.co.idocare.networking.newimplementation.schemes.responses.RequestResponseScheme;
import il.co.idocare.networking.newimplementation.schemes.responses.RequestsResponseScheme;
import il.co.idocare.networking.newimplementation.schemes.responses.RequestScheme;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.requests.RequestsChangedEvent;
import il.co.idocare.requests.cachers.RequestsCacher;
import il.co.idocare.requests.cachers.TempIdCacher;
import il.co.idocare.requests.retrievers.RawRequestRetriever;
import il.co.idocare.serversync.SyncFailedException;
import il.co.idocare.utils.Logger;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private final RawRequestRetriever mRawRequestsRetriever;
    private final TempIdCacher mTempIdCacher;
    private final ServerApi mServerApi;
    private final EventBus mEventBus;
    private Logger mLogger;

    private List<RequestEntity> mCurrentlyCachedRequests;

    public RequestsSyncer(RequestsCacher requestsCacher,
                          RawRequestRetriever rawRequestsRetriever,
                          TempIdCacher tempIdCacher,
                          ServerApi serverApi,
                          EventBus eventBus,
                          Logger logger) {
        mRequestsCacher = requestsCacher;
        mRawRequestsRetriever = rawRequestsRetriever;
        mTempIdCacher = tempIdCacher;
        mServerApi = serverApi;
        mEventBus = eventBus;
        mLogger = logger;
    }

    @WorkerThread
    public void syncRequestCreated(String requestId) {
        mLogger.d(TAG, "syncRequestCreated(); request ID: " + requestId);


        RequestEntity request = mRawRequestsRetriever.getRequestById(requestId);

        MultipartBody.Builder builder = new MultipartBody.Builder();

        builder.setType(MultipartBody.FORM);

        builder.addFormDataPart(Constants.FIELD_NAME_CREATED_COMMENT, request.getCreatedComment());
        builder.addFormDataPart(Constants.FIELD_NAME_LATITUDE, String.valueOf(request.getLatitude()));
        builder.addFormDataPart(Constants.FIELD_NAME_LONGITUDE, String.valueOf(request.getLongitude()));
        builder.addFormDataPart(Constants.FIELD_NAME_CREATED_POLLUTION_LEVEL, "1"); // TODO: remove


        getPicturesRequestBody(
                builder,
                request.getCreatedPictures(),
                Constants.FIELD_NAME_CREATED_PICTURES
        );

        Call<RequestResponseScheme> call = mServerApi.createNewRequest(builder.build());

        try {
            Response<RequestResponseScheme> response = call.execute();

            if (response.isSuccessful()) {
                RequestEntity updatedRequest = convertSchemeToRequest(response.body().getRequestScheme());
                // update temp request with new information
                mRequestsCacher.update(updatedRequest, requestId);
                // cache the mapping from temp request ID to a new one
                mTempIdCacher.cacheTempIdMapping(requestId, updatedRequest.getId());
            } else {
                throw new SyncFailedException("create new request call failed; response code: " + response.code());
            }

        } catch (IOException e) {
            throw new SyncFailedException(e);
        }
    }

    private RequestBody getPicturesRequestBody(MultipartBody.Builder builder, List<String> pictures, String fieldName) {

        String pictureUri;
        File pictureFile;

        for (int i = 0; i < pictures.size(); i ++) {
            pictureUri = pictures.get(i);
            pictureFile = new File(pictureUri);

            if (pictureFile.exists()) {
                builder.addFormDataPart(
                        fieldName + "[" + i + "]",
                        pictureFile.getName(),
                        RequestBody.create(MediaType.parse("image/*"), pictureFile));
            } else {
                mLogger.e(TAG, "picture file doesn't exist: " + pictureFile);
            }
        }

        return builder.build();
    }

    @WorkerThread
    public void syncAllRequests() {
        mLogger.d(TAG, "syncAllRequests()");

        Call<RequestsResponseScheme> call = mServerApi.getRequests();

        try {
            Response<RequestsResponseScheme> response = call.execute();

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
        mCurrentlyCachedRequests = mRawRequestsRetriever.getAllRequests();

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
