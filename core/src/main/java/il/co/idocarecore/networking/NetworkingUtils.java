package il.co.idocarecore.networking;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class NetworkingUtils {

    private static final String MULTIPART_FORM_DATA = "multipart/form-data";


    private NetworkingUtils() {}


    public static RequestBody createPictureBody(String pictureUri) {
        File pictureFile = new File(pictureUri);
        return RequestBody.create(MediaType.parse("image/*"), pictureFile);
    }

    public static RequestBody createStringBody(String string) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), string);
    }

    public static MultipartBody.Builder addPicturesParts(MultipartBody.Builder builder, List<String> pictures, String fieldName) {

        String pictureUri;
        File pictureFile;

        for (int i = 0; i < pictures.size(); i ++) {
            pictureUri = pictures.get(i);
            pictureFile = new File(pictureUri);

            if (pictureFile.exists()) {
                builder.addFormDataPart(
                        fieldName + "[" + i + "]",
                        pictureFile.getName(),
                        RequestBody.create(MediaType.parse("image/*"), pictureFile));
            } else {
                throw new RuntimeException("picture file doesn't exist: " + pictureFile);
            }
        }

        return builder;
    }
}
