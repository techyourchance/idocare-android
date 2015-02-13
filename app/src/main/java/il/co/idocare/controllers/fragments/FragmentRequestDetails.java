package il.co.idocare.controllers.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.UtilMethods;
import il.co.idocare.views.RequestDetailsViewMVC;


public class FragmentRequestDetails extends AbstractFragment {

    private final static String LOG_TAG = "FragmentRequestDetails";

    RequestDetailsViewMVC mRequestDetailsViewMVC;

    RequestItem mRequestItem;
    boolean mIsClosed;
    boolean mIsPickedUp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRequestDetailsViewMVC = new RequestDetailsViewMVC(getActivity(), inflater, container);
        // Provide inbox Handler to the MVC View
        mRequestDetailsViewMVC.addOutboxHandler(getInboxHandler());
        // Add MVC View's Handler to the set of outbox Handlers
        addOutboxHandler(mRequestDetailsViewMVC.getInboxHandler());

        Bundle args = getArguments();
        if (args != null) {
            mRequestItem = (RequestItem) args.getParcelable("requestItem");
        }

        if (mRequestItem == null) {
            // TODO: handle this error somehow
            return mRequestDetailsViewMVC.getRootView();
        }

        Log.v(LOG_TAG, "Request details:"
                + "\nCreated by " + mRequestItem.mOpenedBy + " at " + mRequestItem.mCreationDate
                + "\nPicked up by " + mRequestItem.mPickedUpBy + " at " + mRequestItem.mPickedUpDate
                + "\nClosed by " + mRequestItem.mPickedUpBy + " at " + mRequestItem.mCloseDate);


        mRequestDetailsViewMVC.populateChildViewsFromRequestItem(mRequestItem);

        return mRequestDetailsViewMVC.getRootView();
    }

    @Override
    public boolean isTopLevelFragment() {
        return false;
    }

    @Override
    public Class<? extends AbstractFragment> getNavHierParentFragment() {
        return FragmentHome.class;
    }

    @Override
    protected void handleMessage(Message msg) {
        // TODO: complete this method
        switch (Constants.MESSAGE_TYPE_VALUES[msg.what]) {
            case V_PICKUP_REQUEST_BUTTON_CLICKED:
                pickupRequest();
                break;
            case V_CLOSE_REQUEST_BUTTON_CLICKED:
                closeRequest();
                break;
            default:
                Log.w(LOG_TAG, "Message of type "
                        + Constants.MESSAGE_TYPE_VALUES[msg.what].toString() + " wasn't consumed");
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void pickupRequest() {

        ServerRequest serverRequest = new ServerRequest(Constants.PICKUP_REQUEST_URL,
                Constants.ServerRequestTag.PICKUP_REQUEST, null);

        // TODO: field names should come from constants and the values should not be hardcoded
        SharedPreferences prefs =
                getActivity().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        serverRequest.addTextField("username", prefs.getString("username", "no_username"));
        serverRequest.addTextField("password", prefs.getString("password", "no_password"));
        serverRequest.addTextField("requestId", mRequestItem.mId);
        serverRequest.addTextField("pickedUpBy", prefs.getString("username", "no_username"));

        serverRequest.execute();

    }

    private void closeRequest() {
        Bundle args = new Bundle();
        args.putString("requestId", mRequestItem.mId);
        replaceFragment(FragmentCloseRequest.class, true, args);
    }


}
