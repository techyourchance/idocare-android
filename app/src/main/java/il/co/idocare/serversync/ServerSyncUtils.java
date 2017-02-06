package il.co.idocare.serversync;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class ServerSyncUtils {
    private ServerSyncUtils() {}



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
