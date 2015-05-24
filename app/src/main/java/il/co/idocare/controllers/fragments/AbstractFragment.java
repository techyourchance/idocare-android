package il.co.idocare.controllers.fragments;

import android.accounts.Account;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import il.co.idocare.controllers.ControllerMVC;
import il.co.idocare.handlermessaging.HandlerMessagingMaster;
import il.co.idocare.handlermessaging.HandlerMessagingSlave;
import il.co.idocare.models.RequestsMVCModel;
import il.co.idocare.models.UsersMVCModel;


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
    private ProgressDialog mProgressDialog;

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

    /**
     * Get the resource ID of fragment's title
     * @return resource ID of fragments title, or 0 if the fragment does not have a title
     */
    public abstract int getTitle();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (IDoCareFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IDoCareFragmentCallback");
        }

        mCallback.setActionBarTitle(getTitle());

    }


    /**
     * Call to this method replaces the currently shown fragment with a new one
     * @param claz the class of the new fragment.
     * @param addToBackStack whether the old fragment should be added to the back stack.
     * @param args arguments to be set for the new fragment
     */
    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                 Bundle args) {
        mCallback.replaceFragment(claz, addToBackStack, args);
    }

    /**
     * Change ActionBar title
     */
    public void setActionBarTitle(int resourceId) {
        mCallback.setActionBarTitle(resourceId);
    }


    /**
     * 
     * @return MVC model representing the requests
     */
    public RequestsMVCModel getRequestsModel() {
        return RequestsMVCModel.getInstance();
    }


    /**
     *
     * @return MVC model representing the users
     */
    public UsersMVCModel getUsersModel() {
        return mCallback.getUsersModel();
    }

    /**
     * @return ContentResolver associated with parent activity
     */
    public ContentResolver getContentResolver() {
        return getActivity().getContentResolver();
    }

    /**
     * Show standard (for the app) progress dialog
     */
    public void showProgressDialog(String title, String message) {
        mProgressDialog = ProgressDialog.
                show(getActivity(), title, message, true);
    }

    /**
     * Dismiss the standard progress dialog
     */
    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    /**
     * This method obtains the auth token for the active account (as specified in SharedPreferences).
     * If no IDoCare accounts registered on the device, or active account is not set, or the auth
     * token is not valid - the user will be prompted for credentials using AccountManager's APIs.
     *
     * This method might block and should not be called on the main thread.
     * @return AccountManagerFuture object as returned by GetAuthToken() of AccountManager.
     */
    public String getAuthTokenForActiveAccount() throws AuthenticatorException, OperationCanceledException, IOException {
        return mCallback.getAuthTokenForActiveAccount();
    }

    /**
     * @return the active account as specified in SharedPreferences, null if no account specified
     */
    public Account getActiveAccount() {
        return mCallback.getActiveAccount();
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
        // TODO: consider changing all handlers to run on the main thread.
        // Inbox Handler will be running on a separate thread
        // The cast into Object is due to this:
        // http://stackoverflow.com/questions/18505973/android-studio-ambiguous-method-call-getclass
        if (mInboxHandlerThread == null) {
            mInboxHandlerThread = new HandlerThread(((Object)this).getClass().getSimpleName());
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
        public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                    Bundle args);

        /**
         * Change ActionBar title
         */
        public void setActionBarTitle(int resourceId);

        /**
         * @return MVC model representing the users
         */
        public UsersMVCModel getUsersModel();

        /**
         * This method obtains the auth token for the active account (as specified in SharedPreferences).
         * If no IDoCare accounts registered on the device, or active account is not set, or the auth
         * token is not valid - the user will be prompted for credentials using AccountManager's APIs.
         *
         * This method might block and should not be called on the main thread.
         * @return AccountManagerFuture object as returned by GetAuthToken() of AccountManager.
         */
        public String getAuthTokenForActiveAccount() throws AuthenticatorException, OperationCanceledException, IOException;

        /**
         * @return the active account as specified in SharedPreferences, null if no account specified
         */
        public Account getActiveAccount();
    }


    // End of inner classes and interfaces
    //
    // ---------------------------------------------------------------------------------------------


}
