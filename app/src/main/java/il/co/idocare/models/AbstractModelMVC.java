package il.co.idocare.models;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.handlermessaging.HandlerMessagingMaster;
import il.co.idocare.handlermessaging.HandlerMessagingSlave;

/**
 * This is an abstract implementation of ModelMVC interface which provides some convenience logic
 * specific to the app<br>
 * MVC Models of this app should extend this class.
 */
public abstract class AbstractModelMVC implements
        ModelMVC {


}
