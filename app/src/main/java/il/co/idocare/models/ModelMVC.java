package il.co.idocare.models;

import android.os.Handler;

import java.util.List;

/**
 * MVC Model interface.
 * MVC Model's function is to obtain, store and update the data associated with the application.<br>
 * Our architecture supports nested MVC Models, i.e. MVC Models which contain other MVC Models.
 * @param <T> type of data stored in this MVC Model
 */
public interface ModelMVC<T> {

    /**
     * This method allows to check whether this MVC Model has child MVC Models.
     * @return true if this MVC Model has child MVC Models, false otherwise
     */
    public boolean hasChildModels();

    /**
     * Get a list of child MVC Models of this MVC Model
     * @return a list of child MVC Models or null if this MVC Model has no child MVC Models
     */
    public List<ModelMVC> getChildModels();

    /**
     * MVC Models might have ID's associated with them.
     * @return true if this MVC Model has an ID
     */
    public boolean hasModelId();

    /**
     * Get ID of this MVC Model
     * @return MVC Model's ID or 0 if this MVC Model has no ID
     */
    public long getModelID();

    /**
     * Get the contents of this MVC Model
     * @return list of elements constituting the data of this MVC Model
     */
    public List<T> getData();

    /**
     * Add data object to the contents of this MVC Model
     * @param data the object to be added
     */
    public void addDataObject(T data);

    /**
     * Remove data object from the model
     * @param data the object to be removed
     */
    public void removeDataObject(T data);


}
