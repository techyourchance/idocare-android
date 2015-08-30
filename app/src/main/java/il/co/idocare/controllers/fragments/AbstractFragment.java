package il.co.idocare.controllers.fragments;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.Bundle;

import java.io.IOException;

import de.greenrobot.event.EventBus;


/**
 * This is a wrapper to the standard Fragment class which adds some convenience logic specific
 * to the app.<br>
 * Fragments of this app should extend this class.
 */
public abstract class AbstractFragment extends Fragment implements
        IDoCareFragmentInterface {

    IDoCareFragmentCallback mCallback;

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
    // EventBus configuration

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    // End of EventBus configuration
    //
    // ---------------------------------------------------------------------------------------------


}
