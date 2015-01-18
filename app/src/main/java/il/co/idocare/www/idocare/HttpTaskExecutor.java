package il.co.idocare.www.idocare;


import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

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
                            List<NameValuePair> nameValuePairs) {
        HttpTask getTask = new HttpTask(tag, HttpMethod.POST, callback, nameValuePairs);
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
        private List<NameValuePair> mNameValuePairs;
        private HttpTaskDoneCallback mCallback;
        private Constants.HttpTaskTag mTag;

        protected HttpTask (Constants.HttpTaskTag tag, HttpMethod httpMethod, HttpTaskDoneCallback callback) {
            mTag = tag;
            mHttpMethod = httpMethod;
            mCallback = callback;
        }

        protected HttpTask (Constants.HttpTaskTag tag, HttpMethod httpMethod, HttpTaskDoneCallback callback, List<NameValuePair> nameValuePairs) {
            mTag = tag;
            mHttpMethod = httpMethod;
            mCallback = callback;
            mNameValuePairs = nameValuePairs;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... uris) {


            DefaultHttpClient httpClient = new DefaultHttpClient();
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

                // Setting the parameters (if applicable to the method)
                if (mNameValuePairs != null) {
                    try {
                        HttpEntityEnclosingRequestBase entityEnclosingRequest = (HttpEntityEnclosingRequestBase) httpRequest;
                        entityEnclosingRequest.setEntity(new UrlEncodedFormEntity(mNameValuePairs));
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(LOG_TAG, "Executing http " + mHttpMethod.toString() + " to " + uri);

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
                    httpClient.getConnectionManager().shutdown();
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

    }
}
