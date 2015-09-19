package il.co.idocare.controllers.fragments;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.activities.AbstractActivity;
import il.co.idocare.controllers.activities.LoginActivity;


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
     * See {@link IDoCareFragmentCallback#askUserToLogIn(String, Runnable)}
     */
    public void askUserToLogIn(String message, final Runnable runnable) {
        mCallback.askUserToLogIn(message, runnable);
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
     * See {@link UserStateManager#getActiveAccount()}
     */
    public Account getActiveAccount() {
        return mCallback.getUserStateManager().getActiveAccount();
    }



    /**
     * See {@link UserStateManager#getActiveAccount()}
     */
    public String getActiveAccountUserId() {
        return mCallback.getUserStateManager().getActiveAccountUserId();
    }


    /**
     * See {@link UserStateManager#getActiveAccountAuthToken()}
     */
    public String getActiveAccountAuthToken() {
        return mCallback.getUserStateManager().getActiveAccountAuthToken();
    }


    /**
     * See {@link IDoCareFragmentCallback#requestImmediateSync()}
     */
    public void requestImmediateSync() {
        mCallback.requestImmediateSync();
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
