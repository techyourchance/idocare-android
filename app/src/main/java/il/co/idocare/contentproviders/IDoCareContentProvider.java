package il.co.idocare.contentproviders;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import il.co.idocarecore.contentproviders.IDoCareContract;
import il.co.idocarecore.contentproviders.IdcSQLiteOpenHelper;
import il.co.idocarecore.contentproviders.SQLiteWrapper;

/**
 * Our implementation of ContentProvider
 */
public class IDoCareContentProvider extends ContentProvider {

    private static final int REQUESTS_LIST = 0;
    private static final int REQUEST_ID = 1;
    private static final int USERS_LIST = 10;
    private static final int USER_ID = 11;
    private static final int UNIQUE_USER_IDS = 12;
    private static final int USER_ACTIONS_LIST = 20;
    private static final int USER_ACTION_ID = 21;
    private static final int TEMP_ID_MAPPINGS_LIST = 30;
    private static final int TEMP_ID_MAPPING_ID = 31;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        /*
        I'm forced to use * instead of # because of:
        https://code.google.com/p/android/issues/detail?id=27031
         */
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "requests", REQUESTS_LIST);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "requests/*", REQUEST_ID);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "users", USERS_LIST);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "users/*", USER_ID);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "unique_user_ids", UNIQUE_USER_IDS);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "user_actions", USER_ACTIONS_LIST);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "user_actions/*", USER_ACTION_ID);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "temp_id_mappings", TEMP_ID_MAPPINGS_LIST);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "temp_id_mappings/*", TEMP_ID_MAPPING_ID);
    }


    /*
    This Data Access Object is a wrapper around SQLiteOpenHelper
     */
    private SQLiteWrapper mSQLiteWrapper;


    @Override
    public boolean onCreate() {
        mSQLiteWrapper = new SQLiteWrapper(IdcSQLiteOpenHelper.getInstance(getContext()));
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
                cursor = mSQLiteWrapper.queryRequests(
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
                cursor = mSQLiteWrapper.queryRequests(
                        projection,
                        IDoCareContract.Requests.COL_REQUEST_ID + " = " + uri.getLastPathSegment()
                                + (TextUtils.isEmpty(selection) ? "" : " AND " + selection),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case USERS_LIST:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.Users.SORT_ORDER_DEFAULT;
                cursor = mSQLiteWrapper.queryUsers(
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case USER_ID:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.Users.SORT_ORDER_DEFAULT;
                cursor = mSQLiteWrapper.queryUsers(
                        projection,
                        IDoCareContract.Users.COL_USER_ID + " = " + uri.getLastPathSegment()
                                + (TextUtils.isEmpty(selection) ? "" : " AND " + selection),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case UNIQUE_USER_IDS:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.UniqueUserIds.SORT_ORDER_DEFAULT;
                cursor = mSQLiteWrapper.queryUniqueUserIds(
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case USER_ACTIONS_LIST:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.UserActions.SORT_ORDER_DEFAULT;
                cursor = mSQLiteWrapper.queryUserActions(
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
                cursor = mSQLiteWrapper.queryUserActions(
                        projection,
                        IDoCareContract.UserActions._ID + " = " + uri.getLastPathSegment()
                                + (TextUtils.isEmpty(selection) ? "" : " AND " + selection),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case TEMP_ID_MAPPINGS_LIST:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.TempIdMappings.SORT_ORDER_DEFAULT;
                cursor = mSQLiteWrapper.queryTempIdMappings(
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case TEMP_ID_MAPPING_ID:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = IDoCareContract.TempIdMappings.SORT_ORDER_DEFAULT;
                cursor = mSQLiteWrapper.queryTempIdMappings(
                        projection,
                        IDoCareContract.TempIdMappings.COL_TEMP_ID + " = " + uri.getLastPathSegment()
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
            case USERS_LIST:
                return IDoCareContract.Users.CONTENT_TYPE;
            case USER_ID:
                return IDoCareContract.Users.CONTENT_ITEM_TYPE;
            case UNIQUE_USER_IDS:
                return IDoCareContract.UniqueUserIds.CONTENT_TYPE;
            case USER_ACTIONS_LIST:
                return IDoCareContract.UserActions.CONTENT_TYPE;
            case USER_ACTION_ID:
                return IDoCareContract.UserActions.CONTENT_ITEM_TYPE;
            case TEMP_ID_MAPPINGS_LIST:
                return IDoCareContract.TempIdMappings.CONTENT_TYPE;
            case TEMP_ID_MAPPING_ID:
                return IDoCareContract.TempIdMappings.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;

        switch(URI_MATCHER.match(uri)) {
            case REQUESTS_LIST:
                id = mSQLiteWrapper.addNewRequest(values);
                break;
            case USERS_LIST:
                id = mSQLiteWrapper.addNewUser(values);
                break;
            case USER_ACTIONS_LIST:
                id = mSQLiteWrapper.addNewUserAction(values);
                break;
            case TEMP_ID_MAPPINGS_LIST:
                id = mSQLiteWrapper.addNewTempIdMapping(values);
                break;
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

        int delCount = 0;

        String idStr;
        String where;

        switch (URI_MATCHER.match(uri)) {
            case REQUESTS_LIST:
                delCount = mSQLiteWrapper.deleteRequests(selection, selectionArgs);
                break;

            case REQUEST_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.Requests.COL_REQUEST_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = mSQLiteWrapper.deleteRequests(where, selectionArgs);
                break;

            case USERS_LIST:
                delCount = mSQLiteWrapper.deleteUsers(selection, selectionArgs);
                break;

            case USER_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.Users.COL_USER_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = mSQLiteWrapper.deleteUsers(where, selectionArgs);
                break;
            
            case USER_ACTIONS_LIST:
                delCount = mSQLiteWrapper.deleteUserActions(selection, selectionArgs);
                break;

            case USER_ACTION_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.UserActions._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = mSQLiteWrapper.deleteUserActions(where, selectionArgs);
                break;

            case TEMP_ID_MAPPINGS_LIST:
                delCount = mSQLiteWrapper.deleteTempIdMappings(selection, selectionArgs);
                break;

            case TEMP_ID_MAPPING_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.TempIdMappings._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = mSQLiteWrapper.deleteTempIdMappings(where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for deletion: " + uri);
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
                updateCount = mSQLiteWrapper.updateRequests(values, selection, selectionArgs);
                break;

            case REQUEST_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.Requests.COL_REQUEST_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mSQLiteWrapper.updateRequests(values, where, selectionArgs);
                break;

            case USERS_LIST:
                updateCount = mSQLiteWrapper.updateUsers(values, selection, selectionArgs);
                break;

            case USER_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.Users.COL_USER_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mSQLiteWrapper.updateUsers(values, where, selectionArgs);
                break;

            case USER_ACTIONS_LIST:
                updateCount = mSQLiteWrapper.updateUserActions(values, selection, selectionArgs);
                break;

            case USER_ACTION_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.UserActions._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mSQLiteWrapper.updateUserActions(values, where, selectionArgs);
                break;

            case TEMP_ID_MAPPINGS_LIST:
                updateCount = mSQLiteWrapper.updateTempIdMappings(values, selection, selectionArgs);
                break;

            case TEMP_ID_MAPPING_ID:
                idStr = uri.getLastPathSegment();
                where = IDoCareContract.TempIdMappings._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mSQLiteWrapper.updateTempIdMappings(values, where, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI for update: " + uri);
        }

        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updateCount;
    }

}
