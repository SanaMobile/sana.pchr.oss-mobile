package com.smarking.mhealthsyria.app.view.login;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.UpdateManager;


public class HomeFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = HomeFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        Button bScan= (Button) view.findViewById(R.id.bLoginScan);
        Button bPass  = (Button) view.findViewById(R.id.bLoginPassword);
        bScan.setOnClickListener(this);
        bPass.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bLoginScan:
                if(!isUpdating()) {
                    ((LoginActivity) getActivity()).scanQRCode();
                } else {
                    showIsUpdatingDialog();
                }
                break;
            case R.id.bLoginPassword:
                if(!isUpdating()) {
                    ((LoginActivity) getActivity()).loginWithPassword();
                } else {
                    showIsUpdatingDialog();
                }
                break;
        }
    }

    protected boolean isUpdating(){
        Log.i(TAG, "isUpdating()");
        return UpdateManager.isUpdating(getActivity().getApplicationContext());
    }

    protected void showIsUpdatingDialog(){
        Log.i(TAG, "showIsUpdatingDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(android.R.drawable.ic_menu_upload)
                .setCancelable(true)
                .setTitle(R.string.updating)
                .setMessage(R.string.update_message);
        builder.show();
    }
}
