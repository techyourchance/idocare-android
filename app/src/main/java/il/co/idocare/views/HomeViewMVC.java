package il.co.idocare.views;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import il.co.idocare.Constants;
import il.co.idocare.R;

/**
 * MVC View of the Home screen.
 */
public class HomeViewMVC extends AbstractViewMVC {

    View mRootView;

    public HomeViewMVC(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.fragment_home, container, false);

    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    @Override
    protected void handleMessage(Message msg) {
        // TODO: implement this method
    }
}
