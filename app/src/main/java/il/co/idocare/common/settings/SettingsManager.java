package il.co.idocare.common.settings;

/**
 * This class encapsulates various settings/configurations/data which affect functionality of
 * the app.
 */
public class SettingsManager {

    private static final String KEY_USER_EMAIL = "KEY_USER_EMAIL";
    private static final String KEY_USER_ID = "KEY_USER_ID";
    private static final String KEY_USER_AUTH_TOKEN = "KEY_USER_AUTH_TOKEN";
    private static final String KEY_USER_FB_ID = "KEY_USER_FB_ID";

    private static final String KEY_LOGIN_SKIPPED = "KEY_LOGIN_SKIPPED";

    private final SettingsEntryFactory mSettingsEntryFactory;


    public SettingsManager(SettingsEntryFactory settingsEntryFactory) {
        mSettingsEntryFactory = settingsEntryFactory;
    }

    public SettingDataEntry<String> userEmail() {
        return mSettingsEntryFactory.getDataEntry(String.class, KEY_USER_EMAIL, null);
    }

    public SettingDataEntry<String> userId() {
        return mSettingsEntryFactory.getDataEntry(String.class, KEY_USER_ID, null);
    }

    public SettingDataEntry<String> authToken() {
        return mSettingsEntryFactory.getDataEntry(String.class, KEY_USER_AUTH_TOKEN, null);
    }

    public SettingDataEntry<Boolean> loginSkipped() {
        return mSettingsEntryFactory.getDataEntry(Boolean.class, KEY_LOGIN_SKIPPED, false);
    }
}
