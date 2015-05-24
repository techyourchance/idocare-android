package il.co.idocare.connectivity;


import android.os.AsyncTask;
import android.util.Log;



import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpEntityEnclosingRequestBase;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
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

public class ServerRequest {


    private final static String DEV_ROOT_URL = "http://dev-04.idocare.co.il";
    private final static String QA_ROOT_URL = "http://qa-04.idocare.co.il";

    // URLs used to issue requests to the server
    public final static String LOGIN_URL = DEV_ROOT_URL + "/api-04/user/login";
    public final static String GET_USER_DATA_URL = DEV_ROOT_URL + "/api-04/user/get";
    public final static String ADD_USER_URL = DEV_ROOT_URL + "/api-04/user/add";
    public final static String GET_ALL_ARTICLES_URL = DEV_ROOT_URL + "/api-04/article";
    public final static String VOTE_REQUEST_URL = DEV_ROOT_URL + "/api-04/request/vote";
    public final static String VOTE_ARTICLE_URL = DEV_ROOT_URL + "/api-04/article/vote";
    public final static String GET_ALL_REQUESTS_URL = DEV_ROOT_URL + "/api-04/request";
    public final static String CREATE_REQUEST_URL = DEV_ROOT_URL + "/api-04/request/add";
    public final static String PICKUP_REQUEST_URL = DEV_ROOT_URL + "/api-04/request/pickup";
    public final static String CLOSE_REQUEST_URL = DEV_ROOT_URL + "/api-04/request/close";
    public final static String GET_REQUEST_URL = DEV_ROOT_URL + "/api-04/request/get";


    /**
     * This enum is used in order to indicate which request should be initiated and
     * is also used as an asynchronous completion token in order to identify
     * the request while processing its response
     */
    public enum ServerRequestTag {GET_ALL_REQUESTS, REQUEST_DETAILS, CREATE_REQUEST,
        PICKUP_REQUEST, CLOSE_REQUEST, VOTE_FOR_REQUEST, GET_USER_DATA, LOGIN}


    /**
     * Classes implementing this interface are eligible to be used as callback targets once
     * server response for a particular request is received
     */
    public interface OnServerResponseCallback {
        /**
         * This method will be called by ServerRequest object once the server response is received
         * @param responseStatusOk whether the status of the response was OK (2**)
         * @param tag the tag of the server request
         * @param responseData the body of the received http response
         */
        public void serverResponse(boolean responseStatusOk, ServerRequestTag tag, String responseData);
    }

    private final static String LOG_TAG = "ServerRequest";

    /**
     * Http method selector enum
     */
    public enum HttpMethod { GET, POST }

    private ServerRequestTag mTag;
    private String mUrl;
    private OnServerResponseCallback mCallback;
    private HttpMethod mHttpMethod;
    private HttpTask mHttpTask;
    private String mResponseData;

    /**
     * Headers to be added to HTTP request
     */
    Map<String, String> mRequestHeaders;

    /**
     * Text fields to be added to HTTP request
     */
    Map<String, String> mRequestTextFields;

    /**
     * Images fields to be added to HTTP request
     */
    Map<String, Map<String, String>> mRequestPicturesFields;


    /**
     * Create new server request
     * @param url target URL for this request
     */
    public ServerRequest (String url) {
        this(url, null, null);
    }

    /**
     * Create new server request and register callback object
     * @param url target URL for this request
     * @param tag this tag will be provided alongside server response data on callback call
     * @param callback callback object to be used when the response is available
     */
    public ServerRequest (String url, ServerRequestTag tag, OnServerResponseCallback callback) {
        mUrl = url;
        mTag = tag;
        mCallback = callback;
        mHttpMethod = HttpMethod.POST;
        mRequestHeaders = new HashMap<String, String>();
        mRequestTextFields = new HashMap<String, String>();
        mRequestPicturesFields = new HashMap<String, Map<String, String>>();

    }

    /**
     * Add text name/value pair to this server request
     * @param fieldName name of HTTP field this text should be passed in
     * @param fieldValue the text to be passed
     */
    public void addTextField (String fieldName, String fieldValue) {
        if (!mRequestTextFields.containsKey(fieldName)) {
            mRequestTextFields.put(fieldName, fieldValue);
        } else {
            Log.e(LOG_TAG, "aborting an overwrite of the existing text field: " + fieldName);
        }
    }


    /**
     * Add picture to this server request
     * @param fieldName name of HTTP field this picture should be passed in
     * @param pictureName the name of the picture
     * @param uri local URI of the ficture
     */
    public void addPicture (String fieldName, String pictureName, String uri) {
        Map<String, String> fieldMap;

        if (mRequestPicturesFields.containsKey(fieldName)) {
            fieldMap = mRequestPicturesFields.get(fieldName);
        } else {
            fieldMap = new HashMap<String, String>();
            mRequestPicturesFields.put(fieldName, fieldMap);
        }


        // Report if there was name collision
        if (fieldMap.containsKey(pictureName))
            Log.e(LOG_TAG, "An existing picture was overwritten. Field: " + fieldName
                        + " Name: " + pictureName);

        fieldMap.put(pictureName, uri);
    }

    /**
     * Add custom header to this HTTP request
     * @param name header name
     * @param value header value
     */
    public void addHeader(String name, String value) {
        mRequestHeaders.put(name, value);
    }

    /**
     * Execute this server request. This method returns immediately.
     */
    public void execute() {
        if (mHttpTask != null) {
            throw new IllegalStateException("ServerRequest can't be executed more than once");
        }

        mHttpTask = new HttpTask(mTag, mHttpMethod, mCallback,
                mRequestTextFields, mRequestPicturesFields);
        // TODO: maybe this can be optimized in some way?
        mHttpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUrl);

        logServerRequestInfo();
    }

    /**
     * Execute this server request. This method blocks until the request is executed.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void blockingExecute() throws ExecutionException, InterruptedException {
        execute();
        // TODO: maybe it will be better to use get() with timeout in this method?
        mHttpTask.get();
    }


    /**
     * Get the response obtained for this request. This method will block until the response is received.
     * @return the server response
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String getResponseData() throws ExecutionException, InterruptedException {
        if (mHttpTask == null) {
            throw new IllegalStateException("ServerRequest must be executed before trying to get " +
                    "the response data");
        }
        // TODO: maybe it will be better to use get() with timeout in this method?
        return mHttpTask.get();
    }


    private void logServerRequestInfo() {

        StringBuilder stringBuilder = new StringBuilder(400);

        stringBuilder.append("Executing new server request:").append("\n")
                .append("URL: ").append((mUrl != null ? mUrl : "null")).append("\n")
                .append("Method: ").append(mHttpMethod != null ? mHttpMethod.toString() : "null").append("\n");

        stringBuilder.append("Headers: ").append("\n");
        for (String headerName : mRequestHeaders.keySet()) {
            stringBuilder.append("\t").append(headerName).append(" : ")
                    .append(mRequestHeaders.get(headerName)).append("\n");
        }

        if (mRequestTextFields.size() > 0) {
            stringBuilder.append("Fields: ").append("\n");
            for (String fieldName : mRequestTextFields.keySet()) {
                stringBuilder.append("\t").append(fieldName).append(" : ")
                        .append(mRequestTextFields.get(fieldName)).append("\n");
            }
        }

        if (mRequestPicturesFields.size() > 0) {
            stringBuilder.append("Pictures: ").append("\n");
            for (String pictureField : mRequestPicturesFields.keySet()) {
                stringBuilder.append("\t").append("Field name: ").append(pictureField).append("\n");
                for (String pictureName : mRequestPicturesFields.get(pictureField).keySet()) {
                    stringBuilder.append("\t\t").append(pictureName).append(" : ")
                            .append(mRequestPicturesFields.get(pictureField).get(pictureName))
                            .append("\n");
                }
            }
        }

        stringBuilder.append("Request tag: ").append(mTag != null ? mTag.toString() : "null").append("\n");
        stringBuilder.append("Callback: ").append(mCallback != null ? mCallback.toString() : "null").append("\n");


        Log.d(LOG_TAG, stringBuilder.toString());
    }


    // ---------------------------------------------------------------------------------------------
    //
    // Inner classes
    //
    // ---------------------------------------------------------------------------------------------


    private class HttpTask extends AsyncTask<String, Void, String> {

        private static final String LOG_TAG = "HttpTask";


        private HttpMethod mHttpMethod;
        private Map<String, String> mTextFieldsMap;
        private Map<String, Map<String, String>> mPicturesFieldsMap;
        private OnServerResponseCallback mCallback;
        private ServerRequestTag mTag;

        private boolean mResponseStatusOk = false;

        protected HttpTask (ServerRequestTag tag, HttpMethod httpMethod,
                            OnServerResponseCallback callback) {
            this(tag, httpMethod, callback, null, null);
        }

        protected HttpTask (ServerRequestTag tag, HttpMethod httpMethod,
                            OnServerResponseCallback callback, Map<String, String> textFieldsMap) {
            this(tag, httpMethod, callback, textFieldsMap, null);
        }


        protected HttpTask (ServerRequestTag tag, HttpMethod httpMethod,
                            OnServerResponseCallback callback, Map<String, String> textFieldsMap,
                            Map<String, Map<String, String>> picturesMap) {
            mTag = tag;
            mHttpMethod = httpMethod;
            mCallback = callback;
            mTextFieldsMap = textFieldsMap;
            mPicturesFieldsMap = picturesMap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... uris) {


            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            StringBuilder httpResponseBuffer = new StringBuilder();
            HttpUriRequest httpRequest;

            for (String uri : uris) {

                httpRequest = null;

                // Setting the correct method
                switch (mHttpMethod) {
                    case GET:
                        httpRequest = new HttpGet(uri);
                        break;
                    case POST:
                        httpRequest = new HttpPost(uri);
                        break;
                }

                // Adding headers
                for( String headerName : mRequestHeaders.keySet()) {
                    httpRequest.addHeader(headerName, mRequestHeaders.get(headerName));
                }

                // Adding an entity (if required)
                HttpEntity httpEntity = createHttpEntity();
                String httpEntityBody = "";

                if (httpEntity != null) {
                    try {
                        HttpEntityEnclosingRequestBase entityEnclosingRequest = (HttpEntityEnclosingRequestBase) httpRequest;
                        entityEnclosingRequest.setEntity(httpEntity);
                        httpEntityBody = httpEntity.toString(); //getHttpEntityBody(httpEntity);
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                }


                Log.d(LOG_TAG, "Executing http " + mHttpMethod.toString() + " to " + uri);

                // Executing the request
                try {

                    HttpResponse httpResponse = httpClient.execute(httpRequest);

                    Log.d(LOG_TAG, "Got a response. Status: " + httpResponse.getStatusLine().toString());

                    if (httpResponse.getStatusLine().getStatusCode() == 200 ) {

                        mResponseStatusOk = true;

                        String responseData = EntityUtils.toString(httpResponse.getEntity());

                        Log.d(LOG_TAG, "The content of the response is:\n" + responseData);

                        httpResponseBuffer.append(responseData).append("\n");
                    }

                } catch (ClassCastException e) {
                    e.printStackTrace();
                } catch (IOException e ) {
                    e.printStackTrace();
                } finally {
                    // Close the client
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (httpResponseBuffer.length() > 0) {
                return httpResponseBuffer.toString();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String responseData) {
            ServerRequest.this.mResponseData = responseData;

            if (mCallback != null) {
                mCallback.serverResponse(mResponseStatusOk, mTag, responseData);
            }
        }

        /**
         * This method creates an appropriate entity for the request
         * @return
         */
        protected HttpEntity createHttpEntity() {
            HttpEntity httpEntity = null;

            // Use multipart body if pictures should be attached
            boolean isMultipart = (mPicturesFieldsMap != null && mPicturesFieldsMap.size() > 0);

            if (isMultipart ) {
                MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();

                // Create text fields
                if (mTextFieldsMap != null) {
                    for (String name : mTextFieldsMap.keySet()) {
                        multipartEntity.addPart(name, new StringBody(mTextFieldsMap.get(name), ContentType.TEXT_PLAIN));
                    }
                }


                // For each field name
                for (String fieldName : mPicturesFieldsMap.keySet()) {
                    int i = 0;
                    Map<String, String> field = mPicturesFieldsMap.get(fieldName);
                    // For each picture in the field
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

            } else if (mTextFieldsMap != null) {
                try {
                    ArrayList<NameValuePair> nameValuePairs= new ArrayList<NameValuePair>();

                    for (String name : mTextFieldsMap.keySet()) {
                        nameValuePairs.add(new BasicNameValuePair(name, mTextFieldsMap.get(name)));
                    }

                    httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }


            return httpEntity;
        }


        /**
         * This method returns the actual contents of the entity object
         * @param entity
         * @return
         */
        protected String getHttpEntityBody(HttpEntity entity) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream((int)entity.getContentLength());
            try {
                entity.writeTo(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String entityContentAsString = new String(out.toByteArray());
            return entityContentAsString;
        }

    }
}
