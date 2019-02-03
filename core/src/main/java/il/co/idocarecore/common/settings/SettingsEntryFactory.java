package il.co.idocarecore.common.settings;

public abstract class SettingsEntryFactory {

     public abstract <T>SettingDataEntry<T> getDataEntry(final Class<T> clazz, final String key, final T defaultValue);

     public  <T>SettingDataEntry<T> getDataEntry(final Class<T> clazz, final String key) {
          return getDataEntry(clazz, key, null);
     }
}
