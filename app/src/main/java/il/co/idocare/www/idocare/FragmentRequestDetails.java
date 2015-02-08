package il.co.idocare.www.idocare;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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


public class FragmentRequestDetails extends IDoCareFragment {

    private final static String LOG_TAG = "FragmentRequestDetails";

    RequestPicturesAdapter mListAdapter;
    RequestItem mRequestItem;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_details, container, false);

        mListAdapter = new RequestPicturesAdapter(getActivity(), 0);
        ListView listPictures = (ListView) view.findViewById(R.id.list_request_pictures);
        listPictures.setAdapter(mListAdapter);

        Bundle args = getArguments();
        if (args != null) {
            mRequestItem = (RequestItem) args.getParcelable("requestItem");
        }

        if (mRequestItem == null) {
            // TODO: handle this error somehow
            return view;
        }

        populateChildViewsFromRequestItem(view);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void populateChildViewsFromRequestItem(View view) {

        if (mRequestItem.mOpenedBy != null) {
            TextView openedBy = (TextView) view.findViewById(R.id.txt_opened_by);
            openedBy.setText("Opened by: " + mRequestItem.mOpenedBy);
        }

        if (mRequestItem.mCreationDate != null) {
            TextView creationDate = (TextView) view.findViewById(R.id.txt_creation_date);
            creationDate.setText(mRequestItem.mCreationDate);
        }

        if (mRequestItem.mImagesBefore != null) {
            mListAdapter.addAll(mRequestItem.mImagesBefore);
            mListAdapter.notifyDataSetChanged();
        }

        if (mRequestItem.mNoteBefore != null) {
            TextView commentBefore = (TextView) view.findViewById(R.id.txt_comment_before);
            commentBefore.setLines(UtilMethods.countLines(mRequestItem.mNoteBefore));
            commentBefore.setText(mRequestItem.mNoteBefore);
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

    private static class ViewHolder {
        ImageView imageView;
    }

    private class RequestPicturesAdapter extends ArrayAdapter<String> {

        private LayoutInflater mInflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        public RequestPicturesAdapter(Context context, int resource) {
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

            ImageLoader.getInstance().displayImage(getItem(position), holder.imageView, animateFirstListener);

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
