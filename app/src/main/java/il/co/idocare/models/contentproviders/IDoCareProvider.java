package il.co.idocare.models.contentproviders;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.Contacts;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Vasiliy on 3/24/2015.
 */
public class IDoCareProvider extends ContentProvider {

    private static final int REQUEST_LIST = 0;
    private static final int REQUEST_ID = 1;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "requests", REQUEST_LIST);
        URI_MATCHER.addURI(IDoCareContract.AUTHORITY, "requests/#", REQUEST_ID);
    }


    private IDoCareDatabaseDAO mDbDAO;


    @Override
    public boolean onCreate() {
        mDbDAO = new IDoCareDatabaseDAO(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = Items.SORT_ORDER_DEFAULT;
        }


        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        boolean useAuthorityUri = false;

        switch (URI_MATCHER.match(uri)) {
            case REQUEST_LIST:
                builder.setTables(DbSchema.TBL_ITEMS);
                break;
            case ITEM_ID:
                builder.setTables(DbSchema.TBL_ITEMS);
                // limit query to one row at most:
                builder.appendWhere(Items._ID + " = "
                        + uri.getLastPathSegment());
                break;
            case PHOTO_LIST:
                builder.setTables(DbSchema.TBL_PHOTOS);
                break;
            case PHOTO_ID:
                builder.setTables(DbSchema.TBL_PHOTOS);
                // limit query to one row at most:
                builder.appendWhere(Contacts.Photos._ID + " = " + uri.getLastPathSegment());
                break;
            case ENTITY_LIST:
                builder.setTables(DbSchema.LEFT_OUTER_JOIN_STATEMENT);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ItemEntities.SORT_ORDER_DEFAULT;
                }
                useAuthorityUri = true;
                break;
            case ENTITY_ID:
                builder.setTables(DbSchema.LEFT_OUTER_JOIN_STATEMENT);
                // limit query to one row at most:
                builder.appendWhere(DbSchema.TBL_ITEMS + "." + Items._ID + " = " + uri.getLastPathSegment());
                useAuthorityUri = true;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // if you like you can log the query
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            logQuery(builder,  projection, selection, sortOrder);
        }
        else {
            logQueryDeprecated(builder, projection, selection, sortOrder);
        }
        Cursor cursor = builder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);
        // if we want to be notified of any changes:
        if (useAuthorityUri) {
            cursor.setNotificationUri(getContext().getContentResolver(), LentItemsContract.CONTENT_URI);
        }
        else {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
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

        long id = mDbDAO.addNewRequest(values);

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
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}
