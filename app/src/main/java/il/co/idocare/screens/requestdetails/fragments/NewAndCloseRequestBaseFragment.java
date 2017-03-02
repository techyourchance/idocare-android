package il.co.idocare.screens.requestdetails.fragments;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import il.co.idocare.Constants;
import il.co.idocare.authentication.LoginStateManager;
import il.co.idocare.authentication.events.UserLoggedOutEvent;
import il.co.idocare.location.IdcLocationManager;
import il.co.idocare.serversync.ServerSyncController;
import il.co.idocare.pictures.CameraAdapter;
import il.co.idocare.screens.common.MainFrameHelper;
import il.co.idocare.screens.common.fragments.BaseScreenFragment;
import il.co.idocare.utils.UtilMethods;


public abstract class NewAndCloseRequestBaseFragment extends BaseScreenFragment implements IdcLocationManager.LocationUpdateListener {

    @Inject LoginStateManager mLoginStateManager;
    @Inject CameraAdapter mCameraAdapter;
    @Inject IdcLocationManager mIdcLocationManager;
    @Inject ServerSyncController mServerSyncController;
    @Inject MainFrameHelper mMainFrameHelper;

    private String mLastCameraPicturePath;
    private List<String> mCameraPicturesPaths = new ArrayList<String>(3);
    private int mNextCameraPictureIndex = 0;

    private Location mCurrentLocation;

    /**
     * Will be called when a new picture needs to be added to UI
     * @param index an index of the picture which was added
     */
    protected abstract void onNewPictureAdded(int index, @NonNull String pathToPicture);

    /**
     * @return the maximum number of pictures which can be added
     */
    protected abstract int getMaxPictures();


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getControllerComponent().inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mLastCameraPicturePath = savedInstanceState.getString("lastCameraPicturePath");

            // Get the list of pictures from saved state and pass them to adapter
            String[] cameraPicturesPaths = savedInstanceState.getStringArray("cameraPicturesPaths");

            for (String cameraPicturePath : cameraPicturesPaths) {
                addPicture(cameraPicturePath);
            }

            mNextCameraPictureIndex = savedInstanceState.getInt("nextCameraPictureIndex");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mLoginStateManager.isLoggedIn()) {
            // the user logged out while this fragment was paused
            onUserLoggedOut();
            return;
        }

        mIdcLocationManager.registerListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mIdcLocationManager.unregisterListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("lastCameraPicturePath", mLastCameraPicturePath);

        String[] cameraPicturesPaths = new String[mCameraPicturesPaths.size()];
        mCameraPicturesPaths.toArray(cameraPicturesPaths);

        outState.putStringArray("cameraPicturesPaths", cameraPicturesPaths);

        outState.putInt("nextCameraPictureIndex", mNextCameraPictureIndex);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                UtilMethods.adjustCameraPicture(mLastCameraPicturePath);
                addPicture(mLastCameraPicturePath);
            } else {
                // TODO: do we need anything here?
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    // ---------------------------------------------------------------------------------------------
    //
    // EventBus events handling

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserLoggedOutEvent event) {
        onUserLoggedOut();
    }

    // End of EventBus events handling
    //
    // ---------------------------------------------------------------------------------------------

    /**
     * This method should be called if user logs out when this fragment is shown
     */
    protected void onUserLoggedOut() {
        // TODO: navigate up!
    }

    /**
     * Take a new picture with camera
     */
    protected void takePictureWithCamera() {
        mLastCameraPicturePath = mCameraAdapter.takePicture(
                Constants.REQUEST_CODE_TAKE_PICTURE, "new_request");
    }

    protected List<String> getPicturesPaths() {
        return new ArrayList<>(mCameraPicturesPaths);
    }


    private void addPicture(@NonNull String cameraPicturePath) {

        if (mCameraPicturesPaths.size() > mNextCameraPictureIndex) {
            mCameraPicturesPaths.remove(mNextCameraPictureIndex);
        }

        mCameraPicturesPaths.add(mNextCameraPictureIndex, cameraPicturePath);

        onNewPictureAdded(mNextCameraPictureIndex, cameraPicturePath);

        mNextCameraPictureIndex = (mNextCameraPictureIndex + 1) % getMaxPictures();
    }

    @Override
    public void onLocationUpdateReceived(Location location) {
        mCurrentLocation = location;
    }

    @Nullable
    protected Location getCurrentLocation() {
        return mCurrentLocation;
    }
}
