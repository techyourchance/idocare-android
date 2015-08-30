package il.co.idocare.controllers.fragments;

import android.accounts.Account;
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

import il.co.idocare.handlermessaging.HandlerMessagingMaster;
import il.co.idocare.handlermessaging.HandlerMessagingSlave;


/**
 * This is a wrapper to the standard Fragment class which adds some convenience logic specific
 * to the app.<br>
 * Fragments of this app should extend this class.
 */
public abstract class AbstractFragment extends Fragment implements
        MyFragmentInterface,
        HandlerMessagingMaster,
        HandlerMessagingSlave {

    IDoCareFragmentCallback mCallback;

    HandlerThread mInboxHandlerThread;
    Handler mInboxHandler;
    final List<Handler> mOutboxHandlers = new ArrayList<Handler>();
    private ProgressDialog mProgressDialog;


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
     * See {@link IDoCareFragmentCallback#replaceFragment(Class, boolean, boolean, Bundle)}
     */
    public void replaceFragment(Class<? extends Fragment> claz, boolean addToBackStack,
                                 boolean clearBackStack, Bundle args) {
        mCallback.replaceFragment(claz, addToBackStack, clearBackStack, args);
    }


    /**
     * See {@link IDoCareFragmentCallback#setActionBarTitle(String)}
     */
    public void setActionBarTitle(String title) {
        mCallback.setActionBarTitle(title);
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
     * See {@link IDoCareFragmentCallback#getAuthTokenForActiveAccount()}
     */
    public String getAuthTokenForActiveAccount() throws AuthenticatorException, OperationCanceledException, IOException {
        return mCallback.getAuthTokenForActiveAccount();
    }


    /**
     * See {@link IDoCareFragmentCallback#getActiveAccount()}
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


}
