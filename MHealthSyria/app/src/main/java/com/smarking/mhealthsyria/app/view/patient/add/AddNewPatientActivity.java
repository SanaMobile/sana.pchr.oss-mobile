package com.smarking.mhealthsyria.app.view.patient.add;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;
import com.smarking.mhealthsyria.app.view.patient.PatientForm;
import com.smarking.mhealthsyria.app.view.patient.history.PatientHistoryActivity;

import java.util.Calendar;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


/**
 * Created by mallika on 2015-04-08.
 *
 * Activity for adding new patients. Currently, physician types in all the fields and
 * presses submit button.
 *
 */
public class AddNewPatientActivity extends AuthenticatedActivity
        implements View.OnClickListener {
    private static final String TAG = AddNewPatientActivity.class.getSimpleName();

    private PatientForm patientForm;

    private Button bCancel;
    private Button bEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        patientForm = new PatientForm(viewGroup, this);

        bCancel = (Button) findViewById(R.id.bClear);
        bEnter = (Button) findViewById(R.id.bEnter);

        bCancel.setOnClickListener(this);
        bEnter.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bClear:
                finish();
                break;
            case R.id.bEnter:
                if (patientForm.addPatient(this) > -1)
                    finish();
                break;
            default:
                break;
        }
    }
}
