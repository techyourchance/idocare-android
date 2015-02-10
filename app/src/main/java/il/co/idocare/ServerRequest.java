package il.co.idocare;


import android.os.AsyncTask;
import android.util.Log;



import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

public class ServerRequest {


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
        public void serverResponse(boolean responseStatusOk, Constants.ServerRequestTag tag, String responseData);
    }

    private final static String LOG_TAG = "ServerRequest";

    /**
     * Http method selector enum
     */
    public enum HttpMethod { GET, POST }

    Constants.ServerRequestTag mTag;
    String mUrl;
    OnServerResponseCallback mCallback;
    HttpMethod mHttpMethod;

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
    public ServerRequest (String url, Constants.ServerRequestTag tag, OnServerResponseCallback callback) {
        mUrl = url;
        mTag = tag;
        mCallback = callback;
        mHttpMethod = HttpMethod.POST;
        mRequestTextFields = new HashMap<String, String>();
        mRequestPicturesFields = new HashMap<String, Map<String, String>>();

        Log.d(LOG_TAG, "creating new server request:"
                + "\nURL: "         + (mUrl != null ? mUrl : "null")
                + "\nRequest tag: " + (mTag != null ? mTag.toString() : "null")
                + "\nCallback: "    + (mCallback != null ? mCallback.toString() : "null")
                + "\nMethod: "      + (mHttpMethod != null ? mHttpMethod.toString() : "null") );
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
     * Execute this server request
     */
    public void execute() {
        HttpTask httpTask = new HttpTask(mTag, mHttpMethod, mCallback,
                mRequestTextFields, mRequestPicturesFields);
        httpTask.execute(mUrl);
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
        private Constants.ServerRequestTag mTag;

        private boolean mResponseStatusOk = false;

        protected HttpTask (Constants.ServerRequestTag tag, HttpMethod httpMethod,
                            OnServerResponseCallback callback) {
            this(tag, httpMethod, callback, null, null);
        }

        protected HttpTask (Constants.ServerRequestTag tag, HttpMethod httpMethod,
                            OnServerResponseCallback callback, Map<String, String> textFieldsMap) {
            this(tag, httpMethod, callback, textFieldsMap, null);
        }


        protected HttpTask (Constants.ServerRequestTag tag, HttpMethod httpMethod,
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
