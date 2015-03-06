package il.co.idocare.controllers.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import il.co.idocare.Constants;
import il.co.idocare.Constants.FieldName;
import il.co.idocare.Constants.MessageType;
import il.co.idocare.R;
import il.co.idocare.ServerRequest;
import il.co.idocare.models.RequestsMVCModel;
import il.co.idocare.utils.IDoCareJSONUtils;
import il.co.idocare.views.LoginViewMVC;

public class SplashFragment extends AbstractFragment {

    private final static String LOG_TAG = "SplashFragment";
    private static final int MIN_SPLASH_DURATION_SEC = 3;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash_screen, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e(LOG_TAG, "onResume called");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                long initTime = System.currentTimeMillis();
                // This call will block until the model has been initialized, at which point
                // we can switch to Home Fragment
                try {
                    getRequestsModel().getAllRequests();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime < initTime + MIN_SPLASH_DURATION_SEC*1000) {
                    try {
                        Thread.sleep(initTime + MIN_SPLASH_DURATION_SEC*1000 - currentTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                switchToHome();
            }

            private void switchToHome() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().getFragmentManager().beginTransaction().remove(SplashFragment.this).commit();
                        replaceFragment(HomeFragment.class, false, null);
                    }
                });
            }
        });

        thread.start();
    }

    @Override
    public boolean isTopLevelFragment() {
        return true;
    }

    @Override
    public Class<? extends AbstractFragment> getNavHierParentFragment() {
        return null;
    }

    @Override
    protected void handleMessage(Message msg) {
    }

}
