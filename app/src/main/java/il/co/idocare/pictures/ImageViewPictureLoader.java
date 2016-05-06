package il.co.idocare.pictures;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.MalformedURLException;
import java.net.URL;

import il.co.idocare.Constants;

/**
 * Created by Vasiliy on 5/6/2016.
 */
public class ImageViewPictureLoader {

    public void loadFromWebOrFile(@NonNull ImageView targetImageView,
                                  String uri,
                                  @DrawableRes int defaultDrawableResId) {

        if (uri != null && uri.length() > 0) {
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
        } else {
            targetImageView.setImageResource(defaultDrawableResId);
        }
    }
}
