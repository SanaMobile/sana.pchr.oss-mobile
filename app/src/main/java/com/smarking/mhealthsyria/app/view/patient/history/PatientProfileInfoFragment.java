package com.smarking.mhealthsyria.app.view.patient.history;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.view.custom.ActiveMedListAdapter;
import com.smarking.mhealthsyria.app.view.custom.MedicationEdit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-25.
 */

public class PatientProfileInfoFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = PatientProfileInfoFragment.class.getSimpleName();
    private static final int QUERY_UUID = 1;
    private Patient mPatient;
    private TextView tvPatientName;
    private TextView tvPatientAge;
    private ActiveMedListAdapter activeMedListAdapter;
    private ArrayList<Medication> activeMeds = new ArrayList<>();
    private ListView lvActiveMeds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPatient = getArguments().getParcelable(Patient.UUID);

        AsyncTask loadTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                refresh();
                return  null;
            }
        };
        loadTask.execute();


    }

    public void refresh() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Patient.UUID, mPatient);
        getLoaderManager().restartLoader(PatientProfileInfoFragment.QUERY_UUID, bundle, this);
    }

    public ArrayList<Medication> getActiveMeds(){
        return this.activeMeds;
    }

    public ArrayList<String> getActiveMedUuids(){
        ArrayList<String> meds = new ArrayList<>();
        for(Medication med: activeMeds)
            meds.add(med.uuid);
        return meds;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.patient_profile_info_fragment, container, false);
        tvPatientName = (TextView) rootView.findViewById(R.id.tvPatientName);
        tvPatientAge = (TextView) rootView.findViewById(R.id.tvPatientAge);
        lvActiveMeds = (ListView) rootView.findViewById(R.id.lvActiveMeds);
        activeMedListAdapter = new ActiveMedListAdapter(getActivity(), activeMeds, new HashMap<Medication, Recommender.MedMod>());
        lvActiveMeds.setAdapter(activeMedListAdapter);
        lvActiveMeds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationEdit.openActiveEditDialog( getActivity(), position, activeMeds, activeMedListAdapter);
            }
        });
        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.e(TAG, "on create loader");
        switch(id){
            case PatientProfileInfoFragment.QUERY_UUID:
                Patient patient = args.getParcelable(Patient.UUID);
                try{
                    Looper.prepare();
                } catch (Exception e) {
                    Log.e(TAG, "Looper caught: " + e.getMessage());
                }

                try {
                    return new CursorLoader(getActivity(), Patient.URI, null, Patient.UUID + " = ? ", new String[]{patient.uuid}, null);
                } catch (NullPointerException e) {
                    Log.e(TAG, "Exception caught: " + e.getMessage());
                }
        }
        return null;
    }

    public void loadActiveMedications(List<Encounter> encounters){
        Recommender.getInstance().reset_cur_med_list();
        String whereClause = " %s = '%s' ";
        activeMeds.clear();
        for(Encounter encounter: encounters){
            Cursor medCursor = getActivity().getContentResolver().query(Medication.URI, null, String.format(whereClause, Medication.ENCOUNTER_UUID, encounter.uuid), null, null);
            while(medCursor.moveToNext()){
                Medication medication = new Medication(medCursor);
                String sEndDate = medication.getEndDate();
                if (sEndDate == null || sEndDate.isEmpty() || sEndDate.equals("null")) {
                    activeMeds.add(medication);
                    Recommender.getInstance().addCurMed(medication);
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date endDate = formatter.parse(sEndDate);
                        Date now = new Date();
                        int compare = now.compareTo(endDate);
                        if(compare <= 0) {
                            Recommender.getInstance().addCurMed(medication);
                            activeMeds.add(medication);
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, e.toString() + " " + sEndDate);
                    }
                }
            }
            medCursor.close();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activeMedListAdapter.notifyDataSetChanged();
            }
        });
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            Patient patient = new Patient(data);
            tvPatientName.setText(patient.getFullName());
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            String sBirthYear = patient.getBirthYear();
            int birthYear = Integer.parseInt(sBirthYear);
            tvPatientAge.setText(Integer.toString(year - birthYear));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
