package il.co.idocare.controllers.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import il.co.idocare.R;
import il.co.idocare.utils.UtilMethods;

/**
 * This is the splash fragment that will be shown when the app starts
 */
public class SplashFragment extends Fragment {

    private final static String LOG_TAG = "SplashFragment";
    private static final int MIN_SPLASH_DURATION_SEC = 3;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash_screen, container, false);

        // This is a full screen fragment - remove any padding
        UtilMethods.setPaddingPx(getActivity().findViewById(R.id.frame_contents), 0);

        return view;
    }

}
