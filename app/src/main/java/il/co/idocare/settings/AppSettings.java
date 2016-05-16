package il.co.idocare.settings;

import android.content.SharedPreferences;

import il.co.idocare.settings.SettingDataEntry;

/**
 * This is the "centralized storage" of application's settings
 */
public class AppSettings {

    private static final String KEY_IS_FIRST_APP_LAUNCH = "KEY_IS_FIRST_APP_LAUNCH";

    private SharedPreferences mSettingsSharedPrefs;

    public AppSettings(SharedPreferences settingsSharedPrefs) {
        mSettingsSharedPrefs = settingsSharedPrefs;
    }

    public SettingDataEntry<Boolean> isFirstAppLaunch() {
        return new SettingDataEntry<>(mSettingsSharedPrefs,
                KEY_IS_FIRST_APP_LAUNCH, true, Boolean.class);
    }


}
