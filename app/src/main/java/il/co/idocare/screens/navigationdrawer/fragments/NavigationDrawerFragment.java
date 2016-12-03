package il.co.idocare.screens.navigationdrawer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import il.co.idocare.Constants;
import il.co.idocare.MyApplication;
import il.co.idocare.R;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocare.controllers.fragments.HomeFragment;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.dependencyinjection.contextscope.ContextModule;
import il.co.idocare.dependencyinjection.controllerscope.ControllerModule;
import il.co.idocare.dialogs.DialogsManager;
import il.co.idocare.eventbusevents.DialogEvents;
import il.co.idocare.eventbusevents.LoginStateEvents;
import il.co.idocare.helpers.FrameHelper;
import il.co.idocare.loaders.UserInfoLoader;
import il.co.idocare.networking.ServerSyncController;
import il.co.idocare.screens.common.FrameContainer;
import il.co.idocare.screens.navigationdrawer.mvcviews.NavigationDrawerViewMvcImpl;
import il.co.idocare.utils.Logger;

/**
 * This fragment will be shown in navigation drawer
 */

public class NavigationDrawerFragment extends Fragment implements
        NavigationDrawerViewMvcImpl.NavigationDrawerViewMvcListener,
        LoaderManager.LoaderCallbacks<UserItem> {

    private static final String TAG = "NavigationDrawerFragment";

    private static final String USER_LOGIN_DIALOG_TAG = "USER_LOGIN_DIALOG_TAG";

    private static final int USER_LOADER = 0;

    @Inject LoginStateManager mLoginStateManager;
    @Inject ServerSyncController mServerSyncController;
    @Inject DialogsManager mDialogsManager;
    @Inject EventBus mEventBus;
    @Inject Logger mLogger;


    private NavigationDrawerViewMvcImpl mViewMvc;

    private FrameHelper mMainFrameHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((MyApplication)getActivity().getApplication())
                .getApplicationComponent()
                .newContextComponent(new ContextModule(getActivity()))
                .newControllerComponent(new ControllerModule(getActivity(), getChildFragmentManager()))
        .inject(this);

        mMainFrameHelper = ((FrameContainer)activity).getFrameHelper();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewMvc = new NavigationDrawerViewMvcImpl(inflater, container);
        mViewMvc.registerListener(this);

        getLoaderManager().initLoader(USER_LOADER, null, this);

        return mViewMvc.getRootView();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNavDrawer();
    }

    @Override
    public void onStart() {
        super.onStart();
        mEventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    @Override
    public void onDrawerEntryChosen(String chosenEntry) {
        if (chosenEntry.equals(getString(R.string.nav_drawer_entry_requests_list))) {
            mMainFrameHelper.replaceFragment(HomeFragment.class, false, true, null);
        }
        else if (chosenEntry.equals(getString(R.string.nav_drawer_entry_new_request))) {
            if (mLoginStateManager.isLoggedIn())
                mMainFrameHelper.replaceFragment(NewRequestFragment.class, true, false, null);
            else
                mDialogsManager.showPromptDialog(
                        null,
                        getString(R.string.msg_ask_to_log_in_before_new_request),
                        getResources().getString(R.string.btn_dialog_positive),
                        getResources().getString(R.string.btn_dialog_negative),
                        USER_LOGIN_DIALOG_TAG);
        } else if (chosenEntry.equals(getString(R.string.nav_drawer_entry_login))) {
            initiateLoginFlow();
        }
        else if (chosenEntry.equals(getString(R.string.nav_drawer_entry_logout))) {
            initiateLogoutFlow();
        }
        else {
            mLogger.e(TAG, "drawer entry '" + chosenEntry + "' has no functionality");
        }
    }

    private void initiateLogoutFlow() {
        mLoginStateManager.logOut();
    }

    private void initiateLoginFlow() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN);
    }

    @Subscribe
    public void onPromptDialogDismissed(DialogEvents.PromptDialogDismissedEvent event) {
        if (event.getTag().equals(USER_LOGIN_DIALOG_TAG)) {
            if (event.getClickedButtonIndex() == DialogEvents.PromptDialogDismissedEvent.BUTTON_POSITIVE) {
                initiateLoginFlow();
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    //
    // LoaderCallback methods

    @Override
    public Loader<UserItem> onCreateLoader(int id, Bundle args) {
        mLogger.d(TAG, "onCreateLoader()");

        if (id == USER_LOADER) {
            if (mLoginStateManager.isLoggedIn()) {

                String activeAccountId = mLoginStateManager.getActiveAccountUserId();

                mLogger.d(TAG, "instantiating new UserInfoLoader for; account ID: " + activeAccountId);

                return new UserInfoLoader(
                        getActivity(),
                        getActivity().getContentResolver(),
                        mServerSyncController,
                        activeAccountId);
            } else {
                return null;
            }
        } else {
            mLogger.e(TAG, "onCreateLoader() called with unrecognized loader id: " + id);
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<UserItem> loader, UserItem data) {
        if (loader.getId() == USER_LOADER) {
            mViewMvc.bindUserData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<UserItem> loader) {

    }



    // End of LoaderCallback methods
    //
    // ---------------------------------------------------------------------------------------------


    private void refreshNavDrawer() {
        mLogger.d(TAG, "refreshNavDrawer()");

        boolean isLoggedInUser = mLoginStateManager.isLoggedIn();

        mViewMvc.refreshDrawer(isLoggedInUser);

        if (isLoggedInUser) {
            mLogger.d(TAG, "restarting user info loader");
            getLoaderManager().restartLoader(USER_LOADER, null, this);
        } else {
            mLogger.d(TAG, "no logged in user - clearing user info from nav drawer");
            getLoaderManager().destroyLoader(USER_LOADER);
            mViewMvc.bindUserData(null);
        }
    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateEvents.LoginSucceededEvent event) {
        refreshNavDrawer();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginStateManager.UserLoggedOutEvent event) {
        refreshNavDrawer();
    }


    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------



}
