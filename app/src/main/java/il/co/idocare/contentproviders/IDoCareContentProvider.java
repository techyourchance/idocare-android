package il.co.idocare.contentproviders;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Our implementation of ContentProvider
 */
public class IDoCareContentProvider extends ContentProvider {

    private static final int REQUESTS_LIST = 0;
    private static final int REQUEST_ID = 1;
    private static final int USER_ACTIONS_LIST = 2;
    private static final int USER_ACTION_ID = 3;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "requests", REQUESTS_LIST);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "requests/#", REQUEST_ID);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "user_actions", USER_ACTIONS_LIST);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "user_actions/#", USER_ACTION_ID);
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


        Cursor cursor;

        switch (URI_MATCHER.match(uri)) {

            case REQUESTS_LIST:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.Requests.SORT_ORDER_DEFAULT;
                cursor = mDAO.queryRequests(
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case REQUEST_ID:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.Requests.SORT_ORDER_DEFAULT;
                cursor = mDAO.queryRequests(
                        projection,
                        IDoCareContract.Requests.COL_REQUEST_ID + " = " + uri.getLastPathSegment()
                                + (TextUtils.isEmpty(selection) ? "" : " AND " + selection),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case USER_ACTIONS_LIST:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.UserActions.SORT_ORDER_DEFAULT;
                cursor = mDAO.queryUserActions(
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case USER_ACTION_ID:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.UserActions.SORT_ORDER_DEFAULT;
                cursor = mDAO.queryUserActions(
                        projection,
                        IDoCareContract.UserActions._ID + " = " + uri.getLastPathSegment()
                                + (TextUtils.isEmpty(selection) ? "" : " AND " + selection),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri.toString());
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch(URI_MATCHER.match(uri)) {
            case REQUESTS_LIST:
                return IDoCareContract.Requests.CONTENT_TYPE;
            case REQUEST_ID:
                return IDoCareContract.Requests.CONTENT_ITEM_TYPE;
            case USER_ACTIONS_LIST:
                return IDoCareContract.UserActions.CONTENT_TYPE;
            case USER_ACTION_ID:
                return IDoCareContract.UserActions.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;

        switch(URI_MATCHER.match(uri)) {
            case REQUESTS_LIST:
                id = mDAO.addNewRequest(values);
                break;
            case USER_ACTIONS_LIST:
                id = mDAO.addNewUserAction(values);
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI for insertion: " + uri);
        }

        return getUriForId(id, uri);
    }



    private Uri getUriForId(long id, Uri uri) {
        if (id >= 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        } else {
            // TODO: handle this error somehow clever....
            return null;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // TODO: do we allow requests' deletion? If not, then rewrite this method to throw exception.

        int delCount = 0;

        String idStr;
        String where;

        switch (URI_MATCHER.match(uri)) {
            case REQUESTS_LIST:
                delCount = mDAO.deleteRequests(selection, selectionArgs);
                break;

            case REQUEST_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.Requests.COL_REQUEST_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = mDAO.deleteRequests(where, selectionArgs);
                break;

            case USER_ACTIONS_LIST:
                delCount = mDAO.deleteUserActions(selection, selectionArgs);
                break;

            case USER_ACTION_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.UserActions._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = mDAO.deleteUserActions(where, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;

        String idStr;
        String where;

        switch (URI_MATCHER.match(uri)) {
            case REQUESTS_LIST:
                updateCount = mDAO.updateRequests(values, selection, selectionArgs);
                break;

            case REQUEST_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.Requests.COL_REQUEST_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mDAO.updateRequests(values, where, selectionArgs);
                break;

            case USER_ACTIONS_LIST:
                updateCount = mDAO.updateUserActions(values, selection, selectionArgs);
                break;

            case USER_ACTION_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.UserActions._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mDAO.updateUserActions(values, where, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updateCount;
    }

}
