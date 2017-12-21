package com.smarking.mhealthsyria.app.view.custom;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-18.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public interface DateFragmentListener {
        void dateChanged(int year, int month, int day);

        void showDatePicker();
    }

    private DateFragmentListener listener;

    @SuppressLint("ValidFragment")
    public DatePickerFragment(DateFragmentListener listener){
        this.listener = listener;
    }

    public DatePickerFragment(){

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        listener.dateChanged(year, month, day);

    }
}