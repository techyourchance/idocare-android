package il.co.idocare.requests.cachers;

import android.content.ContentResolver;
import android.content.ContentValues;

import il.co.idocare.contentproviders.IDoCareContract;

public class TempIdCacher {

    private final ContentResolver mContentResolver;

    public TempIdCacher(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    public void cacheTempIdMapping(String oldId, String newId) {
        ContentValues contentValues = new ContentValues(2);
        contentValues.put(IDoCareContract.TempIdMappings.COL_TEMP_ID, oldId);
        contentValues.put(IDoCareContract.TempIdMappings.COL_PERMANENT_ID, newId);

        mContentResolver.insert(IDoCareContract.TempIdMappings.CONTENT_URI, contentValues);
    }

    public void deleteTempIdMappingByNewId(String newId) {
        mContentResolver.delete(
                IDoCareContract.TempIdMappings.CONTENT_URI,
                IDoCareContract.TempIdMappings.COL_PERMANENT_ID + " = ?",
                new String[] {newId}
        );
    }

    public void deleteTempIdMappingByOldId(String oldId) {
        mContentResolver.delete(
                IDoCareContract.TempIdMappings.CONTENT_URI,
                IDoCareContract.TempIdMappings.COL_TEMP_ID + " = ?",
                new String[] {oldId}
        );
    }
}
