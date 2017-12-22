package com.smarking.mhealthsyria.app.view.patient.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;

import java.util.List;

public class SearchPatientActivity extends AuthenticatedActivity {
    private static final String TAG = SearchPatientActivity.class.getSimpleName();
    private PatientListFragment patientListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient);

        patientListFragment= new PatientListFragment();
        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
        transaction2.replace(R.id.frame_list, patientListFragment);
        transaction2.addToBackStack(null);
        transaction2.commit();


        Fragment fragment = new SearchPatientFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_search, fragment);
        transaction.commit();
    }

    public void addParamsToFragment(Bundle bundle){
        patientListFragment= new PatientListFragment();
        patientListFragment.setFromServer(false);
        patientListFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_list, patientListFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setFragmentPatients(List<Patient> patients){
        Log.e(TAG, "SETTING FRAGMENT PATIENT LIST: " + patients.size());
//        patientListFragment= new PatientListFragment();
//        FragmentTransaction transactionList = getSupportFragmentManager().beginTransaction();
//        transactionList.replace(R.id.frame_list, patientListFragment);
//        transactionList.addToBackStack(null);
//        transactionList.commit();
        patientListFragment.setFromServer(true);
        patientListFragment.setPatients(getApplicationContext(), patients);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }





}
