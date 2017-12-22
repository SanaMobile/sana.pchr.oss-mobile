package com.smarking.mhealthsyria.app.view.patient.order;

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
import com.smarking.mhealthsyria.app.model.category.MedicationGroupCategory;
import com.smarking.mhealthsyria.app.view.custom.CategoryAdapter;
import com.smarking.mhealthsyria.app.view.custom.MedicationEdit;

import java.util.List;


/**
 * Created by abe707 on 1/11/16.
 */
public class PatientMedFragment extends Fragment implements View.OnClickListener {
    private List<MedicationGroupCategory> mMedGroups;
    private ListView lvMedicationGroups;
    private CategoryAdapter<MedicationGroupCategory> mMedAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_suggested_med, container, false);
        lvMedicationGroups = (ListView) rootView.findViewById(R.id.lvSuggestedMed);
        mMedGroups = getSuggestedGroups();
        Log.e("MED GROUPS","size: " + mMedGroups.size());
        mMedAdapter = new CategoryAdapter(getActivity(), R.layout.list_category_item_white, mMedGroups);
        lvMedicationGroups.setAdapter(mMedAdapter);
        lvMedicationGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationGroupCategory medicationGroupCategory = mMedGroups.get(position);
                MedicationEdit.openSuggestedEditDialog((PatientMakeOrderActivity) getActivity(), medicationGroupCategory);
            }
        });

        if (mMedGroups.isEmpty())
            ((PatientMakeOrderActivity)getActivity()).setMedView(R.id.patient_med_frame,View.GONE);
        else
            ((PatientMakeOrderActivity)getActivity()).setMedView(R.id.patient_med_frame,View.VISIBLE);
//        tvMed  = (TextView) rootView.findViewById(R.id.tvSuggestedMedication);
//        tvMed.setText(mMedicationGroup.getDisplayName());
//        tvMed.setOnClickListener(this);
        return rootView;
    }

    private List<MedicationGroupCategory> getSuggestedGroups(){
        return Recommender.getInstance().getRecommendedMedications();
//        Cursor mgCursor = getActivity().getContentResolver().query(MedicationGroupCategory.URI, null,null,null,null);
//        List<MedicationGroupCategory> mgCategories = new ArrayList<>();
//        while(mgCursor.moveToNext())
//            mgCategories.add(new MedicationGroupCategory(mgCursor));
//        mgCursor.close();
//        return mgCategories;
    }

    @Override
    public void onClick(View v) {
//        MedicationEdit.openEditDialog(((PatientMakeOrderActivity)getActivity()), mMedicationGroup);
    }
}
