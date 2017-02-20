package il.co.idocare.contentproviders;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class IdcSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "IdcSQLiteOpenHelper";

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "idocare_db";

    /* pp */ static final String REQUESTS_TABLE_NAME = "requests_tbl";
    /* pp */ static final String USERS_TABLE_NAME = "users_tbl";
    /* pp */ static final String USER_ACTIONS_TABLE_NAME = "user_actions_tbl";
    /* pp */ static final String TEMP_ID_MAPPINGS_TABLE_NAME = "temp_id_mappings_tbl";

    private static final String CREATE_REQUESTS_TABLE =
            "CREATE TABLE " + REQUESTS_TABLE_NAME + " ( "
            + IDoCareContract.Requests._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IDoCareContract.Requests.COL_REQUEST_ID + " TEXT NOT NULL UNIQUE, "
            + IDoCareContract.Requests.COL_CREATED_BY + " TEXT NOT NULL, "
            + IDoCareContract.Requests.COL_PICKED_UP_BY + " INTEGER, "
            + IDoCareContract.Requests.COL_CREATED_AT + " DATETIME, "
            + IDoCareContract.Requests.COL_PICKED_UP_AT + " DATETIME, "
            + IDoCareContract.Requests.COL_CLOSED_AT + " DATETIME, "
            + IDoCareContract.Requests.COL_CREATED_COMMENT + " TEXT, "
            + IDoCareContract.Requests.COL_CLOSED_COMMENT + " TEXT, "
            + IDoCareContract.Requests.COL_LATITUDE + " REAL, "
            + IDoCareContract.Requests.COL_LONGITUDE + " REAL, "
            + IDoCareContract.Requests.COL_CREATED_PICTURES + " TEXT, "
            + IDoCareContract.Requests.COL_CLOSED_PICTURES + " TEXT, "
            + IDoCareContract.Requests.COL_POLLUTION_LEVEL + " INTEGER, "
            + IDoCareContract.Requests.COL_CLOSED_BY + " INTEGER, "
            + IDoCareContract.Requests.COL_CREATED_VOTES + " INTEGER DEFAULT 0, "
            + IDoCareContract.Requests.COL_CLOSED_VOTES + " INTEGER DEFAULT 0, "
            + IDoCareContract.Requests.COL_LOCATION + " TEXT );";


    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + USERS_TABLE_NAME + " ( "
            + IDoCareContract.Users._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IDoCareContract.Users.COL_USER_ID + " TEXT NOT NULL UNIQUE, "
            + IDoCareContract.Users.COL_USER_NICKNAME + " TEXT, "
            + IDoCareContract.Users.COL_USER_FIRST_NAME + " TEXT, "
            + IDoCareContract.Users.COL_USER_LAST_NAME + " TEXT, "
            + IDoCareContract.Users.COL_USER_REPUTATION + " INTEGER, "
            + IDoCareContract.Users.COL_USER_PICTURE + " TEXT ); ";


    private static final String CREATE_USER_ACTIONS_TABLE =
            "CREATE TABLE " + USER_ACTIONS_TABLE_NAME + " ( "
            + IDoCareContract.UserActions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IDoCareContract.UserActions.COL_TIMESTAMP + " INTEGER, "
            + IDoCareContract.UserActions.COL_ENTITY_TYPE + " TEXT, "
            + IDoCareContract.UserActions.COL_ENTITY_ID + " INTEGER, "
            + IDoCareContract.UserActions.COL_ENTITY_PARAM + " TEXT, "
            + IDoCareContract.UserActions.COL_ACTION_TYPE + " TEXT, "
            + IDoCareContract.UserActions.COL_ACTION_PARAM + " TEXT, "
            + IDoCareContract.UserActions.COL_SERVER_RESPONSE_STATUS_CODE + " INTEGER DEFAULT 0, "
            + IDoCareContract.UserActions.COL_SERVER_RESPONSE_REASON_PHRASE + " TEXT DEFAULT '', "
            + IDoCareContract.UserActions.COL_SERVER_RESPONSE_ENTITY + " TEXT DEFAULT '' );";


    private static final String CREATE_TEMP_ID_MAPPINGS_TABLE =
            "CREATE TABLE " + TEMP_ID_MAPPINGS_TABLE_NAME + " ( "
            + IDoCareContract.TempIdMappings._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IDoCareContract.TempIdMappings.COL_TEMP_ID + " TEXT NOT NULL UNIQUE, "
            + IDoCareContract.TempIdMappings.COL_PERMANENT_ID + " TEXT NOT NULL UNIQUE ); ";


    // ---------------------------------------------------------------------------------------------
    //
    // Singleton management

    private static IdcSQLiteOpenHelper sInstance;

    public static IdcSQLiteOpenHelper getInstance(Context context) {
        synchronized (IdcSQLiteOpenHelper.class) {
            if (sInstance == null) {
                sInstance = new IdcSQLiteOpenHelper(context.getApplicationContext());
            }
            return sInstance;
        }
    }

    //
    // ---------------------------------------------------------------------------------------------

    private IdcSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, "onCreate is called");
        try {
            db.execSQL(CREATE_REQUESTS_TABLE);
            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_USER_ACTIONS_TABLE);
            db.execSQL(CREATE_TEMP_ID_MAPPINGS_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "onUpgrade is called. Old ver: " + oldVersion + " new ver: " + newVersion);
        try {
            db.execSQL("DROP TABLE IF EXISTS " + REQUESTS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + USER_ACTIONS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TEMP_ID_MAPPINGS_TABLE_NAME);
            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
