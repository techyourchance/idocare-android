package il.co.idocare.www.idocare;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;



public class FragmentHome extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnNewRequest = (Button) view.findViewById(R.id.btn_new_request);
        btnNewRequest.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               FragmentTransaction ft = getFragmentManager().beginTransaction();
               ft.addToBackStack(null);
               ft.replace(R.id.frame_contents, new FragmentNewRequest());
               ft.commit();
           }
       });

        Button btnRequestDetails = (Button) view.findViewById(R.id.btn_request_details);
        btnRequestDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack(null);
                ft.replace(R.id.frame_contents, new FragmentRequestDetails());
                ft.commit();
            }
        });

        return view;
    }



}
