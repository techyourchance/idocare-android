package il.co.idocare.www.idocare;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class FragmentNewRequest extends Fragment {

    private final static String LOG_TAG = "FragmentNewRequest";


    private NewRequestPicturesAdapter mListAdapter;
    private String mCameraPictureAbsolutePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_request, container, false);


        mListAdapter = new NewRequestPicturesAdapter(getActivity(), 0);
        ListView listPictures = (ListView) view.findViewById(R.id.list_new_request_images);
        listPictures.setAdapter(mListAdapter);

        Button btnAddNewImage = (Button) view.findViewById(R.id.btn_add_new_image);
        btnAddNewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImageWithCamera();
            }
        });

        Button btnAddNewRequest = (Button) view.findViewById(R.id.btn_add_new_request);
        btnAddNewRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewRequest();
            }
        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.StartActivityTag.CAPTURE_PICTURE_FOR_NEW_REQUEST.ordinal()) {
            if (resultCode == Activity.RESULT_OK) {
                mListAdapter.add(mCameraPictureAbsolutePath);
                mListAdapter.notifyDataSetChanged();
            } else {
                // TODO: do we need anything here?
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void captureImageWithCamera() {
        String currDateTime =
                new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault()).format(new Date());

        File outputFile = new File(getActivity()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES), currDateTime + ".jpg");
        mCameraPictureAbsolutePath = outputFile.getAbsolutePath();

        Uri cameraPictureUri = Uri.fromFile(outputFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPictureUri);
        startActivityForResult(intent, Constants.StartActivityTag.CAPTURE_PICTURE_FOR_NEW_REQUEST.ordinal());
    }


    private void addNewRequest() {

        // TODO: this method should send a proper request to the proper URL

        File imgFile = new File(mCameraPictureAbsolutePath);
        if (!imgFile.exists()) {
            Log.e(LOG_TAG, "the referenced image file does not exist: " + mCameraPictureAbsolutePath);
            return;
        }

        HashMap<String, String> paramMap = new HashMap<String, String>(2);
        paramMap.put("username", Constants.USERNAME);
        paramMap.put("password", Constants.PASSWORD);

        Main.sHttpTaskExecutor.executePost(Constants.HttpTaskTag.NEW_REQUEST, null, Constants.IMGTEST_URI, paramMap, imgFile);
    }

    private static class ViewHolder {
        ImageView imageView;
    }

    private class NewRequestPicturesAdapter extends ArrayAdapter<String> {

        private final static String LOG_TAG = "NewRequestPicturesAdapter";

        private LayoutInflater mInflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        public NewRequestPicturesAdapter(Context context, int resource) {
            super(context, resource);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.request_images_list_item, parent, false);
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
