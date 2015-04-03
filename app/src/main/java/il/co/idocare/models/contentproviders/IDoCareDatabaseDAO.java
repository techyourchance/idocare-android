package il.co.idocare.models.contentproviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


/**
 * Data Access Object for the underlying SQLite database. This class provides some convenience
 * methods for DB management, while abstracting out implementation details.
 * TODO: review synchronization and data validation requirements
 */
public class IDoCareDatabaseDAO {


    private static final String LOG_TAG = "IDoCareDatabaseDAO";


    private Context mContext;
    private IDoCareSQLOpenHelper mHelper;


    public IDoCareDatabaseDAO(Context context) {
        mContext = context;
        mHelper = new IDoCareSQLOpenHelper(context);
    }


    public long addNewRequest(ContentValues contentValues) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        // TODO: make sure that the added data is verified beforehand!

        long id = db.insert(IDoCareSQLOpenHelper.REQUESTS_TABLE_NAME, null, contentValues);

        if (id >= 0) {
            notifyDataChanged(); // TODO: do we need this call here?
        }

        return id;
    }

//
//    private  ArrayList<HistoryFragment.HistoryEntry> getHistory(String selection,
//                                                                            String orderBy) {
//        SQLiteDatabase db = mHelper.getReadableDatabase();
//
//        String[] columns = {CustomSQLOpenHelper.UID, CustomSQLOpenHelper.LEFT_OR_RIGHT,
//                CustomSQLOpenHelper.DATE, CustomSQLOpenHelper.DURATION, CustomSQLOpenHelper.AMOUNT,
//                CustomSQLOpenHelper.DELTA_ROLL, CustomSQLOpenHelper.DELTA_TILT};
//
//        Cursor cursor = db.query(CustomSQLOpenHelper.TABLE_NAME, columns, selection,
//                null, null, null, orderBy);
//
//        ArrayList<HistoryFragment.HistoryEntry> history = new ArrayList<HistoryFragment.HistoryEntry>();
//
//
//        while (cursor.moveToNext()) {
//            int index0 = cursor.getColumnIndex(CustomSQLOpenHelper.UID);
//            int index1 = cursor.getColumnIndex(CustomSQLOpenHelper.LEFT_OR_RIGHT);
//            int index2 = cursor.getColumnIndex(CustomSQLOpenHelper.DATE);
//            int index3 = cursor.getColumnIndex(CustomSQLOpenHelper.DURATION);
//            int index4 = cursor.getColumnIndex(CustomSQLOpenHelper.AMOUNT);
//            int index5 = cursor.getColumnIndex(CustomSQLOpenHelper.DELTA_ROLL);
//            int index6 = cursor.getColumnIndex(CustomSQLOpenHelper.DELTA_TILT);
//
//            int uid = cursor.getInt(index0);
//            String leftOrRight = cursor.getString(index1);
//            String dateString = cursor.getString(index2);
//            int duration = cursor.getInt(index3);
//            int amount = cursor.getInt(index4);
//            int delta_roll = cursor.getInt(index5);
//            int delta_tilt = cursor.getInt(index6);
//
//            history.add(new HistoryFragment.HistoryEntry(uid, leftOrRight, dateString,
//                    duration, amount, delta_roll, delta_tilt));
//
//        }
//
//        cursor.close();
//
//        return history;
//    }

    public synchronized void clearRequestsTable() {
        mHelper.clearRequestsTable();
        notifyDataChanged();
    }

    private void notifyDataChanged() {
        // TODO: do we need this method? It might be used for e.g. uploading changes to server
    }

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
     * Custom implementation of SQLiteOpenHelper.
     */
    private static class IDoCareSQLOpenHelper extends SQLiteOpenHelper {

        private static final String LOG_TAG = "IDoCareSQLOpenHelper";

        private static final int DATABASE_VERSION = 1;

        private static final String DATABASE_NAME = "idocare_db";
        private static final String REQUESTS_TABLE_NAME = "requests_tbl";
        private static final String UID = BaseColumns._ID;

        private static final String CREATE_REQUESTS_TABLE = "CREATE TABLE " + REQUESTS_TABLE_NAME
                + " ( " + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + IDoCareContract.Requests.REQUEST_ID + " INTEGER, "
                + IDoCareContract.Requests.CREATED_BY + " INTEGER, "
                + IDoCareContract.Requests.PICKED_UP_BY + " INTEGER, "
                + IDoCareContract.Requests.CREATED_AT + " DATETIME, "
                + IDoCareContract.Requests.PICKED_UP_AT + " DATETIME, "
                + IDoCareContract.Requests.CLOSED_AT + " DATETIME, "
                + IDoCareContract.Requests.CREATED_COMMENT + " VARCHAR(2000), "
                + IDoCareContract.Requests.CLOSED_COMMENT + " VARCHAR(2000), "
                + IDoCareContract.Requests.LATITUDE + " REAL, "
                + IDoCareContract.Requests.LONGITUDE + " REAL, "
                + IDoCareContract.Requests.CREATED_PICTURES + " VARCHAR(1000), "
                + IDoCareContract.Requests.CLOSED_PICTURES + " VARCHAR(1000), "
                + IDoCareContract.Requests.POLLUTION_LEVEL + " INTEGER, "
                + IDoCareContract.Requests.POLLUTION_LEVEL + " INTEGER, "
                + IDoCareContract.Requests.CLOSED_BY + " INTEGER, "
                + IDoCareContract.Requests.CREATED_REPUTATION + " INTEGER, "
                + IDoCareContract.Requests.CLOSED_REPUTATION + " INTEGER "
                + " ); ";

        private static final String DROP_REQUESTS_TABLE =
                "DROP TABLE IF EXISTS " + REQUESTS_TABLE_NAME;


        public IDoCareSQLOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(LOG_TAG, "onCreate is called");
            try {
                db.execSQL(CREATE_REQUESTS_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(LOG_TAG, "onUpgrade is called. Old ver: " + oldVersion + " new ver: " + newVersion);
            try {
                db.execSQL(DROP_REQUESTS_TABLE);
                onCreate(db);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void clearRequestsTable() {
            Log.d(LOG_TAG, "Destroying and re-creating the table: " + REQUESTS_TABLE_NAME);
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + REQUESTS_TABLE_NAME);
            db.execSQL(CREATE_REQUESTS_TABLE);
        }

    }

}