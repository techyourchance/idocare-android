package il.co.idocarerequests.screens.requestdetails.fragments;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.techyourchance.fragmenthelper.FragmentHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import il.co.idocarecore.Constants;
import il.co.idocarecore.authentication.LoginStateManager;
import il.co.idocarecore.authentication.events.UserLoggedOutEvent;
import il.co.idocarecore.location.IdcLocationManager;
import il.co.idocarecore.pictures.CameraAdapter;
import il.co.idocarecore.serversync.ServerSyncController;
import il.co.idocarecore.utils.UtilMethods;


public abstract class NewAndCloseRequestBaseFragment extends Fragment implements
        IdcLocationManager.LocationUpdateListener {

    protected final LoginStateManager mLoginStateManager;
    protected final CameraAdapter mCameraAdapter;
    protected final IdcLocationManager mIdcLocationManager;
    protected final ServerSyncController mServerSyncController;
    protected final FragmentHelper mFragmentHelper;

    private String mLastCameraPicturePath;
    private List<String> mCameraPicturesPaths = new ArrayList<String>(3);
    private int mNextCameraPictureIndex = 0;

    private Location mCurrentLocation;

    public NewAndCloseRequestBaseFragment(LoginStateManager loginStateManager,
                                          CameraAdapter cameraAdapter,
                                          IdcLocationManager idcLocationManager,
                                          ServerSyncController serverSyncController,
                                          FragmentHelper fragmentHelper) {
        mLoginStateManager = loginStateManager;
        mCameraAdapter = cameraAdapter;
        mIdcLocationManager = idcLocationManager;
        mServerSyncController = serverSyncController;
        mFragmentHelper = fragmentHelper;
    }

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
