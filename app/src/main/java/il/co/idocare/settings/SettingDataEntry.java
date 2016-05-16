package il.co.idocare.settings;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * This class represents a single data entry stored in app's settings storage.
 * @param <SETTING_CLASS> the actual class of data represented by this object
 */
public class SettingDataEntry<SETTING_CLASS> {

    @NonNull private final SharedPreferences mSharedPreferences;
    @NonNull private final String mSettingKey;
    private SETTING_CLASS mDefValue;
    private SharedPrefsAdapter<SETTING_CLASS> mSharedPrefsAdapter;

    protected SettingDataEntry(@NonNull SharedPreferences sharedPreferences,
                               @NonNull String settingKey,
                               SETTING_CLASS defValue,
                               Class<SETTING_CLASS> settingClass) {
        mSharedPreferences = sharedPreferences;
        mSettingKey = settingKey;
        mDefValue = defValue;
        mSharedPrefsAdapter = getSharedPrefsAdapter(settingClass);
    }

    public SETTING_CLASS getValue() {
        return mSharedPrefsAdapter.getValue();
    }

    public void setValue(SETTING_CLASS newValue) {
        mSharedPrefsAdapter.setValue(newValue);
    }


    private SharedPrefsAdapter getSharedPrefsAdapter(Class settingClass) {
        if (settingClass == Boolean.class) {
            return new BooleanSharedPrefsAdapter();
        } else {
            throw new IllegalArgumentException(
                    "setting class '" + settingClass + "' is not currently supported; " +
                            "please add a respective adapter in order to make use of this class");
        }
    }

    private interface SharedPrefsAdapter<ENTRY_TYPE> {
        ENTRY_TYPE getValue();
        void setValue(ENTRY_TYPE newValue);
    }

    private class BooleanSharedPrefsAdapter implements SharedPrefsAdapter<Boolean> {
        @Override
        public Boolean getValue() {
            return mSharedPreferences.getBoolean(mSettingKey, (Boolean)mDefValue);
        }

        @Override
        public void setValue(Boolean newValue) {
            mSharedPreferences.edit().putBoolean(mSettingKey, newValue).commit();
        }
    }
}
