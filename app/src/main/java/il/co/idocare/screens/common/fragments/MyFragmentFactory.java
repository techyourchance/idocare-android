package il.co.idocare.screens.common.fragments;

import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import il.co.idocarecore.screens.common.dialogs.InfoDialog;
import il.co.idocarecore.screens.common.dialogs.PromptDialog;
import il.co.idocarerequests.screens.requestdetails.fragments.CloseRequestFragment;
import il.co.idocarerequests.screens.requestdetails.fragments.NewRequestFragment;
import il.co.idocarerequests.screens.requestdetails.fragments.RequestDetailsFragment;
import il.co.idocarerequests.screens.requests.fragments.RequestsAllFragment;
import il.co.idocarerequests.screens.requests.fragments.RequestsMyFragment;

public class MyFragmentFactory extends FragmentFactory {

    private final Provider<RequestDetailsFragment> mRequestDetailsFragmentProvider;
    private final Provider<RequestsAllFragment> mRequestAllFragmentProvider;
    private final Provider<RequestsMyFragment> mRequestsMyFragmentProvider;
    private final Provider<CloseRequestFragment> mCloseRequestFragmentProvider;
    private final Provider<NewRequestFragment> mNewRequestFragmentProvider;
    private final Provider<InfoDialog> mInfoDialogProvider;
    private final Provider<PromptDialog> mPromptDialogProvider;

    @Inject
    public MyFragmentFactory(Provider<RequestDetailsFragment> requestDetailsFragmentProvider,
                             Provider<RequestsAllFragment> requestAllFragmentProvider,
                             Provider<RequestsMyFragment> requestsMyFragmentProvider,
                             Provider<CloseRequestFragment> closeRequestFragmentProvider,
                             Provider<NewRequestFragment> newRequestFragmentProvider,
                             Provider<InfoDialog> infoDialogProvider,
                             Provider<PromptDialog> promptDialogProvider) {
        mRequestDetailsFragmentProvider = requestDetailsFragmentProvider;
        mRequestAllFragmentProvider = requestAllFragmentProvider;
        mRequestsMyFragmentProvider = requestsMyFragmentProvider;
        mCloseRequestFragmentProvider = closeRequestFragmentProvider;
        mNewRequestFragmentProvider = newRequestFragmentProvider;
        mInfoDialogProvider = infoDialogProvider;
        mPromptDialogProvider = promptDialogProvider;
    }


    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        Class clazz = loadFragmentClass(classLoader, className);

        Fragment fragment = null;
        if (clazz == RequestDetailsFragment.class) {
            fragment = mRequestDetailsFragmentProvider.get();
        }
        else if (clazz == RequestsAllFragment.class) {
            fragment = mRequestAllFragmentProvider.get();
        }
        else if (clazz == RequestsMyFragment.class) {
            fragment = mRequestsMyFragmentProvider.get();
        }
        else if (clazz == NewRequestFragment.class) {
            fragment = mNewRequestFragmentProvider.get();
        }
        else if (clazz == CloseRequestFragment.class) {
            fragment = mCloseRequestFragmentProvider.get();
        }
        else if (clazz == InfoDialog.class) {
            fragment = mInfoDialogProvider.get();
        }
        else if (clazz == PromptDialog.class) {
            fragment = mPromptDialogProvider.get();
        }
        else {
            fragment = super.instantiate(classLoader, className);
        }

        return fragment;
    }
}
