package il.co.idocare.pictures;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.MalformedURLException;
import java.net.URL;

import il.co.idocare.Constants;

/**
 * This class is responsible for populating ImageViews with pictures
 */
public class ImageViewPictureLoader {

    public void loadFromWebOrFile(@NonNull ImageView targetImageView,
                                  @Nullable String uri,
                                  @DrawableRes int defaultDrawableResId) {

        if (uri != null && uri.length() > 0) {
            loadFromWebOrFile(targetImageView, uri);
        } else {
            targetImageView.setImageResource(defaultDrawableResId);
        }
    }

    public void loadFromWebOrFile(@NonNull ImageView targetImageView, @NonNull String uri) {
        try {
            new URL(uri);
        } catch (MalformedURLException e) {
            // The exception means that the current Uri is not a valid URL - it is local
            // uri and we need to adjust it to the scheme recognized by UIL
            uri = "file://" + uri;
        }

        ImageLoader.getInstance().displayImage(
                uri,
                targetImageView,
                Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS);
    }

    /**
     * Cancel the loading of any image into target ImageView. Has no effect if no loading is in
     * process.
     */
    public void cancelLoading(@NonNull ImageView targetImageView) {
        ImageLoader.getInstance().cancelDisplayTask(targetImageView);
    }
}
