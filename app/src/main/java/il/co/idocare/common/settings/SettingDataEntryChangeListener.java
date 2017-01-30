package il.co.idocare.common.settings;


public interface SettingDataEntryChangeListener<T> {

    void onValueChanged(SettingDataEntry settingDataEntry, T value);

}
