package com.smarking.mhealthsyria.app.view.patient.order;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abe707 on 12/20/15.
 */
public class RecListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = RecListAdapter.class.getSimpleName();
    private Activity context;
    private List<RecordCategory> recordCategories;
    private List<String> values;
    private List<String> comments;
    private ArrayList<Medication> medications;

    public RecListAdapter(Activity context, List<RecordCategory> records, List<String> vals, List<String> comms, ArrayList<Medication> meds) {
        this.context = context;
        this.values = vals;
        this.comments = comms;
        this.recordCategories = records;
        this.medications = meds;

    }

    public Object getChild(int groupPosition, int childPosition) {
        if(groupPosition < recordCategories.size()) {
            if (childPosition == 0)
                return context.getString(R.string.comment) + comments.get(groupPosition);
        } else {
            Medication med = (Medication) getGroup(groupPosition);
            switch (childPosition){
                case 0:
                    return med.toString(context);
            }
        }
        return null;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        String child = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_patient_work_child, null);
        }

        TextView item = (TextView) convertView.findViewById(R.id.childTextView);
        if (child == null || child.isEmpty())
            convertView.setVisibility(View.GONE);
        item.setText(child);
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        if(groupPosition < recordCategories.size())
            return 1;
        return 1;
    }

    public Object getGroup(int groupPosition) {
        if(groupPosition < recordCategories.size())
            return recordCategories.get(groupPosition);
        return medications.get(groupPosition - recordCategories.size());
    }

    public int getGroupCount() {
        return recordCategories.size() + medications.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.fragment_patient_work_group, null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.groupTextView);
        if(groupPosition < recordCategories.size())
            try {
                item.setText(((RecordCategory) getGroup(groupPosition)).getDisplayName() + ": " + values.get(groupPosition));
            } catch (NullPointerException e) {
                item.setText(((RecordCategory) getGroup(groupPosition)).getDisplayName());
            }
        else
            item.setText(((Medication) getGroup(groupPosition)).getName(context));
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}