package com.smarking.mhealthsyria.app.view.patient.search;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.server.ServerSearch;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;
import com.smarking.mhealthsyria.app.view.patient.PatientForm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-18.
 */
public class SearchPatientFragment extends Fragment
        implements View.OnClickListener {

    private PatientForm patientForm;

    private Button bSearch;
    private Button bCancel;
    private Button bSearchServer;
    private Button bAdd;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bSearch:
                ((SearchPatientActivity) getActivity()).addParamsToFragment(patientForm.toBundle());
                hideKeyboard();
                break;
            case R.id.bClear:
                hideKeyboard();
                break;
            case R.id.bSearchServer:
                searchServer();
                hideKeyboard();
                break;
            case R.id.bAdd:
                if (patientForm.addPatient((AuthenticatedActivity) getActivity()) > -1) ;
                break;
            default:
                break;
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void searchServer() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String unchr = patientForm.etUNCHR.getText().toString();
                if (unchr.isEmpty()) {
                    String year = patientForm.etYearOfBirth.getText().toString();
                    if (year.length() == 4) {
                        try {
                            String gender = patientForm.getGender();

                            JSONArray matches = ServerSearch.findPatient(getActivity(), year, patientForm.etCityOfBirth.getText().toString(),
                                    patientForm.etFirstName.getText().toString(), patientForm.etLastName.getText().toString(), gender);
                            final List<Patient> patients = new ArrayList<>();
                            for (int i = 0; i < matches.length(); i++) {
                                JSONObject patientDoc = matches.getJSONObject(i);
                                Patient patient = new Patient(patientDoc);
                                patients.add(patient);
                                Log.e(this.getClass().getSimpleName(), patient.toString());
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((SearchPatientActivity) getActivity()).setFragmentPatients(patients);
                                }
                            });
                        } catch (Exception e) {
                            Log.e(this.getClass().getSimpleName(), e.toString());
                        }
                    } else {
                        final String message = getString(R.string.toast_input_unchr);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }else{
                    try{
                        JSONArray matches = ServerSearch.findPatient(getActivity(),unchr);
                        final List<Patient> patients = new ArrayList<>();
                        for(int i =0; i < matches.length(); i++){
                            JSONObject patientDoc = matches.getJSONObject(i);
                            Patient patient = new Patient(patientDoc);
                            patients.add(patient);
                            Log.e(this.getClass().getSimpleName(), patient.toString());
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((SearchPatientActivity) getActivity()).setFragmentPatients(patients);
                            }
                        });

                    }catch (Exception e){
                        Log.e(this.getClass().getSimpleName(),e.toString());
                    }
                }
            }


        });
        thread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_patient, container, false);

        patientForm = new PatientForm(rootView, getActivity());

        bSearch = (Button) rootView.findViewById(R.id.bSearch);
        bCancel = (Button) rootView.findViewById(R.id.bClear);
        bSearchServer = (Button) rootView.findViewById(R.id.bSearchServer);
        bAdd = (Button) rootView.findViewById(R.id.bAdd);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bSearch.setOnClickListener(this);
        bCancel.setOnClickListener(this);
        bSearchServer.setOnClickListener(this);
        bAdd.setOnClickListener(this);

    }
}
