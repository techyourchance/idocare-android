package il.co.idocare.pictures;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class handles interactions with device camera
 */
public class CameraAdapter {

    private Activity mActivity;

    public CameraAdapter(Activity activity) {
        mActivity = activity;
    }


    /**
     * This method invokes {@link Activity#startActivityForResult(android.content.Intent, int)}
     * with the provided request code in order to take a picture with device's camera.
     * @param requestCode a request code to be used when starting a camera activity
     * @param pictureNamePrefix if not null, this string will be prepended to picture's name
     * @return an absolute path to the file in which the picture will be stored
     */
    public String takePicture(int requestCode, @Nullable String pictureNamePrefix) {
        String currDateTime =
                new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault()).format(new Date());

        String pictureName = pictureNamePrefix != null ? pictureNamePrefix + currDateTime : currDateTime;

        File outputFile = new File(mActivity
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES), pictureName + ".jpg");

        Uri cameraPictureUri = Uri.fromFile(outputFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPictureUri);
        mActivity.startActivityForResult(intent, requestCode);

        return outputFile.getAbsolutePath();
    }


}
