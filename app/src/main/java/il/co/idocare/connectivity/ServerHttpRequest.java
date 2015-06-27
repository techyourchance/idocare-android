package il.co.idocare.connectivity;


import android.accounts.Account;
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
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
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

public class ServerHttpRequest implements Runnable {



    // URLs used to issue requests to the server
    public final static String LOGIN_URL = Constants.ROOT_URL + "/api-04/user/login";
    public final static String GET_USER_DATA_URL = Constants.ROOT_URL + "/api-04/user/get";
    public final static String ADD_USER_URL = Constants.ROOT_URL + "/api-04/user/add";
    public final static String GET_ALL_ARTICLES_URL = Constants.ROOT_URL + "/api-04/article";
    public final static String GET_REQUEST_URL = Constants.ROOT_URL + "/api-04/request/get";


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


    private final static String LOG_TAG = ServerHttpRequest.class.getSimpleName();

    private Account mAccount;
    private String mAuthToken;
    private OnServerResponseCallback mCallback;
    private Object mAsyncCompletionToken;

    private HttpUriRequest mHttpRequest;

    private Map<String, String> mTextFields;


    public ServerHttpRequest(String url, Account account, String authToken,
                             OnServerResponseCallback callback, Object asyncCompletionToken) {
        mAccount = account;
        mAuthToken = authToken;
        mCallback = callback;
        mAsyncCompletionToken = asyncCompletionToken;

        // We have only POSTs for now
        mHttpRequest =  new HttpPost(url);
    }

    @Override
    public void run() {

        // TODO: add text fields and pictures

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

        HttpResponse httpResponse = null;
        String responseEntityString = "";

        //noinspection TryWithIdenticalCatches
        try {
            httpResponse = httpClient.execute(mHttpRequest);

            try {
                responseEntityString = EntityUtils.toString(httpResponse.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
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
    public void addTextField(String name, String value) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("the name of the text field must be non-empty");
        }

        if (mTextFields == null)
            mTextFields = new HashMap<>(1);

        mTextFields.put(name, value);

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
        if (mAccount == null) {
            throw new IllegalStateException("can't add standard headers because there is no " +
                    "associated account.");
        }

        String accountId = mAccount.name;

        mHttpRequest.addHeader(Constants.HttpHeader.USER_ID.getValue(), accountId);

        long timestamp = System.currentTimeMillis();
        String token = generateAuthToken(accountId +
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
        
        
        // TODO: populate the maps from ACTION_PARAM field formatted as JSON
        Map<String, Map<String,String>> picturesFieldsMap = new HashMap<>();
        
        
        HttpEntity httpEntity = null;

        // Use multipart body if pictures should be attached
        boolean isMultipart = (picturesFieldsMap != null && picturesFieldsMap.size() > 0);

        if (isMultipart ) {
            MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();

            // Create text fields
            if (mTextFields != null) {
                for (String name : mTextFields.keySet()) {
                    multipartEntity.addPart(name, new StringBody(mTextFields.get(name), ContentType.TEXT_PLAIN));
                }
            }


            for (String fieldName : picturesFieldsMap.keySet()) {
                int i = 0;
                Map<String, String> field = picturesFieldsMap.get(fieldName);
                for (String pictureName : field.keySet()) {
                    String pictureUri = field.get(pictureName);
                    File pictureFile = new File(pictureUri);

                    if (pictureFile.exists()) {
                        multipartEntity.addBinaryBody(
                                fieldName + "[" + i + "]",
                                pictureFile, ContentType.create("image/jpeg"), pictureName);
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

                httpEntity = new UrlEncodedFormEntity(nameValuePairs);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        return httpEntity;
    }



}