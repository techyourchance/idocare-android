package il.co.idocare.www.idocare;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FragmentRequestDetails extends Fragment {

    private final static String LOG_TAG = "FragmentRequestDetails";

    private final static String USERNAME = "admin";
    private final static String PASSWORD = "123456";
    private final static String URI = "http://dev-04.idocare.co.il/api-04/imglist";


    private final static String JSON_TAG_URIS = "filelist";

    private RequestPicturesAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_details, container, false);

        mListAdapter = new RequestPicturesAdapter(getActivity(), 0);
        ListView listPictures = (ListView) view.findViewById(R.id.list_request_pictures);
        listPictures.setAdapter(mListAdapter);

        HttpTask httpTask = new HttpTask();
        httpTask.execute();

        return view;
    }

    private static class ViewHolder {
        ImageView imageView;
    }

    private class RequestPicturesAdapter extends ArrayAdapter<String> {

        private LayoutInflater mInflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        public RequestPicturesAdapter(Context context, int resource) {
            super(context, resource);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.request_images_list_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ImageLoader.getInstance().displayImage(getItem(position), holder.imageView, animateFirstListener);

            return view;
        }
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new ArrayList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }


    private class HttpTask extends AsyncTask<String, Void, List<String>> {

        private static final String LOG_TAG = "HttpTask";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(String... strings) {

            Log.d(LOG_TAG, "Executing http POST to URI: " + URI + "\nUsername: " + USERNAME
                    + "\nPassword: " + PASSWORD);

            DefaultHttpClient httpClient = new DefaultHttpClient();

            try
            {

                HttpPost httpPost = new HttpPost(URI);

                // Adding data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("username", "admin"));
                nameValuePairs.add(new BasicNameValuePair("password", "123456"));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                HttpResponse httpResponse = httpClient.execute(httpPost);

                Log.d(LOG_TAG, "Got a response. Status: " + httpResponse.getStatusLine().toString());

                if (httpResponse.getStatusLine().getStatusCode() == 200 ) {

                    String jsonData = EntityUtils.toString(httpResponse.getEntity());

                    Log.d(LOG_TAG, "The content of the response is:\n" + jsonData);

                    return extractUrisFromJSON(jsonData);
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                httpClient.getConnectionManager().shutdown();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<String> urisList) {
            if (urisList != null) {
                mListAdapter.clear();
                mListAdapter.addAll(urisList);
                mListAdapter.notifyDataSetChanged();
            }

        }

        private List<String> extractUrisFromJSON(String jsonData) {

            ArrayList<String> urisList = null;

            if (jsonData != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonData);

                    // Getting JSON Array
                    JSONArray uris = jsonObj.getJSONArray(JSON_TAG_URIS);

                    urisList = new ArrayList<String>(uris.length());

                    // Adding uris to the list
                    for (int i = 0; i < uris.length(); i++) {
                        urisList.add(uris.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(LOG_TAG,  "Couldn't get any data from the url");
            }

            return urisList;
        }
    }
}
