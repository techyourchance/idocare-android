package il.co.idocare.controllers.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;

import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import il.co.idocare.Constants;
import il.co.idocare.R;
import il.co.idocare.ServerRequest;
import il.co.idocare.utils.UtilMethods;
import il.co.idocare.controllers.activities.IDoCareActivity;


public class FragmentNewAndCloseRequest extends IDoCareFragment {

    private final static String LOG_TAG = "FragmentNewAndCloseRequest";


    /**
     * Names of JSON fields which contain lists of pictures
     */
    private final static String NEW_REQUEST_PICTURES_HTTP_FIELD_NAME = "imagesBefore";
    private final static String CLOSE_REQUEST_PICTURES_HTTP_FIELD_NAME = "imagesAfter";


    NewPicturesAdapter mListAdapter;
    String mLastCameraPicturePath;
    boolean mIsCloseRequestType;
    String mRequestId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_request, container, false);

        // Whether New Request or Close Request layout and functionality
        Bundle args = getArguments();
        if (args != null) {
            mIsCloseRequestType = args.getBoolean("isCloseRequestType");
            mRequestId = args.getString("requestId");
        } else {
            mIsCloseRequestType = false;
        }

        // Make all the initiations which are required by fragment's child views
        initiateTheViews(view);

        // Restore state from bundle (if required)
        restoreSavedStateIfNeeded(view, savedInstanceState);

        return view;
    }

    /**
     * This method makes all the initiations of the child views of this fragment
     * @param view
     */
    private void initiateTheViews(View view) {

        mListAdapter = new NewPicturesAdapter(getActivity(), 0);
        ListView listPictures = (ListView) view.findViewById(R.id.list_pictures);
        listPictures.setAdapter(mListAdapter);

        Button btnTakePicture = (Button) view.findViewById(R.id.btn_take_picture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureWithCamera();
            }
        });

        Button btnAddOrCloseRequest = (Button) view.findViewById(R.id.btn_add_or_close_request);

        if (mIsCloseRequestType) {
            // "Close request" functionality
            btnAddOrCloseRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeRequest();
                }
            });
            btnAddOrCloseRequest.setText(getActivity().getResources()
                    .getString(R.string.btn_close_request));
            view.findViewById(R.id.ratingbar_rate).setVisibility(View.GONE);
        } else {
            // "Add new request" functionality
            btnAddOrCloseRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addNewRequest();
                }
            });
            btnAddOrCloseRequest.setText(getActivity().getResources()
                    .getString(R.string.btn_add_new_request));
        }
    }

    private void restoreSavedStateIfNeeded(View view, Bundle savedInstanceState) {

        if (savedInstanceState == null) return;

        // Get the list of pictures from saved state and pass them to adapter
        String[] adapterItems = savedInstanceState.getStringArray("adapterItems");
        if (adapterItems != null) {
            mListAdapter.addAll(adapterItems);
            mListAdapter.notifyDataSetChanged();
        }

        // Restore the last path to camera picture
        if (savedInstanceState.getString("lastCameraPicturePath") != null) {
            mLastCameraPicturePath = savedInstanceState.getString("lastCameraPicturePath");
        }
    }


    @Override
    public boolean isTopLevelFragment() {
        return false;
    }

    @Override
    public Class<? extends IDoCareFragment> getNavHierParentFragment() {
        return FragmentHome.class;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save adapter items
        String[] adapterItems = mListAdapter.getItems();
        if (adapterItems != null) {
            outState.putStringArray("adapterItems", adapterItems);
        }
        // Save the absolute path of the last picture (otherwise we get NullPointerException when
        // trying to access it in onActivityResult())
        outState.putString("lastCameraPicturePath", mLastCameraPicturePath);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.StartActivityTag.CAPTURE_PICTURE_FOR_NEW_REQUEST.ordinal()) {
            if (resultCode == Activity.RESULT_OK) {
                UtilMethods.adjustCameraPicture(mLastCameraPicturePath);
                mListAdapter.add(mLastCameraPicturePath);
                mListAdapter.notifyDataSetChanged();
            } else {
                // TODO: do we need anything here?
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Create ACTION_IMAGE_CAPTURE intent with EXTRA_OUTPUT path and call startActivityForResult()
     * with this intent
     */
    private void takePictureWithCamera() {
        String currDateTime =
                new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault()).format(new Date());

        File outputFile = new File(getActivity()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES), currDateTime + ".jpg");

        mLastCameraPicturePath = outputFile.getAbsolutePath();

        Uri cameraPictureUri = Uri.fromFile(outputFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPictureUri);
        startActivityForResult(intent, Constants.StartActivityTag.CAPTURE_PICTURE_FOR_NEW_REQUEST.ordinal());
    }


    /**
     * Create, populate and execute a new server request of type "add new request" based
     * on the contents of fragment's views
     */
    private void addNewRequest() {

        if (getView() == null) {
            Log.e(LOG_TAG, "getView() returned null");
            return;
        }

        RatingBar rating = (RatingBar) getView().findViewById(R.id.ratingbar_rate);
        EditText edtComment = (EditText) getView().findViewById(R.id.edt_comment);

        ServerRequest serverRequest = new ServerRequest(Constants.ADD_REQUEST_URL);

        // TODO: field names should come from constants and the values should not be hardcoded
        SharedPreferences prefs =
                getActivity().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        serverRequest.addTextField("username", prefs.getString("username", "no_username"));
        serverRequest.addTextField("password", prefs.getString("password", "no_password"));
        serverRequest.addTextField("openedBy", prefs.getString("username", "no_username"));

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                ((IDoCareActivity)getActivity()).mGoogleApiClient);
        if (lastLocation != null) {
            serverRequest.addTextField("lat", String.valueOf(lastLocation.getLatitude()));
            serverRequest.addTextField("long", String.valueOf(lastLocation.getLongitude()));
        }

        serverRequest.addTextField("pollutionLevel", String.valueOf(rating.getRating()));

        if (edtComment.getText().toString().length() > 0) {
            serverRequest.addTextField("noteBefore", edtComment.getText().toString());
        }

        for (int i = 0; i < mListAdapter.getCount(); i++) {
            serverRequest.addPicture(NEW_REQUEST_PICTURES_HTTP_FIELD_NAME,
                    "picture" + String.valueOf(i) + ".jpg", mListAdapter.getItem(i));
        }

        serverRequest.execute();
    }

    /**
     * Create, populate and execute a new server request of type "close equest" based
     * on the contents of fragment's views
     */
    private void closeRequest() {

        if (getView() == null) {
            Log.e(LOG_TAG, "getView() returned null");
            return;
        }

        EditText edtComment = (EditText) getView().findViewById(R.id.edt_comment);

        ServerRequest serverRequest = new ServerRequest(Constants.CLOSE_REQUEST_URL);

        // TODO: field names should come from constants and the values should not be hardcoded
        SharedPreferences prefs =
                getActivity().getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        serverRequest.addTextField("username", prefs.getString("username", "no_username"));
        serverRequest.addTextField("password", prefs.getString("password", "no_password"));
        serverRequest.addTextField("requestId", mRequestId);


        if (edtComment.getText().toString().length() > 0) {
            serverRequest.addTextField("noteAfter", edtComment.getText().toString());
        }

        for (int i = 0; i < mListAdapter.getCount(); i++) {
            serverRequest.addPicture(CLOSE_REQUEST_PICTURES_HTTP_FIELD_NAME,
                    "picture" + String.valueOf(i) + ".jpg", mListAdapter.getItem(i));
        }

        serverRequest.execute();

    }


    private static class ViewHolder {
        ImageView imageView;
    }

    private class NewPicturesAdapter extends ArrayAdapter<String> {

        private final static String LOG_TAG = "NewPicturesAdapter";

        private LayoutInflater mInflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        public NewPicturesAdapter(Context context, int resource) {
            super(context, resource);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.element_camera_picture, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            String pathForUIL = "file://" + getItem(position);

            Log.d(LOG_TAG, "UIL is loading picture at: " + pathForUIL);

            ImageLoader.getInstance().displayImage(pathForUIL, holder.imageView, animateFirstListener);

            return view;
        }


        /**
         * Get all the items of this adapter
         * @return array of items or null if there are none
         */
        public String[] getItems() {
            if (getCount() == 0) {
                return null;
            }
            String[] items = new String[getCount()];
            for (int i = 0; i < getCount(); i++) {
                items[i] = getItem(i);
            }
            return items;
        }
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new ArrayList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }







}
