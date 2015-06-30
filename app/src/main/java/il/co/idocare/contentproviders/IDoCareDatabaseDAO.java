package il.co.idocare.contentproviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Data Access Object for the underlying SQLite database. This class provides some convenience
 * methods for DB management, while abstracting out implementation details.
 * TODO: review synchronization and data validation requirements
 */
public class IDoCareDatabaseDAO {


    private static final String LOG_TAG = IDoCareDatabaseDAO.class.getSimpleName();


    private IDoCareSQLOpenHelper mHelper;


    public IDoCareDatabaseDAO(Context context) {
        mHelper = new IDoCareSQLOpenHelper(context);
    }


    public long addNewRequest(ContentValues contentValues) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        // TODO: make sure that the added data is verified beforehand!

        long id = db.insert(IDoCareSQLOpenHelper.REQUESTS_TABLE_NAME, null, contentValues);

        return id;
    }


    /**
     * Query Requests table
     * @return cursor containing query results
     */
    public Cursor queryRequests(String[] projection, String selection, String[] selectionArgs,
                                String groupBy, String having, String sortOrder) {
        Cursor cursor = mHelper.getReadableDatabase().query(
                IDoCareSQLOpenHelper.REQUESTS_TABLE_NAME,
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
        return mHelper.getWritableDatabase().update(IDoCareSQLOpenHelper.REQUESTS_TABLE_NAME, values,
                selection, selectionArgs);
    }


    /**
     * Delete entries from Requests table
     * @return the number of rows affected
     */
    public int deleteRequests(String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().delete(IDoCareSQLOpenHelper.REQUESTS_TABLE_NAME,
                selection, selectionArgs);
    }


    public long addNewUserAction(ContentValues contentValues) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        // TODO: make sure that the added data is verified beforehand!

        long id = db.insert(IDoCareSQLOpenHelper.USER_ACTIONS_TABLE_NAME, null, contentValues);

        return id;
    }

    /**
     * Query UserActions table
     * @return cursor containing query results
     */
    public Cursor queryUserActions(String[] projection, String selection, String[] selectionArgs,
                                String groupBy, String having, String sortOrder) {
        Cursor cursor = mHelper.getReadableDatabase().query(
                IDoCareSQLOpenHelper.USER_ACTIONS_TABLE_NAME,
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
        return mHelper.getWritableDatabase().update(IDoCareSQLOpenHelper.USER_ACTIONS_TABLE_NAME, values,
                selection, selectionArgs);
    }


    /**
     * Delete entries from UserActions table
     * @return the number of rows affected
     */
    public int deleteUserActions(String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().delete(IDoCareSQLOpenHelper.USER_ACTIONS_TABLE_NAME,
                selection, selectionArgs);
    }


    public long addNewTempIdMapping(ContentValues contentValues) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        long id = db.insert(IDoCareSQLOpenHelper.TEMP_ID_MAPPINGS_TABLE_NAME, null, contentValues);

        return id;
    }

    /**
     * Query TempIdMappings table
     * @return cursor containing query results
     */
    public Cursor queryTempIdMappings(String[] projection, String selection, String[] selectionArgs,
                                   String groupBy, String having, String sortOrder) {
        Cursor cursor = mHelper.getReadableDatabase().query(
                IDoCareSQLOpenHelper.TEMP_ID_MAPPINGS_TABLE_NAME,
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
        return mHelper.getWritableDatabase().update(IDoCareSQLOpenHelper.TEMP_ID_MAPPINGS_TABLE_NAME,
                values, selection, selectionArgs);
    }


    /**
     * Delete entries from TempIdMappings table
     * @return the number of rows affected
     */
    public int deleteTempIdMappings(String selection, String[] selectionArgs) {
        return mHelper.getWritableDatabase().delete(IDoCareSQLOpenHelper.TEMP_ID_MAPPINGS_TABLE_NAME,
                selection, selectionArgs);
    }

    /**
     * Custom implementation of SQLiteOpenHelper.
     */
    private static class IDoCareSQLOpenHelper extends SQLiteOpenHelper {

        private static final String LOG_TAG = IDoCareSQLOpenHelper.class.getSimpleName();

        private static final int DATABASE_VERSION = 1;

        private static final String DATABASE_NAME = "idocare_db";

        private static final String REQUESTS_TABLE_NAME = "requests_tbl";
        private static final String USER_ACTIONS_TABLE_NAME = "user_actions_tbl";
        private static final String TEMP_ID_MAPPINGS_TABLE_NAME = "temp_id_mappings_tbl";

        private static final String CREATE_REQUESTS_TABLE =
                "CREATE TABLE " + REQUESTS_TABLE_NAME + " ( "
                + IDoCareContract.Requests._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + IDoCareContract.Requests.COL_REQUEST_ID + " INTEGER, "
                + IDoCareContract.Requests.COL_CREATED_BY + " INTEGER, "
                + IDoCareContract.Requests.COL_PICKED_UP_BY + " INTEGER, "
                + IDoCareContract.Requests.COL_CREATED_AT + " DATETIME, "
                + IDoCareContract.Requests.COL_PICKED_UP_AT + " DATETIME, "
                + IDoCareContract.Requests.COL_CLOSED_AT + " DATETIME, "
                + IDoCareContract.Requests.COL_CREATED_COMMENT + " VARCHAR(1000), "
                + IDoCareContract.Requests.COL_CLOSED_COMMENT + " VARCHAR(1000), "
                + IDoCareContract.Requests.COL_LATITUDE + " REAL, "
                + IDoCareContract.Requests.COL_LONGITUDE + " REAL, "
                + IDoCareContract.Requests.COL_CREATED_PICTURES + " VARCHAR(1000), "
                + IDoCareContract.Requests.COL_CLOSED_PICTURES + " VARCHAR(1000), "
                + IDoCareContract.Requests.COL_POLLUTION_LEVEL + " INTEGER, "
                + IDoCareContract.Requests.COL_CLOSED_BY + " INTEGER, "
                + IDoCareContract.Requests.COL_CREATED_REPUTATION + " INTEGER DEFAULT 0, "
                + IDoCareContract.Requests.COL_CLOSED_REPUTATION + " INTEGER DEFAULT 0, "
                + IDoCareContract.Requests.COL_MODIFIED_LOCALLY_FLAG + " INTEGER DEFAULT 0);";


        private static final String CREATE_USER_ACTIONS_TABLE =
                "CREATE TABLE " + USER_ACTIONS_TABLE_NAME + " ( "
                + IDoCareContract.UserActions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + IDoCareContract.UserActions.COL_TIMESTAMP + " INTEGER, "
                + IDoCareContract.UserActions.COL_ENTITY_TYPE + " VARCHAR(1000), "
                + IDoCareContract.UserActions.COL_ENTITY_ID + " INTEGER, "
                + IDoCareContract.UserActions.COL_ENTITY_PARAM + " VARCHAR(        1000), "
                + IDoCareContract.UserActions.COL_ACTION_TYPE + " VARCHAR(1000), "
                + IDoCareContract.UserActions.COL_ACTION_PARAM + " VARCHAR(1000), "
                + IDoCareContract.UserActions.COL_SERVER_RESPONSE_STATUS_CODE + " INTEGER DEFAULT 0, "
                + IDoCareContract.UserActions.COL_SERVER_RESPONSE_REASON_PHRASE + " VARCHAR(1000) DEFAULT '', "
                + IDoCareContract.UserActions.COL_SERVER_RESPONSE_ENTITY + " VARCHAR(10000) DEFAULT '' );";

        private static final String CREATE_TEMP_ID_MAPPINGS_TABLE =
                "CREATE TABLE " + TEMP_ID_MAPPINGS_TABLE_NAME + " ( "
                + IDoCareContract.TempIdMappings._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + IDoCareContract.TempIdMappings.COL_TEMP_ID + " INTEGER, "
                + IDoCareContract.TempIdMappings.COL_PERMANENT_ID + " INTEGER ); ";



        public IDoCareSQLOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(LOG_TAG, "onCreate is called");
            try {
                db.execSQL(CREATE_REQUESTS_TABLE);
                db.execSQL(CREATE_USER_ACTIONS_TABLE);
                db.execSQL(CREATE_TEMP_ID_MAPPINGS_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(LOG_TAG, "onUpgrade is called. Old ver: " + oldVersion + " new ver: " + newVersion);
            try {
                db.execSQL("DROP TABLE IF EXISTS " + REQUESTS_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + USER_ACTIONS_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + TEMP_ID_MAPPINGS_TABLE_NAME);
                onCreate(db);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

}