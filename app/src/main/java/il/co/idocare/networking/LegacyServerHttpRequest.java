package il.co.idocare.networking;


import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpEntityEnclosingRequestBase;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpUriRequest;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;
import ch.boye.httpclientandroidlib.impl.client.CloseableHttpClient;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import il.co.idocare.Constants;

public class LegacyServerHttpRequest implements Runnable {

    /**
     * Classes implementing this interface are eligible to be used as callback targets once
     * server response for a particular request is received
     */
    public interface OnServerResponseCallback {
        /**
         * This callback method will be called by ServerRequest object once the response from
         * the server will be received
         *
         * @param statusCode   status code of server response
         * @param reasonPhrase reason phrase for the status code of server response
         * @param entityString entity part of server response
         * @param asyncCompletionToken the Object passed to the ServerRequest which calls this
         *                             callback at creation time
         */
        public void serverResponse(int statusCode, String reasonPhrase, String entityString,
                                   Object asyncCompletionToken);
    }

    /**
     * Http method selector enum
     */
    public enum HttpMethod {
        GET, POST
    }


    private final static String LOG_TAG = LegacyServerHttpRequest.class.getSimpleName();

    private String mUserId;
    private String mAuthToken;
    private OnServerResponseCallback mCallback;
    private Object mAsyncCompletionToken;

    private HttpUriRequest mHttpRequest;

    private Map<String, String> mTextFields;
    private Map<String, Map<String, String>> mPicturesFields;


    public LegacyServerHttpRequest(String url, String userId, String authToken,
                                   OnServerResponseCallback callback, Object asyncCompletionToken) {
        mUserId = userId;
        mAuthToken = authToken;
        mCallback = callback;
        mAsyncCompletionToken = asyncCompletionToken;

        // We have only POSTs for now
        mHttpRequest =  new HttpPost(url);
    }

    @Override
    public void run() {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        StringBuilder httpResponseBuffer = new StringBuilder();

        // Adding an entity (if required)
        HttpEntity httpEntity = createHttpEntity();

        if (httpEntity != null) {
            try {
                HttpEntityEnclosingRequestBase entityEnclosingRequest =
                        (HttpEntityEnclosingRequestBase) mHttpRequest;
                entityEnclosingRequest.setEntity(httpEntity);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        CloseableHttpResponse httpResponse = null;
        String responseEntityString = "";

        //noinspection TryWithIdenticalCatches
        try {
            httpResponse = httpClient.execute(mHttpRequest);

            try {
                responseEntityString = EntityUtils.toString(httpResponse.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpResponse.close();
            }


        } catch (ClientProtocolException e) {
            e.printStackTrace();
            /*
            Let the callback know that this server request failed
            TODO: this is an error case. Make sure that the callback treats it as such!!!
             */
            mCallback.serverResponse(0, "", "", mAsyncCompletionToken);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            /*
            Let the callback know that this server request failed
            TODO: this is an error case. Make sure that the callback treats it as such!!!
             */
            mCallback.serverResponse(0, "", "", mAsyncCompletionToken);
            return;
        } finally {
            try {
                if (httpClient != null) httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mCallback.serverResponse(
                httpResponse.getStatusLine().getStatusCode(),
                httpResponse.getStatusLine().getReasonPhrase(),
                responseEntityString,
                mAsyncCompletionToken);


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
        if (TextUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("the name of the field must be non-empty");
        }
        if (TextUtils.isEmpty(pictureName)) {
            throw new IllegalArgumentException("the name of the picture must be non-empty");
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
     * Add a new header to Http request.
     */
    public void addHeader(String name, String value) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("the name of the header must be non-empty");
        }
        mHttpRequest.addHeader(name, value);
    }

    /**
     * Add "standard" headers to Http request. Standard headers are:<br>
     * User ID<br>
     * Authentication token<br>
     * Timestamp<br>
     * @throws IllegalStateException if this ServerRequest is not associated with a particular account
     */
    public void addStandardHeaders() throws IllegalStateException{
        if (TextUtils.isEmpty(mUserId) || TextUtils.isEmpty(mAuthToken)) {
            throw new IllegalStateException("can't add standard headers without user ID or auth token");
        }

        mHttpRequest.addHeader(Constants.HttpHeader.USER_ID.getValue(), mUserId);

        long timestamp = System.currentTimeMillis();
        String token = generateAuthToken(mUserId +
                mAuthToken +
                String.valueOf(timestamp));

        mHttpRequest.addHeader(Constants.HttpHeader.USER_TOKEN.getValue(), token);
        mHttpRequest.addHeader(Constants.HttpHeader.USER_TIMESTAMP.getValue(), String.valueOf(timestamp));
    }


    /**
     * This method generates the authentication token from a string of text
     * @param arg string of text from which auth token will be generated
     * @return auth token
     */
    private static String generateAuthToken(String arg) {
        MessageDigest digest=null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            byte[] resultBytes = digest.digest(arg.getBytes("UTF-8"));
            return (String
                    .format("%0" + (resultBytes.length * 2) + "X", new BigInteger(1, resultBytes)))
                    .toLowerCase();
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method creates an appropriate entity for the request
     */
    private HttpEntity createHttpEntity() {

        HttpEntity httpEntity = null;

        // Use multipart body if pictures should be attached
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