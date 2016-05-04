package il.co.idocare.networking;


import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;
import ch.boye.httpclientandroidlib.impl.client.CloseableHttpClient;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class ServerHttpRequest {


    private final static String LOG_TAG = "ServerHttpRequest";


    private String mUrl;

    private Map<String, String> mHeaders;
    private Map<String, String> mTextFields;
    private Map<String, Map<String, String>> mPicturesFields;


    public ServerHttpRequest(String url) {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("parameter must be non-empty");
        mUrl = url;
    }

    public ServerHttpRequest() {
        mUrl = null;
    }

    public void setUrl(@NonNull String url) {
        mUrl = url;
    }

    public CloseableHttpResponse execute(@NonNull CloseableHttpClient httpClient) {

        // Currently we have only posts
        HttpPost request = new HttpPost(mUrl);

        // Adding headers (if required)
        if (mHeaders != null) {
            for (String headerName : mHeaders.keySet()) {
                request.addHeader(headerName, mHeaders.get(headerName));
            }
        }

        // Adding an entity (if required)
        HttpEntity httpEntity = createHttpEntity();
        if (httpEntity != null) {
            request.setEntity(httpEntity);
        }

        CloseableHttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(request);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpResponse;
    }

    /**
     * Add a text field to the body of this request
     */
    public void addTextField(String fieldName, String value) {
        if (TextUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("the name of the field must be non-empty");
        }

        if (mTextFields == null)
            mTextFields = new HashMap<>(1);

        mTextFields.put(fieldName, value);

    }

    /**
     * Add binary data of picture to the body of this request.
     * @param fieldName the name of the field in the body of the request which will contain the
     *                  picture. Picture fields are arrays - multiple pictures might be added
     *                  under the same fieldName
     * @param pictureName the name of the picture
     * @param uri local URI of the picture
     */
    public void addPictureField(String fieldName, String pictureName, String uri) {
        if (TextUtils.isEmpty(fieldName) || TextUtils.isEmpty(pictureName)
                || TextUtils.isEmpty(uri)) {
            throw new IllegalArgumentException("all parameters must be non-empty");
        }

        if (mPicturesFields == null)
            mPicturesFields = new HashMap<>(1);

        Map<String, String> field;

        if (mPicturesFields.containsKey(fieldName)) {
            field = mPicturesFields.get(fieldName);
        } else {
            field = new HashMap<String, String>(1);
            mPicturesFields.put(fieldName, field);
        }

        field.put(pictureName, uri);

    }

    /**
     * Add a header to this request.
     */
    public void addHeader(String name, String value) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
            throw new IllegalArgumentException("all parameters must be non-empty");
        }

        if (mHeaders == null)
            mHeaders = new HashMap<>(1);

        mHeaders.put(name, value);
    }


    /**
     * This method creates an appropriate entity for the request
     */
    private HttpEntity createHttpEntity() {

        HttpEntity httpEntity = null;

        // Use multipart entity if pictures should be attached
        boolean isMultipart = (mPicturesFields != null && mPicturesFields.size() > 0);

        if (isMultipart ) {
            MultipartEntityBuilder multipartEntity =
                    MultipartEntityBuilder.create();

            // Create text fields
            if (mTextFields != null) {
                for (String name : mTextFields.keySet()) {
                    multipartEntity.addPart(name, new StringBody(mTextFields.get(name),
                            ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), "UTF-8")));
                }
            }


            for (String fieldName : mPicturesFields.keySet()) {
                int i = 0;
                Map<String, String> field = mPicturesFields.get(fieldName);
                for (String pictureName : field.keySet()) {
                    String pictureUri = field.get(pictureName);
                    File pictureFile = new File(pictureUri);

                    if (pictureFile.exists()) {
                        multipartEntity.addBinaryBody(
                                fieldName + "[" + i + "]",
                                pictureFile, ContentType.create("image/jpeg"), pictureName + ".jpg");
                    } else {
                        Log.e(LOG_TAG, "the picture file does not exist: " + pictureFile);
                    }
                    i++;
                }
            }
            httpEntity = multipartEntity.build();

        } else if (mTextFields != null) {
            try {
                ArrayList<NameValuePair> nameValuePairs= new ArrayList<NameValuePair>();

                for (String name : mTextFields.keySet()) {
                    nameValuePairs.add(new BasicNameValuePair(name, mTextFields.get(name)));
                }

                httpEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        return httpEntity;
    }



}