package il.co.idocarecore.requests.cachers;

import android.content.ContentResolver;
import android.content.ContentValues;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import il.co.idocarecore.contentproviders.ContentProviderUtils;
import il.co.idocarecore.requests.RequestEntity;
import il.co.idocarecore.contentproviders.IDoCareContract.Requests;
import il.co.idocarecore.requests.events.RequestsChangedEvent;
import il.co.idocarecore.utils.Logger;
import il.co.idocarecore.utils.StringUtils;
import il.co.idocarecore.utils.Logger;
import il.co.idocarecore.utils.StringUtils;

/**
 * This class handles request related info storage into the cache
 */
public class RequestsCacher {

    private static final String TAG = "RequestsCacher";

    private final ContentResolver mContentResolver;
    private EventBus mEventBus;
    private final Logger mLogger;

    public RequestsCacher(ContentResolver contentResolver, EventBus eventBus, Logger logger) {
        mContentResolver = contentResolver;
        mEventBus = eventBus;
        mLogger = logger;
    }


    public void delete(RequestEntity request) {
        mContentResolver.delete(
                Requests.CONTENT_URI,
                Requests.COL_REQUEST_ID + " = ?",
                new String[] {request.getId()}
        );
    }

    public void deleteAllRequestsWithNonMatchingIds(List<String> requestIds) {
        ContentProviderUtils.SelectionAndSelectionArgsPair selectionPair =
                ContentProviderUtils.getSelectionByColumnExceptListOfValues(
                        Requests.COL_REQUEST_ID,
                        requestIds
                );

        mContentResolver.delete(
                Requests.CONTENT_URI,
                selectionPair.getSelection(),
                selectionPair.getSelectionArgs()
        );
    }

    public void deleteAllRequests() {
        mContentResolver.delete(
                Requests.CONTENT_URI,
                null,
                null
        );
    }

    public void updateOrInsertAndNotify(RequestEntity request) {
        updateOrInsert(request);
        notifyRequestsChanged();
    }

    public void updateOrInsert(RequestEntity request) {
        mLogger.d(TAG, "updateOrInsert() called; request ID: " + request.getId());
        // TODO: make operations atomic
        ContentValues cv = requestEntityToContentValues(request);

        int updateCount = mContentResolver.update(
                Requests.CONTENT_URI,
                cv,
                Requests.COL_REQUEST_ID + " = ?",
                new String[] {request.getId()}
        );

        if (updateCount <= 0) {
            mContentResolver.insert(
                    Requests.CONTENT_URI,
                    cv
            );
            mLogger.v(TAG, "new request inserted");
        } else {
            mLogger.v(TAG, "request updated");
        }
    }

    public void updateWithPossibleIdChange(RequestEntity request, String currentRequestId) {
        mLogger.d(TAG, "updateWithPossibleIdChange() called; current request ID: " + request.getId());
        // TODO: make operations atomic
        ContentValues cv = requestEntityToContentValues(request);

        int updateCount = mContentResolver.update(
                Requests.CONTENT_URI,
                cv,
                Requests.COL_REQUEST_ID + " = ?",
                new String[] {currentRequestId}
        );

        if (updateCount <= 0) {
            throw new RuntimeException("no entries updateWithPossibleIdChange");
        }
    }

    private ContentValues requestEntityToContentValues(RequestEntity requestEntity) {

        ContentValues values = new ContentValues();

        values.put(Requests.COL_REQUEST_ID, requestEntity.getId());
        values.put(Requests.COL_CREATED_BY, requestEntity.getCreatedBy());
        values.put(Requests.COL_CREATED_AT, requestEntity.getCreatedAt());
        values.put(Requests.COL_CREATED_COMMENT, requestEntity.getCreatedComment());
        values.put(Requests.COL_CREATED_PICTURES, StringUtils.listToCommaSeparatedString(requestEntity.getCreatedPictures()));
        values.put(Requests.COL_CREATED_VOTES, requestEntity.getCreatedVotes());
        values.put(Requests.COL_LATITUDE, requestEntity.getLatitude());
        values.put(Requests.COL_LONGITUDE, requestEntity.getLongitude());
        values.put(Requests.COL_PICKED_UP_BY, requestEntity.getPickedUpBy());
        values.put(Requests.COL_PICKED_UP_AT, requestEntity.getPickedUpAt());
        values.put(Requests.COL_CLOSED_BY, requestEntity.getClosedBy());
        values.put(Requests.COL_CLOSED_AT, requestEntity.getClosedAt());
        values.put(Requests.COL_CLOSED_COMMENT, requestEntity.getClosedComment());
        values.put(Requests.COL_CLOSED_PICTURES, StringUtils.listToCommaSeparatedString(requestEntity.getClosedPictures()));
        values.put(Requests.COL_CLOSED_VOTES, requestEntity.getClosedVotes());
        values.put(Requests.COL_LOCATION, requestEntity.getLocation());

        return values;
    }

    private void notifyRequestsChanged() {
        mEventBus.post(new RequestsChangedEvent());
    }

}
