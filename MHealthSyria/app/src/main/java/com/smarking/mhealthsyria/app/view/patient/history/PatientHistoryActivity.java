package com.smarking.mhealthsyria.app.view.patient.history;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;
import com.smarking.mhealthsyria.app.view.patient.add.AddNewTestActivity;
import com.smarking.mhealthsyria.app.view.patient.edit.EditPatientActivity;
import com.smarking.mhealthsyria.app.view.patient.order.PatientMakeOrderActivity;

import java.util.ArrayList;
import java.util.List;

public class PatientHistoryActivity extends AuthenticatedActivity implements View.OnClickListener{
    private Patient mPatient;
    private Visit mVisit = null;
    private Encounter mEncounter = null;

    private List<Encounter> mEncounters;
    
    private boolean testing = true;
    private static final String TAG  = PatientHistoryActivity.class.getSimpleName();
    public static final int QUERY_UUID = 2;
    public static final int REQUEST_EXIT = 99;
    public static final int REQUEST_STAY = 88;

    public static ProgressDialog mDialog;
    private static PatientProfileInfoFragment patientProfileInfoFragment;
    private static PatientHistoryFragment patientHistoryFragment;
    private Button bMakeOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.patient_history_activity);

        mPatient = getIntent().getParcelableExtra(Patient.TABLE);
        Recommender.start(mPatient);


//        LoadingFragment loadingFragment = getIntent().getParcelableExtra(LoadingFragment.DIALOG);
//
//        loadingFragment.dismiss();



        patientProfileInfoFragment = new PatientProfileInfoFragment();
        patientHistoryFragment = new PatientHistoryFragment();

        bMakeOrder = (Button) findViewById(R.id.bMakeOrder);
        Button bFinish = (Button) findViewById(R.id.bFinish);
        Button bAddLabTest = (Button) findViewById(R.id.bAddTestLab);
        Button bAddMedHist = (Button) findViewById(R.id.bAddTestHist);
        Button bAddPhysExam = (Button) findViewById(R.id.bAddTestPhys);

        bAddLabTest.setOnClickListener(this);
        bAddMedHist.setOnClickListener(this);
        bAddPhysExam.setOnClickListener(this);
        bMakeOrder.setOnClickListener(this);
        bFinish.setOnClickListener(this);

        Fragment[] fragment = new Fragment[]{patientProfileInfoFragment, patientHistoryFragment};
        for (Fragment aFragment : fragment) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Patient.UUID, mPatient);
            bundle.putParcelable(Visit.TABLE, mVisit);
            aFragment.setArguments(bundle);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.patient_info_frame, patientProfileInfoFragment)
                    .add(R.id.patient_history_frame, patientHistoryFragment)
                    .commit();
        }

        openLoadingDialog(this);

        try{
            mEncounter = getIntent().getParcelableExtra(Encounter.TABLE);
        }catch (Exception e){
            Log.e(TAG, "encounter err: " + e.toString());
            createVisitAndEncounter();
        }

        if(mEncounter == null)
            createVisitAndEncounter();
    }

    @Override
    public void onBackPressed() {
        showCancelDialog(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }


    public void showCancelDialog(DialogInterface.OnClickListener listen) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.finish);
        alertDialogBuilder.setMessage(R.string.finishCancel);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.yes, listen)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.create().show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bMakeOrder:
                Recommender.getInstance().compute_lists();
                Recommender.getInstance().recompute();

                ArrayList<Medication> active = patientProfileInfoFragment.getActiveMeds();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.disclaimer);
                alertDialogBuilder.setMessage(R.string.disclaimer_txt);
                Intent intent = new Intent(this, PatientMakeOrderActivity.class);
                intent.putExtra(Patient.TABLE, mPatient);
                intent.putExtra(Encounter.TABLE, mEncounter);
                intent.putParcelableArrayListExtra(Medication.TABLE, active);
                final Intent in = intent;
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // This prevents duplicate generation of recommendations, and is good for workflow.
                                startActivityForResult(in, REQUEST_EXIT);
                                dialog.dismiss();
                            }
                        });
                alertDialogBuilder.create().show();


                break;
            case R.id.bAddTestHist:
                addNewTest(AddNewTestActivity.MEDICAL_HISTORY);
                break;
            case R.id.bAddTestLab:
                addNewTest(AddNewTestActivity.LAB_TEST);
                break;
            case R.id.bAddTestPhys:
                addNewTest(AddNewTestActivity.PHYSICAL_EXAMINATION);
                break;
            case R.id.bFinish:
                showCancelDialog(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EXIT && resultCode == RESULT_OK) {
            //       Recommender.getInstance().write_level_tests(this);
            runSync();
            finish();
            return;
        }
        Log.e(TAG, "result: " +resultCode + "ok: " + RESULT_OK + " cancel: " + RESULT_CANCELED + " request: " + requestCode + " stay: " +REQUEST_STAY);
        if (resultCode != RESULT_CANCELED || resultCode==RESULT_OK || requestCode==REQUEST_STAY) {
            refresh();
        }
    }

    public void refresh() {
        patientHistoryFragment.refresh();
        patientProfileInfoFragment.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.patient_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_patient: {
                Intent intent = new Intent(this, EditPatientActivity.class);
                intent.putExtra(Patient.TABLE, mPatient);
                startActivityForResult(intent, REQUEST_STAY);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        hideSoftKeyboard(this);
//        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        hideSoftKeyboard(this);
    }

//    public void hideSoftKeyboard(Activity activity) {
////        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
////        inputMethodManager.hideSoftInputFromWindow(bMakeOrder.getWindowToken(), 0);
//    }

    public void addNewTest(int priority) {
        Intent intent = new Intent(getApplicationContext(), AddNewTestActivity.class);
        intent.putExtra(Patient.TABLE, mPatient);
        intent.putExtra(Encounter.TABLE, mEncounter);
        intent.putExtra(AddNewTestActivity.PRIORITY, priority);
        startActivityForResult(intent, REQUEST_STAY);
    }

    private void createVisitAndEncounter() {
        try {
            mVisit = new Visit("", "", "", "", mPatient.uuid, getString(R.string.general_visit_cat));
        } catch (NullPointerException e) {
            Log.e(TAG, "BAD  UUID?" + getIntent().getParcelableExtra(Patient.TABLE).toString());
            Toast.makeText(this, getString(R.string.patient_not_found), Toast.LENGTH_SHORT);
            finish();
        }
        Uri uri_visit = getApplicationContext().getContentResolver().insert(Visit.URI, mVisit.putContentValues());
        Cursor visitCursor = getApplicationContext().getContentResolver().query(uri_visit, null, null, null, null);
        if (visitCursor.moveToFirst()) {
            mVisit = new Visit(visitCursor);
            patientHistoryFragment.setCurrentVisit(mVisit);
        } else
            Log.e(TAG, "unable to get saved visit");
        visitCursor.close();
        try {
            mEncounter = new Encounter(this, mVisit);
            Uri uri_enc = getApplicationContext().getContentResolver().insert(Encounter.URI, mEncounter.putContentValues());
            Cursor encCursor = getApplicationContext().getContentResolver().query(uri_enc, null, null, null, null);
            if (encCursor.moveToFirst()) {
                mEncounter = new Encounter(encCursor);
                Recommender.getInstance().setEncounter(mEncounter);
            } else
                Log.e(TAG, "unable to get saved enc");
            encCursor.close();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Attempting to create encounter: " + e.getMessage());
        }
    }

    private static void openLoadingDialog(PatientHistoryActivity a) {
        mDialog = new ProgressDialog(a);
        mDialog.setCancelable(false);

        mDialog.setTitle(a.getString(R.string.loading));
        mDialog.setMessage(a.getString(R.string.please_wait));
        mDialog.show();
    }

    public List<Encounter> getEncounters() {
        return mEncounters;
    }

    public void passEncountersToProfileFrag(List<Encounter> encounters){
        patientProfileInfoFragment.loadActiveMedications(encounters);
        mEncounters = encounters;
    }


}
