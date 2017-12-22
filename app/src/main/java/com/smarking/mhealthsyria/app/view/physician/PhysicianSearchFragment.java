package com.smarking.mhealthsyria.app.view.physician;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.smarking.mhealthsyria.app.R;

/**
 */
public class PhysicianSearchFragment extends Fragment implements View.OnClickListener {

    private String mLastName;
    private String mFirstName;
    private String mPhoneNumber;
    private String mEmailAddress;

    public PhysicianSearchFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment PhysicianSearchFragment.
     */
    public static PhysicianSearchFragment newInstance() {
        PhysicianSearchFragment fragment = new PhysicianSearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_physician_search, container, false);

        Button bCancel = (Button) view.findViewById(R.id.bCancel);
        Button bSearch = (Button) view.findViewById(R.id.bSearch);

        bCancel.setOnClickListener(this);
        bSearch.setOnClickListener(this);


        return view;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bCancel:

                break;
        }
    }
}
