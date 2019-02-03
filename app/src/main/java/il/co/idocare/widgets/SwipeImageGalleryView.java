package il.co.idocare.widgets;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import il.co.idocare.R;
import il.co.idocarecore.pictures.ImageViewPictureLoader;

/**
 * This View is a "swipeable" images gallery that has "bullets" indicators of total images count
 * and currently selected image.
 */
public class SwipeImageGalleryView extends FrameLayout implements ViewPager.OnPageChangeListener {

    private SwipeImageGalleryAdapter mSwipeImageGalleryAdapter = new SwipeImageGalleryAdapter();
    private ImageViewPictureLoader mImageViewPictureLoader = new ImageViewPictureLoader();

    private ViewPager mViewPager;
    private LinearLayout mIndicatorsHolderLayout;

    private int mCurrentlyShownImagePosition = 0;

    public SwipeImageGalleryView(Context context) {
        super(context);
        init();
    }

    public SwipeImageGalleryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeImageGalleryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SwipeImageGalleryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.element_swipe_image_gallery, this);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mIndicatorsHolderLayout = (LinearLayout) findViewById(R.id.indicators_holder);

        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(mSwipeImageGalleryAdapter);
    }

    /**
     * Add a single picture from path to this gallery
     */
    public void addPicture(@NonNull String picturePath) {
        mSwipeImageGalleryAdapter.mPicturesPaths.add(picturePath);
        mSwipeImageGalleryAdapter.notifyDataSetChanged();
    }

    /**
     * Add all the pictures from paths specified in the list to this gallery
     */
    public void addPictures(@NonNull List<String> picturesPaths) {
        mSwipeImageGalleryAdapter.mPicturesPaths.addAll(picturesPaths);
        mSwipeImageGalleryAdapter.notifyDataSetChanged();
    }

    /**
     * Add all the pictures from paths specified in the array to this gallery
     */
    public void addPictures(@NonNull String[] picturesPaths) {
        addPictures(Arrays.asList(picturesPaths));
    }

    private void updateImagesIndicators() {
        mIndicatorsHolderLayout.removeAllViews();

        int horizontalMargin = getContext().getResources()
                .getDimensionPixelOffset(R.dimen.swipe_image_gallery_indicators_margin);

        ImageView imageView;
        for (int i = 0; i < mSwipeImageGalleryAdapter.mPicturesPaths.size(); i++) {
            imageView = new ImageView(getContext());


            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(horizontalMargin, lp.topMargin, horizontalMargin, lp.bottomMargin);

            imageView.setLayoutParams(lp);

            if (i == mCurrentlyShownImagePosition) {
                imageView.setImageResource(R.drawable.ic_image_gallery_indicator_selected);
            } else {
                imageView.setImageResource(R.drawable.ic_image_gallery_indicator);
            }

            mIndicatorsHolderLayout.addView(imageView);
        }
    }


    public void clear() {
        mSwipeImageGalleryAdapter.mPicturesPaths.clear();
        mSwipeImageGalleryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentlyShownImagePosition = position;
        updateImagesIndicators();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }



    //----------------------------------------------------------------------------------------------
    //
    // Custom adapter for ViewPager
    //
    //----------------------------------------------------------------------------------------------

    private class SwipeImageGalleryAdapter extends PagerAdapter {

        private List<String> mPicturesPaths = new ArrayList<>();

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            mImageViewPictureLoader.loadFromWebOrFile(imageView, mPicturesPaths.get(position));

            container.addView(imageView);

            // this is a workaround for bundling ImageView with path
            imageView.setTag(mPicturesPaths.get(position));

            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView imageView = (ImageView) object;
            mImageViewPictureLoader.cancelLoading(imageView);
            container.removeView(imageView);
        }

        @Override
        public int getCount() {
            return mPicturesPaths.size();
        }

        @Override
        public int getItemPosition(Object object) {
            int position = mPicturesPaths.indexOf((String) ((ImageView)object).getTag());
            if (position == -1) {
                return POSITION_NONE;
            } else {
                return position;
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            updateImagesIndicators();
        }
    }
}
