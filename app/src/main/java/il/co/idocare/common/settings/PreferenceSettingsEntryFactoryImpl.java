package il.co.idocare.common.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

public class PreferenceSettingsEntryFactoryImpl extends SettingsEntryFactory {

    private final SharedPreferences mSharedPreferences;

    public PreferenceSettingsEntryFactoryImpl(SharedPreferences sharedPreferences){
        mSharedPreferences = sharedPreferences;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> SettingDataEntry<T> getDataEntry(Class<T> clazz, String key, @Nullable T defaultValue){

        if (clazz == Boolean.class) return (SettingDataEntry<T>) new BooleanEntry(mSharedPreferences, key, (Boolean) defaultValue);
        if (clazz == String.class) return (SettingDataEntry<T>) new StringEntry(mSharedPreferences, key, (String) defaultValue);
        if (clazz == Integer.class) return (SettingDataEntry<T>) new IntegerEntry(mSharedPreferences, key, (Integer) defaultValue);
        if (clazz == Long.class) return (SettingDataEntry<T>) new LongEntry(mSharedPreferences, key, (Long) defaultValue);
        if (clazz == Double.class) return (SettingDataEntry<T>) new DoubleEntry(mSharedPreferences, key, (Double) defaultValue);

        throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " is not supported yet");

    }


    private abstract class PreferenceDataEntry<T> extends SettingDataEntry<T> implements SharedPreferences.OnSharedPreferenceChangeListener {

        protected final SharedPreferences preferences;

        protected PreferenceDataEntry(SharedPreferences preferences, String key, T defaultValue){
            super(key, defaultValue);
            this.preferences = preferences;
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public void remove(){
            preferences.edit().remove(key).commit();
        }


        @Override
        protected void onFirstListenerRegistered() {
            preferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        protected void onLastListenerUnregistered() {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            notifyListeners(key, getValue());
        }

    }


    // ******************** Boolean *******************

    private class BooleanEntry extends PreferenceDataEntry<Boolean>{

        protected BooleanEntry(SharedPreferences preferences, String key, Boolean defaultValue) {
            super(preferences, key, defaultValue);
        }

        @Override
        public Boolean getValue() {
            return preferences.getBoolean(key, defaultValue == null ? false : defaultValue);
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public void setValue(Boolean value) {
            preferences.edit().putBoolean(key, value == null ? false : value).commit();
        }
    }

    // ******************** String *******************

    private class StringEntry extends PreferenceDataEntry<String>{

        protected StringEntry(SharedPreferences preferences, String key, String defaultValue) {
            super(preferences, key, defaultValue);
        }

        @Override
        public String getValue() {
            return mSharedPreferences.getString(key, defaultValue);
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public void setValue(String value) {
            preferences.edit().putString(key, value).commit();
        }
    }

    // ******************** Integer *******************

    private class IntegerEntry extends PreferenceDataEntry<Integer>{

        protected IntegerEntry(SharedPreferences preferences, String key, Integer defaultValue) {
            super(preferences, key, defaultValue);
        }

        @Override
        public Integer getValue() {
            return preferences.getInt(key, defaultValue == null ? 0: defaultValue);
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public void setValue(Integer value) {
            preferences.edit().putInt(key, value == null ? 0 : value).commit();
        }
    }

    // ******************** Float *******************

    private class FloatEntry extends PreferenceDataEntry<Float>{

        protected FloatEntry(SharedPreferences preferences, String key, Float defaultValue) {
            super(preferences, key, defaultValue);
        }

        @Override
        public Float getValue() {
            return preferences.getFloat(key, defaultValue == null ? 0f : defaultValue);
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public void setValue(Float value) {
            preferences.edit().putFloat(key, value == null ? 0 : value).commit();
        }
    }

    // ******************** Long *******************

    private class LongEntry extends PreferenceDataEntry<Long>{

        protected LongEntry(SharedPreferences preferences, String key, Long defaultValue) {
            super(preferences, key, defaultValue);
        }

        @Override
        public Long getValue() {
            return preferences.getLong(key, defaultValue == null ? 0 : defaultValue);
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public void setValue(Long value) {
            preferences.edit().putLong(key, value == null ? 0 : value).commit();
        }
    }

    // ******************** Double *******************

    private class DoubleEntry extends PreferenceDataEntry<Double> {

        protected DoubleEntry(SharedPreferences preferences, String key, Double defaultValue) {
            super(preferences, key, defaultValue);
        }

        @Override
        public Double getValue() {
            final double defValD = defaultValue == null ? 0 : defaultValue;
            return Double.longBitsToDouble(preferences.getLong(key, Double.doubleToLongBits(defValD)));
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public void setValue(Double value) {
            final double valD = value == null ? 0 : value;
            preferences.edit().putLong(key, Double.doubleToLongBits(valD)).commit();
        }
    }
}
