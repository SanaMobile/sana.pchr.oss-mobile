package com.smarking.mhealthsyria.app.view.patient.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;
import com.smarking.mhealthsyria.app.view.patient.PatientForm;

/**
 * Created by tamhok on 1/23/16.
 */

public class EditPatientActivity extends AuthenticatedActivity
        implements View.OnClickListener {
    private static final String TAG = EditPatientActivity.class.getSimpleName();

    private PatientForm patientForm;

    private Button bCancel;
    private Button bSave;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient);

        patient = getIntent().getParcelableExtra(Patient.TABLE);

        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        patientForm = new PatientForm(viewGroup, this);
        patientForm.loadInfo(patient);

        bCancel = (Button) findViewById(R.id.bClear);
        bSave = (Button) findViewById(R.id.bSave);

        bCancel.setOnClickListener(this);
        bSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bClear:
                finish();
                setResult(RESULT_CANCELED);
                break;
            case R.id.bSave:
                patientForm.savePatient(this, patient);
                Intent intent = new Intent();
                intent.putExtra(Patient.TABLE, patientForm.toPatient());
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }
}
