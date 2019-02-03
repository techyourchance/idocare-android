package il.co.idocarecore.networking;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import il.co.idocarecore.utils.Logger;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class FilesDownloader {

    private static final String TAG = "FilesDownloader";

    private final CacheDirRetriever mCacheDirRetriever;
    private final GeneralApi mGeneralApi;
    private final Logger mLogger;

    public FilesDownloader(CacheDirRetriever cacheDirRetriever, GeneralApi generalApi, Logger logger) {
        mCacheDirRetriever = cacheDirRetriever;
        mGeneralApi = generalApi;
        mLogger = logger;
    }

    @WorkerThread
    @Nullable
    public String downloadFileAndStoreLocallySync(String fileUrl) {
        mLogger.d(TAG, "downloadFileAndStoreLocallySync() called; file URL: " + fileUrl);

        Call<ResponseBody> call = mGeneralApi.downloadFile(fileUrl);

        try {
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()) {
                mLogger.d(TAG, "server contacted and had the file");

                String fileName = extractFileNameFromUrl(fileUrl);
                String fileUri = writeResponseBodyToAppCache(response.body(), fileName);

                if (fileUri != null) {
                    mLogger.d(TAG, "file stored successfully; local URI: " + fileUri);
                } else {
                    mLogger.e(TAG, "file storage failed");
                }

                return fileUri;
            } else {
                mLogger.e(TAG, "server contact failed");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractFileNameFromUrl(String fileUrl) {
        URI uri = null;
        try {
            uri = new URI(fileUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    @WorkerThread
    @Nullable
    private String writeResponseBodyToAppCache(ResponseBody body, String fileName) {
        try {
            String localFileUri = mCacheDirRetriever.getCacheDir() + File.separator + fileName;
            File localFile = new File(localFileUri);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(localFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                }

                outputStream.flush();

                return localFileUri;
            } catch (IOException e) {
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * The purpose of this class is to be an adapter (design pattern) between FilesDownloader and
     * Context. This removes dependency on Context from FilesDownloader.
     */
    public static class CacheDirRetriever {

        private final Context mContext;

        public CacheDirRetriever(Context context) {
            mContext = context;
        }

        public File getCacheDir() {
            return mContext.getCacheDir();
        }
    }

}
