package il.co.idocare.screens.navigationdrawer.mvcviews;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.controllers.listadapters.NavigationDrawerListAdapter;
import il.co.idocare.datamodels.functional.NavigationDrawerEntry;
import il.co.idocare.mvcviews.AbstractViewMVC;
import il.co.idocare.users.UserEntity;


public class NavigationDrawerViewMvcImpl extends
        AbstractViewMVC<NavigationDrawerViewMvc.NavigationDrawerViewMvcListener>
        implements NavigationDrawerViewMvc, AdapterView.OnItemClickListener {

    private static final String ENTRY_NEW_REQUEST = "ENTRY_NEW_REQUEST";
    private static final String ENTRY_REQUESTS_LIST = "ENTRY_REQUESTS_LIST";
    private static final String ENTRY_MY_REQUESTS = "ENTRY_MY_REQUESTS";
    private static final String ENTRY_SHOW_MAP = "ENTRY_SHOW_MAP";
    private static final String ENTRY_LOG_IN = "ENTRY_LOG_IN";
    private static final String ENTRY_LOG_OUT = "ENTRY_LOG_OUT";


    private ImageView mImgUserPicture;
    private ImageView mImgReputationStar;
    private TextView mTxtUserReputation;
    private TextView mTxtUserNickname;

    private NavigationDrawerListAdapter mNavDrawerAdapter;
    private ListView mDrawerList;

    private UserEntity mCurrentUser;

    public NavigationDrawerViewMvcImpl(LayoutInflater inflater, ViewGroup container) {
        setRootView(inflater.inflate(R.layout.layout_navigation_drawer, container, false));


        mImgUserPicture = (ImageView) getRootView().findViewById(R.id.img_user_picture);
        mImgReputationStar = (ImageView) getRootView().findViewById(R.id.img_reputation_star);
        mTxtUserReputation = (TextView) getRootView().findViewById(R.id.txt_user_reputation);
        mTxtUserNickname = (TextView) getRootView().findViewById(R.id.txt_user_nickname);

        initDrawerListView();
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    @Override
    public void bindUserData(UserEntity user) {
        mCurrentUser = user;
        refreshDrawer();
    }

    private void initDrawerListView() {

        mDrawerList = (ListView) getRootView().findViewById(R.id.drawer_list);

        mNavDrawerAdapter = new NavigationDrawerListAdapter(getRootView().getContext(), 0);
        mDrawerList.setAdapter(mNavDrawerAdapter);

        mDrawerList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        mDrawerList.setItemChecked(position, true);

        NavigationDrawerEntry clickedEntry = mNavDrawerAdapter.getItem(position);

        if (clickedEntry != null) {
            for (NavigationDrawerViewMvcListener listener : getListeners()) {
                switch (clickedEntry.getTag()){
                    case ENTRY_REQUESTS_LIST:
                        listener.onRequestsListClicked();
                        break;
                    case ENTRY_MY_REQUESTS:
                        listener.onMyRequestsClicked();
                        break;
                    case ENTRY_NEW_REQUEST:
                        listener.onNewRequestClicked();
                        break;
                    case ENTRY_SHOW_MAP:
                        listener.onShowMapClicked();
                        break;
                    case ENTRY_LOG_IN:
                        listener.onLogInClicked();
                        break;
                    case ENTRY_LOG_OUT:
                        listener.onLogOutClicked();
                        break;
                    default:
                        throw new IllegalStateException("unrecognized entry tag:" + clickedEntry.getTag());
                }
            }
        }
    }

    private void refreshDrawer() {
        refreshDrawerHeader();
        refreshDrawerBody();
    }

    private void refreshDrawerBody() {
        List<NavigationDrawerEntry> entries = new ArrayList<>(8);

        // "My requests" only exposed for logged in users
        if (isUserLoggedIn()) {
            entries.add(new NavigationDrawerEntry(R.string.nav_drawer_entry_my,
                    R.drawable.ic_drawer_assigned_to_me, ENTRY_MY_REQUESTS));
        }

        entries.add(new NavigationDrawerEntry(R.string.nav_drawer_entry_new_request,
                R.drawable.ic_drawer_add_new_request, ENTRY_NEW_REQUEST));
        entries.add(new NavigationDrawerEntry(R.string.nav_drawer_entry_map,
                R.drawable.ic_drawer_location, ENTRY_SHOW_MAP));
        entries.add(new NavigationDrawerEntry(R.string.nav_drawer_entry_requests_list,
                R.drawable.ic_drawer_requests_list, ENTRY_REQUESTS_LIST));

        // No need for both login/logout options at once
        if (isUserLoggedIn())
            entries.add(new NavigationDrawerEntry(R.string.nav_drawer_entry_logout,
                    R.drawable.ic_drawer_logout, ENTRY_LOG_OUT));
        else
            entries.add(new NavigationDrawerEntry(R.string.nav_drawer_entry_login,
                    0, ENTRY_LOG_IN));

        mNavDrawerAdapter.clear();
        mNavDrawerAdapter.addAll(entries);
        mNavDrawerAdapter.notifyDataSetChanged();
    }

    private void refreshDrawerHeader() {
        if (isUserLoggedIn()) {
            mImgUserPicture.setVisibility(View.VISIBLE);
            mImgReputationStar.setVisibility(View.VISIBLE);
            mTxtUserReputation.setVisibility(View.VISIBLE);
            mTxtUserNickname.setVisibility(View.VISIBLE);

            mTxtUserNickname.setText(mCurrentUser.getNickname());
            mTxtUserReputation.setText(String.valueOf(mCurrentUser.getReputation()));

            if (!TextUtils.isEmpty(mCurrentUser.getPictureUrl())) {
                showUserPicture(mCurrentUser.getPictureUrl());
            } else {
                mImgUserPicture.setImageResource(R.drawable.ic_default_user_picture);
            }
        } else {
            mImgUserPicture.setVisibility(View.GONE);
            mImgReputationStar.setVisibility(View.GONE);
            mTxtUserReputation.setVisibility(View.GONE);
            mTxtUserNickname.setVisibility(View.GONE);
        }
    }

    private void showUserPicture(String pictureUrl) {
        String universalImageLoaderUri = pictureUrl;
        try {
            new URL(universalImageLoaderUri);
        } catch (MalformedURLException e) {
            // The exception means that the current Uri is not a valid URL - it is local
            // uri and we need to adjust it to the scheme recognized by UIL
            universalImageLoaderUri = "file://" + universalImageLoaderUri;
        }

        ImageLoader.getInstance().displayImage(
                universalImageLoaderUri,
                mImgUserPicture,
                Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);

    }

    private boolean isUserLoggedIn() {
        return mCurrentUser != null;
    }

}
