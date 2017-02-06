package il.co.idocare.requests.cachers;

import android.content.ContentResolver;
import android.content.ContentValues;

import org.greenrobot.eventbus.EventBus;

import il.co.idocare.requests.RequestEntity;
import il.co.idocare.contentproviders.IDoCareContract.Requests;
import il.co.idocare.requests.RequestsChangedEvent;
import il.co.idocare.utils.StringUtils;

/**
 * This class handles request related info storage into the cache
 */
public class RequestsCacher {

    private final ContentResolver mContentResolver;
    private EventBus mEventBus;

    public RequestsCacher(ContentResolver contentResolver, EventBus eventBus) {
        mContentResolver = contentResolver;
        mEventBus = eventBus;
    }


    public void delete(RequestEntity request) {
        mContentResolver.delete(
                Requests.CONTENT_URI,
                Requests.COL_REQUEST_ID + " = ?",
                new String[] {request.getId()}
        );
    }

    public void updateOrInsertAndNotify(RequestEntity request) {
        updateOrInsert(request);
        notifyRequestsChanged();
    }

    public void updateOrInsert(RequestEntity request) {
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
        }
    }

    public void update(RequestEntity request, String currentRequestId) {
        // TODO: make operations atomic
        ContentValues cv = requestEntityToContentValues(request);

        int updateCount = mContentResolver.update(
                Requests.CONTENT_URI,
                cv,
                Requests.COL_REQUEST_ID + " = ?",
                new String[] {currentRequestId}
        );

        if (updateCount <= 0) {
            throw new RuntimeException("no entries update");
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
