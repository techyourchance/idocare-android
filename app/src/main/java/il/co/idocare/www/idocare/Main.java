package il.co.idocare.www.idocare;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Main extends Activity {

    private static final String LOG_TAG = "Main";

    private static final int TAKE_PHOTO_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.frame_contents, new FragmentHome())
                    .commit();
        }
    }


    private class GetMessageFromServer extends AsyncTask<String, String, String> {

        protected String doInBackground(String... urls) {
            URL url = null;
            try {
                url = new URL(urls[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            int numOfBytesRead = 0;
            byte input[] = new byte[100];

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                numOfBytesRead = in.read(input, 0, 100);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return numOfBytesRead > 0 ? new String(input) : null;
        }

        protected void onPostExecute(String result) {
            if (result != null) {
            } else {
            }
        }

    }



}
