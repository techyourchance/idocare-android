package il.co.idocare.www.idocare;


import android.os.AsyncTask;
import android.util.Log;



import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

public class HttpTaskExecutor {

    private enum HttpMethod { GET, POST }

    public HttpTaskExecutor() {}

    /**
     * Execute http GET request
     * @param tag the tag of the task
     * @param callback callback object to be used when the response is available (can be null)
     * @param uri the URL to be used
     */
    public void executeGet(Constants.HttpTaskTag tag, HttpTaskDoneCallback callback, String uri) {
        HttpTask getTask = new HttpTask(tag, HttpMethod.GET, callback);
        getTask.execute(uri);
    }

    /**
     * Execute http POST request
     * @param tag the tag of the task
     * @param callback callback object to be used when the response is available (can be null)
     * @param uri the URL to be used
     */
    public void executePost(Constants.HttpTaskTag tag, HttpTaskDoneCallback callback, String uri) {
        HttpTask getTask = new HttpTask(tag, HttpMethod.POST, callback);
        getTask.execute(uri);
    }

    /**
     * Execute http POST request containing additional query strings (name/value pairs)
     * @param tag the tag of the task
     * @param callback callback object to be used when the response is available (can be null)
     * @param uri the URL to be used
     * @param nameValuePairs name/value pairs to be added to the request
     */
    public void executePost(Constants.HttpTaskTag tag, HttpTaskDoneCallback callback, String uri,
                            Map<String, String> nameValuePairs) {
        HttpTask getTask = new HttpTask(tag, HttpMethod.POST, callback, nameValuePairs);
        getTask.execute(uri);
    }


    /**
     * Execute http POST request containing additional query strings (name/value pairs) and
     * an image file.
     * @param tag the tag of the task
     * @param callback callback object to be used when the response is available (can be null)
     * @param uri the URL to be used
     * @param nameValuePairs name/value pairs to be added to the request
     */
    public void executePost(Constants.HttpTaskTag tag, HttpTaskDoneCallback callback, String uri,
                            Map<String, String> nameValuePairs, File imgFile) {
        HttpTask getTask = new HttpTask(tag, HttpMethod.POST, callback, nameValuePairs, imgFile);
        getTask.execute(uri);
    }

    /**
     * Classes implementing this interface are eligible to be used as callback targets once
     * http response for a requested request is received
     */
    public interface HttpTaskDoneCallback {
        /**
         * This method will be called once a particular task for which this callback was set is
         * done
         * @param tag the tag of the task
         * @param responseData the body of the received http response
         */
        public void httpTaskDone(Constants.HttpTaskTag tag, String responseData);
    }



    private class HttpTask extends AsyncTask<String, Void, String> {

        private static final String LOG_TAG = "HttpTask";


        private HttpMethod mHttpMethod;
        private Map<String, String> mParamMap;
        private HttpTaskDoneCallback mCallback;
        private Constants.HttpTaskTag mTag;
        private File mImgFile;

        protected HttpTask (Constants.HttpTaskTag tag, HttpMethod httpMethod,
                            HttpTaskDoneCallback callback) {
            mTag = tag;
            mHttpMethod = httpMethod;
            mCallback = callback;
        }

        protected HttpTask (Constants.HttpTaskTag tag, HttpMethod httpMethod,
                            HttpTaskDoneCallback callback, Map<String, String> nameValuePairs) {
            mTag = tag;
            mHttpMethod = httpMethod;
            mCallback = callback;
            mParamMap = nameValuePairs;
        }


        protected HttpTask (Constants.HttpTaskTag tag, HttpMethod httpMethod,
                            HttpTaskDoneCallback callback, Map<String, String> nameValuePairs,
                            File imgFile) {
            mTag = tag;
            mHttpMethod = httpMethod;
            mCallback = callback;
            mParamMap = nameValuePairs;
            mImgFile = imgFile;
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
                HttpEntity httpEntity = getHttpEntity();
                String httpEntityType = "";

                if (httpEntity != null) {
                    try {
                        HttpEntityEnclosingRequestBase entityEnclosingRequest = (HttpEntityEnclosingRequestBase) httpRequest;
                        entityEnclosingRequest.setEntity(httpEntity);

                        httpEntityType = httpEntity.toString();
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                }


                Log.d(LOG_TAG, "Executing http " + mHttpMethod.toString() + " to " + uri +
                        (httpEntity != null ? ". Entity:\n" + httpEntityType : ""));

                // Executing the request
                try {

                    HttpResponse httpResponse = httpClient.execute(httpRequest);

                    Log.d(LOG_TAG, "Got a response. Status: " + httpResponse.getStatusLine().toString());

                    if (httpResponse.getStatusLine().getStatusCode() == 200 ) {

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
                Log.d(LOG_TAG, "sending data to callback:\n" + responseData);
                mCallback.httpTaskDone(mTag, responseData);
            }
        }

        protected HttpEntity getHttpEntity() {

            HttpEntity httpEntity = null;

            // Whether multipart message is required
            boolean isMultipart = (mImgFile != null);

            if (isMultipart ) {
                MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();

                if (mParamMap != null) {
                    for (String name : mParamMap.keySet()) {
                        multipartEntity.addPart(name, new StringBody(mParamMap.get(name), ContentType.TEXT_PLAIN));
                    }
                }

                multipartEntity.addBinaryBody("image", mImgFile, ContentType.create("image/jpeg"), mImgFile.getName());

                httpEntity = multipartEntity.build();

            } else if (mParamMap != null) {
                try {
                    ArrayList<NameValuePair> nameValuePairs= new ArrayList<NameValuePair>();

                    for (String name : mParamMap.keySet()) {
                        nameValuePairs.add(new BasicNameValuePair(name, mParamMap.get(name)));
                    }

                    httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

            return httpEntity;
        }

    }
}
