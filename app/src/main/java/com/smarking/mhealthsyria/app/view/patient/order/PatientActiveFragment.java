package com.smarking.mhealthsyria.app.view.patient.order;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.view.custom.ActiveMedListAdapter;
import com.smarking.mhealthsyria.app.view.custom.MedicationEdit;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by abe707 on 1/20/16.
 */
public class PatientActiveFragment extends Fragment  {
    private ArrayList<Medication> mActiveMeds;
    private HashMap<Medication, Recommender.MedMod> mRecChanges;
    private ListView lvActiveMeds;
    private ActiveMedListAdapter mMedAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActiveMeds = getArguments().getParcelableArrayList(Medication.TABLE);
        mRecChanges = Recommender.getInstance().getChangedMedications();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_active_med, container, false);
        lvActiveMeds = (ListView) rootView.findViewById(R.id.lvActiveMeds);

        Log.e(this.getClass().getSimpleName(), "size: " + mActiveMeds.size());
        mMedAdapter = new ActiveMedListAdapter(getActivity(), mActiveMeds, mRecChanges);
        lvActiveMeds.setAdapter(mMedAdapter);
        lvActiveMeds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationEdit.openActiveEditDialog( getActivity(),position,mActiveMeds,mMedAdapter);
            }
        });


        if (mActiveMeds.isEmpty())
            ((PatientMakeOrderActivity)getActivity()).setMedView(R.id.patient_active_frame, View.GONE);
        else
            ((PatientMakeOrderActivity)getActivity()).setMedView(R.id.patient_active_frame, View.VISIBLE);

        return rootView;
    }

    public ArrayList<Medication> saveActive(Encounter encounter) throws JSONException {
        for (Medication med: mActiveMeds) {
            getActivity().getContentResolver().insert(Medication.URI, med.putContentValues());
        }
        return mActiveMeds;
    }
}
