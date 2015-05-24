package il.co.idocare.contentproviders;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by Vasiliy on 3/24/2015.
 */
public class IDoCareContentProvider extends ContentProvider {

    private static final int REQUEST_LIST = 0;
    private static final int REQUEST_ID = 1;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "requests", REQUEST_LIST);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "requests/#", REQUEST_ID);
    }


    /*
    This Data Access Object is a wrapper around SQLite DB
     */
    private IDoCareDatabaseDAO mDAO;


    @Override
    public boolean onCreate() {
        mDAO = new IDoCareDatabaseDAO(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = IDoCareContract.Requests.SORT_ORDER_DEFAULT;
        }

        Cursor cursor;

        switch (URI_MATCHER.match(uri)) {
            case REQUEST_LIST:
                cursor = mDAO.queryRequests(
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case REQUEST_ID:
                cursor = mDAO.queryRequests(
                        projection,
                        "WHERE " + IDoCareContract.Requests.REQUEST_ID + " = " + uri.getLastPathSegment()
                                + (TextUtils.isEmpty(selection) ? "" : " AND " + selection),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch(URI_MATCHER.match(uri)) {
            case REQUEST_LIST:
                return IDoCareContract.Requests.CONTENT_TYPE;
            case REQUEST_ID:
                return IDoCareContract.Requests.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        if (URI_MATCHER.match(uri) != REQUEST_LIST) {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

        long id = mDAO.addNewRequest(values);

        return getUriForId(id, uri);
    }



    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
//            if (!isInBatchMode()) {
//                // notify all listeners of changes and return itemUri:
//                getContext().
//                        getContentResolver().
//                        notifyChange(itemUri, null);
//            }
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // TODO: do we allow requests' deletion? If not, then rewrite this method to throw exception.

        int delCount = 0;

        switch (URI_MATCHER.match(uri)) {
            case REQUEST_LIST:
                delCount = mDAO.deleteRequests(selection, selectionArgs);
                break;
            case REQUEST_ID:
                String idStr = uri.getLastPathSegment();
                String where = IDoCareContract.Requests._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = mDAO.deleteRequests(where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
//        // notify all listeners of changes:
//        if (delCount > 0 && !isInBatchMode()) {
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;
        switch (URI_MATCHER.match(uri)) {
            case REQUEST_LIST:
                updateCount = mDAO.updateRequests(values, selection, selectionArgs);
                break;
            case REQUEST_ID:
                String idStr = uri.getLastPathSegment();
                String where = IDoCareContract.Requests._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mDAO.updateRequests(values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
//        // notify all listeners of changes:
//        if (updateCount > 0 && !isInBatchMode()) {
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
        return updateCount;
    }

}
