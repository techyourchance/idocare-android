package il.co.idocare.contentproviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * Data Access Object for the underlying SQLite database. This class provides some convenience
 * methods for DB management, while abstracting out implementation details.
 * TODO: review synchronization and data validation requirements
 */
public class SQLiteWrapper {


    private static final String LOG_TAG = SQLiteWrapper.class.getSimpleName();


    private final IdcSQLiteOpenHelper mHelper;


    public SQLiteWrapper(IdcSQLiteOpenHelper helper) {
        mHelper = helper;
    }


    public long addNewRequest(ContentValues contentValues) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        // TODO: make sure that the added data is verified beforehand!

        long id = db.insert(IdcSQLiteOpenHelper.REQUESTS_TABLE_NAME, null, contentValues);

        return id;
    }


    /**
     * Query Requests table
     * @return cursor containing query results
     */
    public Cursor queryRequests(String[] projection, String selection, String[] selectionArgs,
                                String groupBy, String having, String sortOrder) {
        Cursor cursor = mHelper.getReadableDatabase().query(
                IdcSQLiteOpenHelper.REQUESTS_TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                groupBy,
                having,
                sortOrder);

        return cursor;
    }

    /**
     * Update entries in Requests table
     * @return the number of rows affected
     */
    public int updateRequests(ContentValues values, String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().update(IdcSQLiteOpenHelper.REQUESTS_TABLE_NAME, values,
                selection, selectionArgs);
    }


    /**
     * Delete entries from Requests table
     * @return the number of rows affected
     */
    public int deleteRequests(String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().delete(IdcSQLiteOpenHelper.REQUESTS_TABLE_NAME,
                selection, selectionArgs);
    }



    public long addNewUser(ContentValues contentValues) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        // TODO: make sure that the added data is verified beforehand!

        long id = db.insert(IdcSQLiteOpenHelper.USERS_TABLE_NAME, null, contentValues);

        return id;
    }


    /**
     * Query Users table
     * @return cursor containing query results
     */
    public Cursor queryUsers(String[] projection, String selection, String[] selectionArgs,
                                String groupBy, String having, String sortOrder) {
        Cursor cursor = mHelper.getReadableDatabase().query(
                IdcSQLiteOpenHelper.USERS_TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                groupBy,
                having,
                sortOrder);

        return cursor;
    }

    /**
     * Update entries in Users table
     * @return the number of rows affected
     */
    public int updateUsers(ContentValues values, String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().update(IdcSQLiteOpenHelper.USERS_TABLE_NAME, values,
                selection, selectionArgs);
    }


    /**
     * Delete entries from Users table
     * @return the number of rows affected
     */
    public int deleteUsers(String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().delete(IdcSQLiteOpenHelper.USERS_TABLE_NAME,
                selection, selectionArgs);
    }


    public Cursor queryUniqueUserIds(String[] projection, String selection, String[] selectionArgs,
                                     Object o, Object o1, String sortOrder) {

        String query = "SELECT " + IDoCareContract.Requests.COL_CREATED_BY + " AS "
                + IDoCareContract.UniqueUserIds.COL_USER_ID  + " FROM " + IdcSQLiteOpenHelper.REQUESTS_TABLE_NAME
                + " UNION "
                + " SELECT " + IDoCareContract.Requests.COL_PICKED_UP_BY + " AS "
                + IDoCareContract.UniqueUserIds.COL_USER_ID + " FROM " + IdcSQLiteOpenHelper.REQUESTS_TABLE_NAME
                + " WHERE " + IDoCareContract.Requests.COL_PICKED_UP_BY + " IS NOT NULL "
                + " UNION "
                + " SELECT " + IDoCareContract.Requests.COL_CLOSED_BY + " AS "
                + IDoCareContract.UniqueUserIds.COL_USER_ID + " FROM " + IdcSQLiteOpenHelper.REQUESTS_TABLE_NAME
                + " WHERE " + IDoCareContract.Requests.COL_CLOSED_BY + " IS NOT NULL ";

        return mHelper.getReadableDatabase().rawQuery(query, null);
    }


    public long addNewUserAction(ContentValues contentValues) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        // TODO: make sure that the added data is verified beforehand!

        long id = db.insert(IdcSQLiteOpenHelper.USER_ACTIONS_TABLE_NAME, null, contentValues);

        return id;
    }

    /**
     * Query UserActions table
     * @return cursor containing query results
     */
    public Cursor queryUserActions(String[] projection, String selection, String[] selectionArgs,
                                String groupBy, String having, String sortOrder) {
        Cursor cursor = mHelper.getReadableDatabase().query(
                IdcSQLiteOpenHelper.USER_ACTIONS_TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                groupBy,
                having,
                sortOrder);

        return cursor;
    }


    /**
     * Update entries in UserActions table
     * @return the number of rows affected
     */
    public int updateUserActions(ContentValues values, String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().update(IdcSQLiteOpenHelper.USER_ACTIONS_TABLE_NAME, values,
                selection, selectionArgs);
    }


    /**
     * Delete entries from UserActions table
     * @return the number of rows affected
     */
    public int deleteUserActions(String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().delete(IdcSQLiteOpenHelper.USER_ACTIONS_TABLE_NAME,
                selection, selectionArgs);
    }


    public long addNewTempIdMapping(ContentValues contentValues) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        long id = db.insert(IdcSQLiteOpenHelper.TEMP_ID_MAPPINGS_TABLE_NAME, null, contentValues);

        return id;
    }

    /**
     * Query TempIdMappings table
     * @return cursor containing query results
     */
    public Cursor queryTempIdMappings(String[] projection, String selection, String[] selectionArgs,
                                   String groupBy, String having, String sortOrder) {
        Cursor cursor = mHelper.getReadableDatabase().query(
                IdcSQLiteOpenHelper.TEMP_ID_MAPPINGS_TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                groupBy,
                having,
                sortOrder);

        return cursor;
    }


    /**
     * Update entries in TempIdMappings table
     * @return the number of rows affected
     */
    public int updateTempIdMappings(ContentValues values, String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().update(IdcSQLiteOpenHelper.TEMP_ID_MAPPINGS_TABLE_NAME,
                values, selection, selectionArgs);
    }


    /**
     * Delete entries from TempIdMappings table
     * @return the number of rows affected
     */
    public int deleteTempIdMappings(String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().delete(IdcSQLiteOpenHelper.TEMP_ID_MAPPINGS_TABLE_NAME,
                selection, selectionArgs);
    }


}