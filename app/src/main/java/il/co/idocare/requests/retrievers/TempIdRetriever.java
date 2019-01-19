package il.co.idocare.requests.retrievers;

import android.content.ContentResolver;
import android.database.Cursor;
import androidx.annotation.Nullable;

import il.co.idocare.contentproviders.IDoCareContract;

public class TempIdRetriever {

    private final ContentResolver mContentResolver;

    public TempIdRetriever(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    @Nullable
    public String getNewIdForTempId(String oldId) {

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(
                    IDoCareContract.TempIdMappings.CONTENT_URI,
                    IDoCareContract.TempIdMappings.PROJECTION_ALL,
                    IDoCareContract.TempIdMappings.COL_TEMP_ID + " = ?",
                    new String[] {oldId},
                    IDoCareContract.TempIdMappings.SORT_ORDER_DEFAULT
            );


            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(IDoCareContract.TempIdMappings.COL_PERMANENT_ID));
            } else {
                return null;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }


}
