package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

import il.co.idocare.controllers.ControllerMVC;
import il.co.idocare.handlermessaging.HandlerMessagingMaster;
import il.co.idocare.handlermessaging.HandlerMessagingSlave;
import il.co.idocare.models.RequestsMVCModel;


/**
 * This is a wrapper to the standard Fragment class which adds some convenience logic specific
 * to the app.<br>
 * Fragments of this app should extend this class.
 */
public abstract class AbstractFragment extends Fragment implements
        ControllerMVC,
        HandlerMessagingMaster,
        HandlerMessagingSlave {

    IDoCareFragmentCallback mCallback;

    HandlerThread mInboxHandlerThread;
    Handler mInboxHandler;
    final List<Handler> mOutboxHandlers = new ArrayList<Handler>();

    /**
     * Top level fragment =  a fragment which does not have parent in navigation hierarchy
     * @return true if this fragment is a "top level fragment"
     */
    public abstract boolean isTopLevelFragment();

    /**
     * If {@link #isTopLevelFragment() isTopLevelFragment} returns false, then this method should
     * be used to obtain the parent of this fragment in the navigation hierarchy.<br>
     * This information might be used, for example, when navigating to a "non-top-level fragment"
     * via Navigation Drawer - in this case the UP button on Action Bar should bring the user to the
     * fragment returned by this method (which might be different from previously shown fragment).
     * @return the class of the navigation hierarchy parent of this fragment, or null (for top
     *         level fragments)
     */
    public abstract Class<? extends AbstractFragment> getNavHierParentFragment();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (IDoCareFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IDoCareFragmentCallback");
        }

    }

    /**
     * Call to this method replaces the currently shown fragment with a new one
     * @param claz the class of the new fragment.
     * @param addToBackStack whether the old fragment should be added to the back stack.
     * @param args arguments to be set for the new fragment
     */
    public void replaceFragment(Class<? extends AbstractFragment> claz, boolean addToBackStack,
                                 Bundle args) {
        mCallback.replaceFragment(claz, addToBackStack, args);
    }


    /**
     * TODO: complete javadoc
     * @return
     */
    public RequestsMVCModel getRequestsModel() {
        return mCallback.getRequestsModel();
    }

    // ---------------------------------------------------------------------------------------------
    //
    // MVC Controller methods


    /**
     * Handle the message received by the inbox Handler
     * @param msg message to handle
     */
    protected abstract void handleMessage(Message msg);

    @Override
    public Handler getInboxHandler() {
        // Inbox Handler will be running on a separate thread
        if (mInboxHandlerThread == null) {
            mInboxHandlerThread = new HandlerThread(this.getClass().getSimpleName());
            mInboxHandlerThread.start();
        }
        if (mInboxHandler == null) {
            mInboxHandler = new Handler(mInboxHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    AbstractFragment.this.handleMessage(msg);
                }
            };
        }
        return mInboxHandler;
    }

    @Override
    public void addOutboxHandler(Handler handler) {
        // Not sure that there will be use case that requires sync, but just as precaution...
        synchronized (mOutboxHandlers) {
            if (!mOutboxHandlers.contains(handler)) {
                mOutboxHandlers.add(handler);
            }
        }
    }

    @Override
    public void removeOutboxHandler(Handler handler) {
        // Not sure that there will be use case that requires sync, but just as precaution...
        synchronized (mOutboxHandlers) {
            mOutboxHandlers.remove(handler);
        }
    }

    @Override
    public void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj) {
        // Not sure that there will be use case that requires sync, but just as precaution...
        synchronized (mOutboxHandlers) {
            for (Handler handler : mOutboxHandlers) {
                Message msg = Message.obtain(handler, what, arg1, arg2, obj);
                msg.sendToTarget();
            }
        }
    }



    // End of MVC Controller methods
    //
    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
    //
    // Inner classes and interfaces

    /**
     * The enclosing activity must implement this interface
     */
    public interface IDoCareFragmentCallback {

        /**
         * Call to this method replaces the currently shown fragment with a new one
         * @param claz the class of the new fragment
         * @param addToBackStack whether the old fragment should be added to the back stack
         * @param args arguments to be set for the new fragment
         */
        public void replaceFragment(Class<? extends AbstractFragment> claz, boolean addToBackStack,
                                    Bundle args);

        /**
         * TODO: complete javadoc
         * @return
         */
        public RequestsMVCModel getRequestsModel();
    }


    // End of inner classes and interfaces
    //
    // ---------------------------------------------------------------------------------------------


}
