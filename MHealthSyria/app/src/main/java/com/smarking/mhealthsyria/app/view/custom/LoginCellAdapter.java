package com.smarking.mhealthsyria.app.view.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.Physician;

import java.util.List;

/**
 * Created by Richard on 11/5/14.
 */
public class LoginCellAdapter extends ArrayAdapter<Physician> {
    private final LayoutInflater mInflater;
    public List<Physician> mData;

    public LoginCellAdapter(Context context) {
        super(context, R.layout.login_grid_cell, R.id.tvName);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<Physician> physicians){
        clear();
        if (physicians != null) {
            for(Physician physician: physicians){
                add(physician);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.login_grid_cell, parent, false);
        }
        else{
            view = convertView;
        }

        Physician physician = getItem(position);



        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        TextView tvEmail = (TextView) view.findViewById(R.id.tvInfo1);
        TextView tvPhone = (TextView) view.findViewById(R.id.tvInfo2);

        tvName.setText(physician.getFullName());
        tvEmail.setText(physician.getEmail());
        tvPhone.setText(physician.getPhone());


        return view;
    }
}
