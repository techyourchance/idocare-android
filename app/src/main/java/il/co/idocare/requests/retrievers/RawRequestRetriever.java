package il.co.idocare.requests.retrievers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.contentproviders.IDoCareContract.Requests;
import il.co.idocare.requests.RequestEntity;
import il.co.idocare.utils.StringUtils;

/**
 * This class can be used in order to retrieve raw information about requests. "Raw" here means
 * that the retrieved info does not include information about potential user's actions which affected
 * the request in some way and haven't been synced to the server yet.
 */
public class RawRequestRetriever {

    private ContentResolver mContentResolver;

    /**
     * Get "raw" info of requests assigned to user. "Raw" means that the returned information
     * does not take into account the locally cached user's actions on the requests.
     * @param userId ID of the user
     * @return a list of "raw" requests assigned to the user
     */
    @WorkerThread
    public @NonNull List<RequestEntity> getRequestsAssignedToUser(@NonNull String userId) {

        String[] projection = Requests.PROJECTION_ALL;

        String selection = Requests.COL_PICKED_UP_BY + " = ?";
        String[] selectionArgs = new String[] {userId};
        String sortOrder = Requests.COL_PICKED_UP_AT + " ASC";

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(
                    Requests.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);


            if (cursor != null && cursor.moveToFirst()) {
                List<RequestEntity> requests = new ArrayList<>(cursor.getCount());
                do {
                    RequestEntity request = createRequestFromCurrentCursorPosisition(cursor);
                    requests.add(request);
                } while (cursor.moveToNext());
                return requests;
            } else {
                return new ArrayList<>(0);
            }

        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private static RequestEntity createRequestFromCurrentCursorPosisition(Cursor cursor) throws IllegalArgumentException {

        String requestId = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_REQUEST_ID));
        String createdBy = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_CREATED_BY));
        String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_CREATED_AT));
        String createdComment = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_CREATED_COMMENT));
        List<String> createdPictures = StringUtils.commaSeparatedStringToList(
                cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_CREATED_PICTURES)));
        int createdVotes = cursor.getInt(cursor.getColumnIndexOrThrow(Requests.COL_CREATED_VOTES));
        double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Requests.COL_LATITUDE));
        double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Requests.COL_LONGITUDE));
        String pickedUpBy = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_PICKED_UP_BY));
        String pickedUpAt = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_PICKED_UP_AT));
        String closedBy = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_CLOSED_BY));
        String closedAt = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_CLOSED_AT));
        String closedComment = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_CLOSED_COMMENT));
        List<String> closedPictures = StringUtils.commaSeparatedStringToList(
                cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_CLOSED_PICTURES)));
        int closedVotes = cursor.getInt(cursor.getColumnIndexOrThrow(Requests.COL_CLOSED_VOTES));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(Requests.COL_LOCATION));

        return new RequestEntity(requestId, createdBy, createdAt, createdComment, createdPictures,
                createdVotes, latitude, longitude, pickedUpBy, pickedUpAt, closedBy, closedAt,
                closedComment, closedPictures, closedVotes, location);
    }
}
