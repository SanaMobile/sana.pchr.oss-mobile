package com.smarking.mhealthsyria.app.view.patient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;
import com.smarking.mhealthsyria.app.view.patient.history.PatientHistoryActivity;
import com.smarking.mhealthsyria.app.view.patient.search.PatientListFragment;

import org.apache.http.auth.AUTH;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by tamhok on 1/23/16.
 */
public class PatientForm {
    public EditText etLastName;
    public EditText etFirstName;
    public EditText etCityOfBirth;
    public EditText etUNCHR;
    public EditText etYearOfBirth;
    public Spinner spGender;
    public EditText etPhone;
    public EditText etProviderId;

    public PatientForm(View rootView, Context c) {
        etLastName = (EditText) rootView.findViewById(R.id.etLastName);
        etFirstName = (EditText) rootView.findViewById(R.id.etFirstName);
        etCityOfBirth = (EditText) rootView.findViewById(R.id.etCityOfBirth);
        etUNCHR = (EditText) rootView.findViewById(R.id.etUNCHR);
        etYearOfBirth = (EditText) rootView.findViewById(R.id.etYearOfBirth);
        etPhone = (EditText) rootView.findViewById(R.id.etPhoneNumber);
        etProviderId = (EditText) rootView.findViewById(R.id.etProviderID);

        spGender = (Spinner) rootView.findViewById(R.id.spGender);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(c, R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adapter);
    }

    public void loadInfo(Patient patient) {
        etLastName.setText(patient.getLastName());
        etFirstName.setText(patient.getFirstName());
        etPhone.setText(patient.getPhone());
        etProviderId.setText(patient.getProviderID());
        etYearOfBirth.setText(patient.getBirthYear());
        etCityOfBirth.setText(patient.getBirthCity());
        etUNCHR.setText(patient.getUNHCR());

        spGender.setSelection(patient.getGender().equals("M") ? 0 : 1);
    }

    public String getGender() {
        int gender = spGender.getSelectedItemPosition();

        String genderChar = "";
        if (gender == 0) {
            genderChar = "M";
        } else if (gender == 1) {
            genderChar = "F";
        }

        return genderChar;
    }

    public Bundle toBundle() {
        StringBuilder sb = new StringBuilder();

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String cityOfBirth = etCityOfBirth.getText().toString().trim();
        String unchr = etUNCHR.getText().toString().trim();
        String dateOfBirth = etYearOfBirth.getText().toString().trim();
        String phone = etUNCHR.getText().toString().trim();
        String providerID = etProviderId.getText().toString().trim();

        String str = " AND %s LIKE \"%%%s%%\" ";
        sb.append(" 1 ");
        if (!firstName.isEmpty()) {
            sb.append(String.format(Locale.US, str, Patient.FIRSTNAME, firstName));
        }

        if (!lastName.isEmpty()) {
            sb.append(String.format(Locale.US, str, Patient.LASTNAME, lastName));
        }

        if (!cityOfBirth.isEmpty()) {
            sb.append(String.format(Locale.US, str, Patient.BIRTHCITY, cityOfBirth));
        }

        if (!unchr.isEmpty()) {
            sb.append(String.format(Locale.US, str, Patient.UNHCR, unchr));
        }

        if (!dateOfBirth.isEmpty()) {
            sb.append(String.format(Locale.US, str, Patient.BIRTHYEAR, dateOfBirth));
        }

        if (!phone.isEmpty()) {
            sb.append(String.format(Locale.US, str, Patient.PHONE, phone));
        }

        if (!providerID.isEmpty()) {
            sb.append(String.format(Locale.US, str, Patient.PROVIDERID, providerID));
        }

        String gender = spGender.getSelectedItemPosition() == 0 ? "M" : "F";
        sb.append(String.format(Locale.US, str, Patient.GENDER, gender));

        Bundle bundle = new Bundle();
        bundle.putString(PatientListFragment.SELECTION, sb.toString());

        return bundle;
    }

    public Patient toPatient() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String cityOfBirth = etCityOfBirth.getText().toString().trim();
        String unhcr = etUNCHR.getText().toString().trim();
        String sYearofBirth = etYearOfBirth.getText().toString().trim();
        String phonenum = etPhone.getText().toString().trim();
        String providerID = etProviderId.getText().toString().trim();

        String genderChar = getGender();

        return new Patient(firstName, lastName, unhcr, sYearofBirth,
                cityOfBirth, genderChar, phonenum, providerID);
    }

    public boolean validatePatient(final AuthenticatedActivity a) {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String cityOfBirth = etCityOfBirth.getText().toString().trim();
        String unhcr = etUNCHR.getText().toString().trim();
        String sYearofBirth = etYearOfBirth.getText().toString().trim();
        String phonenum = etPhone.getText().toString().trim();
        String providerID = etProviderId.getText().toString().trim();

        String message = null;
        if (firstName.isEmpty() || firstName.length() > Constants.VARCHAR_LIMIT) {
            message = a.getString(R.string.first_name);
        } else if (lastName.isEmpty() || lastName.length() > Constants.VARCHAR_LIMIT) {
            message = a.getString(R.string.last_name);
        } else if (cityOfBirth.length() > Constants.VARCHAR_LIMIT) {
            message = a.getString(R.string.city_of_birth);
        } else if (unhcr.length() > Constants.VARCHAR_LIMIT) {
            message = a.getString(R.string.UNCHR);
        } else if (phonenum.length() > Constants.VARCHAR_LIMIT) {
            message = a.getString(R.string.phone_colon);
        } else if (providerID.length() > Constants.VARCHAR_LIMIT) {
            message = a.getString(R.string.provider_id);
        }

        int yearOfBirth = 0;
        if (sYearofBirth.length() <= 0 || sYearofBirth.length() > 5) {
            message = a.getString(R.string.year_of_birth);
        } else {
            yearOfBirth = Integer.parseInt(sYearofBirth);

            if (yearOfBirth < 1899 || yearOfBirth > Calendar.getInstance().get(Calendar.YEAR))
                message = a.getString(R.string.year_of_birth);
        }

        if (message != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(a);
            alertDialogBuilder.setTitle(a.getString(R.string.error_adding_new_patient));
            alertDialogBuilder.setMessage(a.getString(R.string.invalid) + " " + message);
            alertDialogBuilder.show();
            return false;
        }
        return true;
    }

    public void savePatient(final AuthenticatedActivity a, Patient original) {
        if (validatePatient(a)) {
            String where = "UUID = ?";
            Patient patient = toPatient();
            patient.uuid = original.uuid;
            patient.setCreated(original.getCreated());
            patient.markForUpdate();
            a.getContentResolver().update(Patient.URI, patient.putContentValues(), where, new String[]{patient.uuid});
        }
    }

    public int addPatient(final AuthenticatedActivity a) {
        if (validatePatient(a)) {
            final Patient patient = toPatient();
            final ProgressDialog progressDialog = new ProgressDialog(a);

            progressDialog.setCancelable(false);
            progressDialog.show();
            progressDialog.setTitle(a.getString(R.string.adding_patient));
            progressDialog.setMessage(a.getString(R.string.please_wait));

            Task.callInBackground(new Callable<Patient>() {
                @Override
                public Patient call() throws Exception {
                    Uri uri = a.getContentResolver().insert(Patient.URI, patient.putContentValues());
                    Cursor cursor = a.getContentResolver().query(uri, null, null, null, null);
                    cursor.moveToFirst();
                    Patient patient = new Patient(cursor);
                    cursor.close();
                    a.runSync();
                    return patient;
                }
            }).continueWith(new Continuation<Patient, Void>() {
                @Override
                public Void then(Task<Patient> task) throws Exception {
                    progressDialog.dismiss();
                    Intent intent = new Intent(a, PatientHistoryActivity.class);
                    intent.putExtra(Patient.TABLE, task.getResult());
                    a.startActivity(intent);
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
            return 1;
        }
        return -1;
    }
}
