package com.smarking.mhealthsyria.app.view.physician;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.auth.Security;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.server.ServerSearch;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;
import com.smarking.mhealthsyria.app.view.custom.QRScanner;
import com.smarking.mhealthsyria.app.view.patient.add.AddNewPatientActivity;
import com.smarking.mhealthsyria.app.view.patient.history.PatientHistoryActivity;
import com.smarking.mhealthsyria.app.view.patient.search.SearchPatientActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


/**
 * Created by mallika on 2015-04-08.
 *
 * Activity that allows a physician to select how to pick patients
 * <p>
 *     This activity presents three choices to physicians. Scan a patient's QR code from a
 *     previous visit's documentation, manually input in data to search for a patient, or
 *     add a new patient.
 * </p>
 */
public class PhysicianMenuActivity extends AuthenticatedActivity implements View.OnClickListener {
    //For debuggin purposes
    private static final String TAG = PhysicianMenuActivity.class.getSimpleName();

    private Button bScan;
    private Button bAddNew;
    private Button bSearch;
    private TextView tvDoctorName;
    private String mPhysicianUuid;

    public static ProgressDialog mDialog;
    public static int hasLoaded = 0;

    //Defines the activity intent outcomes
    public static final int RQ_SCAN_PATIENT_QR = 0;
    public static final int RQ_SCAN_NEW_PATIENT_CARD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Wait for categories to be loaded.


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physician_homepage);

        bScan = (Button) findViewById(R.id.bScan);
        bAddNew = (Button) findViewById(R.id.btnAddNew);
        bSearch = (Button) findViewById(R.id.btnEnterPatient);
        tvDoctorName = (TextView) findViewById(R.id.tvDoctorName);

        bAddNew.setOnClickListener(this);
        bScan.setOnClickListener(this);
        bSearch.setOnClickListener(this);


        Map<String, String> userData = getCurrentUserData();

        mPhysicianUuid = userData.get(SessionManager.KEY_UUID);

        tvDoctorName.setText(getString(R.string.homepage_welcom_tv) + " " + userData.get(SessionManager.KEY_FULLNAME));

        Cursor c = null;
        try {
            c = getContentResolver().query(VisitCategory.URI, null, null, null, null);
            if (c.getCount() == 0) {
                hasLoaded = 0;
                waitForSync(this);
            } else {
                hasLoaded = 1;
            }
            c.close();
        } catch (NullPointerException e) {
            if (c != null) {
                c.close();
            }
            waitForSync(this);
            hasLoaded = 0;
        }
    }

    public static void waitForSync(AuthenticatedActivity a) {
        mDialog = new ProgressDialog(a);
        mDialog.setCancelable(false);

        mDialog.setTitle(a.getString(R.string.downloading_data_wait));
        mDialog.setMessage(a.getString(R.string.please_wait));
        mDialog.show();
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){
            case R.id.bScan:
                intent = new Intent(PhysicianMenuActivity.this, QRScanner.class);
                startActivityForResult(intent, RQ_SCAN_PATIENT_QR);
                break;
            case R.id.btnEnterPatient:
                intent = new Intent(PhysicianMenuActivity.this, SearchPatientActivity.class);
                startActivity(intent);
                break;
            case R.id.btnAddNew:
                intent = new Intent(PhysicianMenuActivity.this, AddNewPatientActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RQ_SCAN_PATIENT_QR: {
                if (resultCode == Activity.RESULT_OK) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle(getString(R.string.loading));
                    progressDialog.setMessage(getString(R.string.looking_up_patient));
                    progressDialog.show();
                    Task.callInBackground(new Callable<Patient>() {
                        @Override
                        public Patient call() throws Exception {
                            String qrString = data.getStringExtra(QRScanner.QR_DATA);
                            byte[] qrBytes = Base64.decode(qrString.getBytes(), Base64.DEFAULT);

                            if (qrBytes.length == 0 || qrBytes.length > 49 || qrBytes[0] != 0) {
                                Log.e("PhysicianMenuActivity", "QR Scanning problem");
                                //TODO Implement something to do when this happens
                                return null;
                            }
                            final String uuid = Security.printHex(qrBytes, 1, 17);
                            Patient patient =  Patient.getPatient(PhysicianMenuActivity.this, uuid);
                            if(patient == null){
                                JSONArray result = ServerSearch.addPatient(getApplicationContext(), uuid, mPhysicianUuid);
                                JSONObject patientDoc = result.getJSONObject(0);
                                patient = new Patient(patientDoc);
                            }
                            return patient;
                        }
                    }).continueWith(new Continuation<Patient, Void>() {
                        @Override
                        public Void then(Task<Patient> task) throws Exception {
                            if (task.isFaulted() || task.getResult() == null) {
                                progressDialog.setMessage(getString(R.string.patient_not_found));
                                progressDialog.setCancelable(true);
                            } else {
                                progressDialog.dismiss();
                                Intent intent = new Intent(PhysicianMenuActivity.this, PatientHistoryActivity.class);
                                intent.putExtra(Patient.TABLE, task.getResult());
                                startActivity(intent);
                            }
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);

                }
                break;
            }
        }
    }
}
