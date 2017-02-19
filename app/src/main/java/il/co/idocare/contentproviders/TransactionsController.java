package il.co.idocare.contentproviders;

import android.database.sqlite.SQLiteDatabase;

/**
 * This class can be used in order to issue transactions against the database. It should be used in
 * conjunction with ContentResolver.<br>
 * The need for this class is "abstraction leak" - ContentProvider abstraction does not suffice here.
 */
public class TransactionsController {

    private final SQLiteDatabase mDatabase;

    public TransactionsController(IdcSQLiteOpenHelper sqLiteOpenHelper) {
        mDatabase = sqLiteOpenHelper.getWritableDatabase();
    }

    /**
     * Begin database transaction in EXCLUSIVE mode. Transactions can be nested.
     * @see SQLiteDatabase#beginTransaction()
     */
    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    /**
     * Marks the current transaction as successful.
     * @see SQLiteDatabase#setTransactionSuccessful()
     */
    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    /**
     * End transaction.
     * @see SQLiteDatabase#endTransaction()
     */
    public void endTransaction() {
        mDatabase.endTransaction();
    }
}
