package com.smarking.mhealthsyria.app.view.patient.order;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;

import java.util.List;

/**
 * Created by abe707 on 10/1/15.
 */
public class WorkOrderListAdapter extends BaseAdapter {
    private static final String TAG = WorkOrderListAdapter.class.getSimpleName();
    private Activity context;
    private List<RecordCategory> recordCategories;

    public WorkOrderListAdapter(Activity context, List<RecordCategory> records) {
        this.context = context;
        this.recordCategories = records;

    }

    @Override
    public RecordCategory getItem(int position) {
        return recordCategories.get(position);
    }

    @Override
    public int getCount() {
        return recordCategories.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.fragment_patient_work_group, null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.groupTextView);
        item.setText(getItem(position).getDisplayName());
        return convertView;
    }

}