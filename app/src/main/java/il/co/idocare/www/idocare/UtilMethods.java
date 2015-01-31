package il.co.idocare.www.idocare;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class UtilMethods {

    private final static String LOG_TAG = "UtilMethods";


    private final static String JSON_TAG_REQUESTS = "data";


    // Target size of new images captured inside the app
    public final static int NEW_CAMERA_PICTURE_WIDTH = 1024;
    public final static int NEW_CAMERA_PICTURE_HEIGHT = 768;

    // Quality factor for camera picture compression (0-100, the higher the better quality)
    public final static int NEW_CAMERA_PICTURE_COMPRESSION_QUALITY = 50;



    /**
     * Count lines in string.
     * @param string it is assumed that line terminator is either \n or \r or \r\n, but not a mix of them.
     * @return number of lines in this string
     */
    public static int countLines(String string) {
        int lines = 1;
        int pos = 0;
        while ((pos = Math.max(string.indexOf("\n", pos), string.indexOf("\r", pos)) + 1) != 0) {
            lines++;
        }
        return lines;
    }


    /**
     * This method parses the JSON formatted text obtained from the server and extract the
     * details of the requests embedded in that text
     * @param jsonData
     * @return
     */
    public static List<RequestItem> extractRequestsFromJSON(String jsonData) {

        ArrayList<RequestItem> requestItemsList = new ArrayList<RequestItem>();

        if (jsonData == null || jsonData.length() <= 0) {
            Log.e(LOG_TAG, "jsonData is null or empty");
            return requestItemsList;
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonData);

            // Getting JSON Array
            JSONArray requestsArray = jsonObj.getJSONArray(JSON_TAG_REQUESTS);

            if (requestsArray != null && requestsArray.length() > 0) {

                JSONObject request;
                RequestItem requestItem;

                for (int i = 0; i < requestsArray.length(); i++) {

                    request = requestsArray.getJSONObject(i);
                    requestItem = RequestItem.createRequestItem(request);

                    if (requestItem != null) {
                        requestItemsList.add(requestItem);
                    } else {
                        Log.e(LOG_TAG, "couldn't build request item!");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return requestItemsList;
    }

    /**
     * Adjust the raw JPEG picture taken by the camera. The adjustments are:<br>
     * 1. Compression
     * @param absPathToThePicture absolute path to the picture file (its contents will be overwriten)
     */
    public static void adjustCameraPicture(String absPathToThePicture) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        // Discover the current dimensions of the picture
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absPathToThePicture, options);

        // Calculate inSampleSize
        options.inSampleSize =
                calculateInSampleSize(options, NEW_CAMERA_PICTURE_WIDTH, NEW_CAMERA_PICTURE_HEIGHT);

        // Decode (smaller) bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(absPathToThePicture, options);

        if (bitmap == null) {
            Log.e(LOG_TAG, "couldn't decode bitmap from: " + absPathToThePicture);
            return;
        }

        Log.d(LOG_TAG, "bitmap height: " + bitmap.getHeight() + "  " + "bitmap width: " + bitmap.getWidth());

        OutputStream stream = null;
        try {
            stream = new FileOutputStream(absPathToThePicture);
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, NEW_CAMERA_PICTURE_COMPRESSION_QUALITY, stream)) {
                Log.e(LOG_TAG, "couldn't compress picture from: " + absPathToThePicture);
            }
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Try to free bitmap's memory by making it "attractive" to GC
        bitmap.recycle();
        bitmap = null;

        File file = new File(absPathToThePicture);
        Log.d(LOG_TAG, "Created new picture file of length " + file.length() + "B" + " at " + absPathToThePicture);
    }

    /**
     * This method is used to calculate what value of inSampleSize parameter of
     * BitmapFactory.Options object should be used when decoding a bitmap from JPEG file
     * @param options options object which has already been passed to BitmapFactory.decode while its
     *                inJustDecodeBounds parameter set to true
     * @param reqWidth target width
     * @param reqHeight target height
     * @return maximal value of inSampleSize that can be used such that decoded bitmap is still
     *         larger than target parameters
     */
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
