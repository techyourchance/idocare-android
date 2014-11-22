package il.co.idocare.www.idocare;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Main extends Activity implements HomeFragment.HomeFragmentCallback{

    private static final String LOG_TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.contents, new HomeFragment())
                    .commit();
        }
    }


    @Override
    public void getMessageFromServer() {
        new GetMessageFromServer().execute("http://idocare.co.il/api/");
    }

    private void receivedMessage(String msg) {
        HomeFragment f = (HomeFragment) getFragmentManager().findFragmentById(R.id.contents);
        TextView txt = null;
        if (f != null) {
            txt = (TextView) f.getView().findViewById(R.id.incoming_message_txt);
        } else {
            Log.e(LOG_TAG, "contents of 'contents' is null!");
            return;
        }

        txt.setText(msg);
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
                receivedMessage(result);
            } else {
                receivedMessage("Null");
            }
        }

    }



}
