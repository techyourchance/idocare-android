package il.co.idocare.www.idocare;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;



public class HomeFragment extends Fragment {

    private HomeFragmentCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button getMessageBtn = (Button) view.findViewById(R.id.get_message_btn);

        getMessageBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mCallback.getMessageFromServer();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (HomeFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement HomeFragmentCallback");
        }
    }


    public interface HomeFragmentCallback {
        public void getMessageFromServer();
    }


}
