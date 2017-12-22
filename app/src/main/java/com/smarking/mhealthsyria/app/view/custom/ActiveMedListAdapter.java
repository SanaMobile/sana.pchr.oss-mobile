package com.smarking.mhealthsyria.app.view.custom;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.category.Category;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by abe707 on 1/20/16.
 */
public class ActiveMedListAdapter extends BaseAdapter {
    private ArrayList<Medication> medications;
    private Activity mActivity;
    private HashMap<Medication, Recommender.MedMod> mods;

    public ActiveMedListAdapter(Activity activity, ArrayList<Medication> meds, HashMap<Medication, Recommender.MedMod> mods) {
        super();
        medications = meds;
        this.mActivity = activity;
        this.mods = mods;
    }


    @Override
    public int getCount()  {
        return medications.size();
    }

    @Override
    public Medication getItem(int position)  {
        return medications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View activeMedChild = View.inflate(mActivity, R.layout.active_meds_child, null);

        Medication medication = getItem(position);
        String prefix = "";
        if (mods.get(medication) != null) {
            if (mods.get(medication) == Recommender.MedMod.STOP) {
                prefix = mActivity.getString(R.string.stop);
            } else {
                prefix = mActivity.getString(R.string.adjust_dose);
            }
        }
        TextView tvMed = (TextView) activeMedChild.findViewById(R.id.tvMedication);
        String display;
        if (Category.language != Category.Language.ARABIC) {
            display = prefix + " " + medication.getName(mActivity) + ": " + medication.toString(mActivity);
        } else {
            display = medication.toString(mActivity) + ": " + medication.getName(mActivity) + " " + prefix;
        }

        tvMed.setText(display);

        return activeMedChild;
    }
}