package com.smarking.mhealthsyria.app.view.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.category.Category;

import java.util.List;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-24.
 */


public class CategoryAdapter<T extends Category> extends ArrayAdapter<T> {
    private final int layoutResourceId;

    public CategoryAdapter(Context context, int resource, List<T> items) {
        super(context, resource, items);
        this.layoutResourceId = resource;
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView,ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        T p = getItem(position);
        if (p != null) {
            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            tvName.setText( p.getDisplayName() );
        }
        return convertView;
    }
}
