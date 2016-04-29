package il.co.idocare.localcachedata;

/**
 * This interface should be implemented by classes that should be notified when locally cached data
 * changes
 */
public interface LocalCacheDataChangeListener {

    /**
     * Will be called on UI thread when locally cached data changes
     */
    void onDataChanged();

}
