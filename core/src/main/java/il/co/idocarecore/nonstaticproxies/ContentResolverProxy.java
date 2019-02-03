package il.co.idocarecore.nonstaticproxies;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

/**
 * Non-static proxy for ContentResolver's static functionality
 */
public class ContentResolverProxy {


    public void setIsSyncable (Account account, String authority, int syncable) {
        ContentResolver.setIsSyncable(account, authority, syncable);
    }

    public void setSyncAutomatically (Account account, String authority, boolean sync) {
        ContentResolver.setSyncAutomatically(account, authority, sync);
    }

    public void requestSync (Account account, String authority, Bundle extras) {
        ContentResolver.requestSync(account, authority, extras);
    }
}
