package il.co.idocare.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import il.co.idocare.models.ModelMVC;

/**
 * MVC view interface.
 * MVC view is a "dumb" component used for presenting information to the user.<br>
 * Please note that MVC view is not the same as Android View - MVC view will usually wrap one or
 * more Android View's while adding logic for communication with MVC Controller and MVC Model.
 */
public interface ViewMVC {

    /**
     * Get the root Android View which is used internally by this MVC View for presenting data
     * to the user.<br>
     * The returned Android View might be used by an MVC Controller in order to query or alter the
     * properties of either the root Android View itself, or any of its child Android View's.
     * @return root Android View of this MVC View
     */
    public View getRootView();

    /**
     * This method aggregates all the information about the state of this MVC View into Bundle
     * object.
     * @return Bundle containing the state of this MVC View
     */
    public Bundle getViewState();

}
