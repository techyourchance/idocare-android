package il.co.idocarecore.common.settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SettingDataEntry<T> {

    private final Set<SettingDataEntryChangeListener<T>> mListeners = new HashSet<>();

    protected final String key;
    protected final T defaultValue;

    public SettingDataEntry(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public abstract T getValue();
    public abstract void setValue(T value);
    public abstract void remove();



    public void registerListener(SettingDataEntryChangeListener<T> listener){
        synchronized (mListeners) {
            boolean modified = mListeners.add(listener);
            if (modified && mListeners.size() == 1) {
                onFirstListenerRegistered();
            }
        }
    }

    public void unregisterListener(SettingDataEntryChangeListener<T> listener){
        synchronized (mListeners) {
            boolean modified = mListeners.remove(listener);
            if (modified && mListeners.isEmpty()) {
                onLastListenerUnregistered();
            }
        }
    }

    protected void onLastListenerUnregistered(){
    }

    protected void onFirstListenerRegistered(){
    }

    protected void notifyListeners(String key, T value){
        final List<SettingDataEntryChangeListener<T>> listenersCopy;
        synchronized (mListeners) {
            listenersCopy = new ArrayList<>(mListeners);
        }

        for (SettingDataEntryChangeListener<T> listener : listenersCopy){
            listener.onValueChanged(this, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettingDataEntry<?> that = (SettingDataEntry<?>) o;

        return key.equals(that.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
