package il.co.idocare.screens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.techyourchance.fragmenthelper.FragmentHelper;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import il.co.idocare.controllers.activities.LoginActivity;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarerequests.screens.requestdetails.fragments.CloseRequestFragment;
import il.co.idocarerequests.screens.requestdetails.fragments.NewRequestFragment;
import il.co.idocarerequests.screens.requestdetails.fragments.RequestDetailsFragment;
import il.co.idocarerequests.screens.requests.fragments.RequestsAllFragment;
import il.co.idocarerequests.screens.requests.fragments.RequestsMyFragment;

public class ScreensNavigatorImpl implements ScreensNavigator {

    private final FragmentHelper mFragmentHelper;
    private final Activity mActivity;
    private final FragmentFactory mFragmentFactory;

    public ScreensNavigatorImpl(FragmentHelper fragmentHelper, Activity activity, FragmentFactory fragmentFactory) {
        mFragmentHelper = fragmentHelper;
        mActivity = activity;
        mFragmentFactory = fragmentFactory;
    }

    @Override
    public void navigateUp() {
        mFragmentHelper.navigateUp();
    }

    @Override
    public void navigateBack() {
        mFragmentHelper.navigateBack();
    }

    @Override
    public void toRequestDetails(String requestId) {
        Bundle args = new Bundle();
        args.putString(RequestDetailsFragment.ARG_REQUEST_ID, requestId);

        Fragment fragment = mFragmentFactory.instantiate(mActivity.getClassLoader(), RequestDetailsFragment.class.getName());
        fragment.setArguments(args);

        mFragmentHelper.replaceFragment(fragment);
    }

    @Override
    public void toCloseRequest(String requestId, double longitude, double latitude) {
        Bundle args = new Bundle();
        args.putString(CloseRequestFragment.ARG_REQUEST_ID, requestId);

        Fragment fragment = mFragmentFactory.instantiate(mActivity.getClassLoader(), CloseRequestFragment.class.getName());
        fragment.setArguments(args);

        mFragmentHelper.replaceFragment(fragment);
    }

    @Override
    public void toLogin() {
        Intent intent = new Intent(mActivity, LoginActivity.class);
        mActivity.startActivity(intent);
    }

    @Override
    public void toNewRequest() {
        mFragmentHelper.replaceFragment(
                mFragmentFactory.instantiate(mActivity.getClassLoader(), NewRequestFragment.class.getName())
        );
    }

    @Override
    public void toMyRequests() {
        mFragmentHelper.replaceFragmentAndClearHistory(
                mFragmentFactory.instantiate(mActivity.getClassLoader(), RequestsMyFragment.class.getName())
        );
    }

    @Override
    public void toAllRequests() {
        mFragmentHelper.replaceFragmentAndClearHistory(
                mFragmentFactory.instantiate(mActivity.getClassLoader(), RequestsAllFragment.class.getName())
        );
    }
}
