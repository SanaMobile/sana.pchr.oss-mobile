package com.smarking.mhealthsyria.app.view.patient.order;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.Reporter;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.print.DataForm;
import com.smarking.mhealthsyria.app.print.LifestyleForm;
import com.smarking.mhealthsyria.app.print.ViewPrinter;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PatientMakeOrderActivity extends AuthenticatedActivity implements View.OnClickListener, View.OnLongClickListener{
    private static final String TAG = PatientMakeOrderActivity.class.getSimpleName();
    private Patient mPatient;
    private Activity mActivity;
    public static final int QUERY_UUID = 2;
    private Encounter mEncounter;
    private ArrayList<Medication> mActiveMed;

    private PatientRecFragment patientRecFragment;
    private PatientWorkFragment patientWorkFragment;
    private PatientMedFragment patientMedFragment;
    private PatientActiveFragment patientActiveFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_make_order_activity);

        patientRecFragment = new PatientRecFragment();
        patientWorkFragment = new PatientWorkFragment();
        patientMedFragment = new PatientMedFragment();
        patientActiveFragment = new PatientActiveFragment();

        Button bFinish = (Button) findViewById(R.id.bFinishOrder);
        Button bCancel = (Button) findViewById(R.id.bCancelOrder);

        bFinish.setOnClickListener(this);
        bCancel.setOnClickListener(this);

        mPatient = getIntent().getParcelableExtra(Patient.TABLE);
        mEncounter = getIntent().getParcelableExtra(Encounter.TABLE);
        mActiveMed = getIntent().getParcelableArrayListExtra(Medication.TABLE);

        Fragment[] fragment = new Fragment[]{patientRecFragment, patientWorkFragment, patientMedFragment, patientActiveFragment};
        for (Fragment aFragment : fragment) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Patient.UUID, mPatient);
            bundle.putParcelableArrayList(Medication.TABLE, mActiveMed);
            aFragment.setArguments(bundle);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.patient_rec_frame, patientRecFragment)
                    .add(R.id.patient_work_frame, patientWorkFragment)
                    .add(R.id.patient_med_frame, patientMedFragment)
                    .add(R.id.patient_active_frame, patientActiveFragment)
                    .commit();
        }
        Reporter.encounterStart(this);
//        patientActiveFragment.passActiveMedUuids(mActiveUuids);
//        patientActiveFragment.passActiveMeds(mActiveMeds);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bCancelOrder: {
                showCancelDialog(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Reporter.encounter(PatientMakeOrderActivity.this, "Cancel pressed");
                        Reporter.encounterEnd(PatientMakeOrderActivity.this);
                        Recommender.getInstance().clear_recs();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
                break;
            }
            case R.id.bFinishOrder:{
                setResult(RESULT_OK);
                showFinishDialog();
                break;
            }
        }
    }

    public void showCancelDialog(DialogInterface.OnClickListener listen) {
        mActivity = PatientMakeOrderActivity.this;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.cancel);
        alertDialogBuilder.setMessage(getString(R.string.cancel_work));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.ok, listen)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.create().show();
    }

    public void showFinishDialog() {
        mActivity = PatientMakeOrderActivity.this;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.finishPrint);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //    try{
                            saveAndPrintSimpleReport();
                        //  } catch (Exception e) {
                        //    e.printStackTrace();
                        // }

                        setResult(RESULT_OK);
                        Reporter.encounterEnd(PatientMakeOrderActivity.this);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.create().show();
    }

    public void addRecomendation(RecordCategory recCat , String value, String comment) {
        patientRecFragment.addRecord(recCat, value, comment);
    }

    public void addRecommendation(RecordCategory recCat, String value, String comment, String value_ar, String comment_ar) {
        patientRecFragment.addRecord(recCat, value, comment, value_ar, comment_ar);
    }

    public List<String> getTests() {
        return patientRecFragment.getTests();
    }

    public void addMedication(Medication medication){
        patientRecFragment.addMedication(medication);
    }

    @Override
    public void onBackPressed() {
        showCancelDialog(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Reporter.encounter(PatientMakeOrderActivity.this, "Back pressed");
                Reporter.encounterEnd(PatientMakeOrderActivity.this);
                Recommender.getInstance().clear_recs();
                setResult(RESULT_CANCELED);
                finish();
            }
        });

    }

    protected final void saveAndPrintSimpleReport() {
        Reporter.encounter(this, "Enter saveAnPrintSimpleReport()");
        // Call the printservice method which will launch the service and print
        Log.e(TAG, "simple encounter: " + mEncounter);

        Recommender.getInstance().write_level_tests(this);

        Pair<ArrayList<Record>, ArrayList<Medication>> res = null;
        ArrayList<Test> tests = new ArrayList<>();
        ArrayList<Record> records = new ArrayList<>();
        ArrayList<Medication> meds = new ArrayList<>();

        // TODO This can bubble up db exceptions
        try {
            res = patientRecFragment.saveRecords(mEncounter);
        } catch (JSONException e) {
            Log.e(TAG, "json: " + e.getLocalizedMessage());
        }

        if (res != null) {
            if (res.first != null) {
                records = res.first;
            }
            if (res.second != null) {
                meds = res.second;
            }
        }

        // TODO This can bubble up db exceptions
        try {
            ArrayList<Medication> active = patientActiveFragment.saveActive(mEncounter);
            meds.addAll(active);
        } catch (JSONException e) {
            Log.e(TAG, "json: " + e.getLocalizedMessage());
        }

        // TODO This can bubble up db locked exceptions
        android.database.Cursor testCursor = getContentResolver().query(Test.URI, null, String.format(" %s = '%s' ",
                Test.ENCOUNTER_UUID, mEncounter.uuid), null, null);
        while (testCursor.moveToNext()) {
            tests.add(new Test(testCursor));
        }
        testCursor.close();

        DataForm dataform = new DataForm(mEncounter, tests, records, meds, this);

        Category.Language lang = Category.language;

        updateLanguage("ar");
        LifestyleForm pform = new LifestyleForm(mEncounter, tests, records, meds, this, lang);
        Bitmap pmap = ViewPrinter.printForm(this, pform.setUpLifestyleForm(this));

        updateLanguage(SessionManager.get(this).getDeviceClinicLanguage());

        Bitmap datamap = ViewPrinter.printForm(this, dataform.setUpDataForm(this));
        ViewPrinter.printBitmaps(new Bitmap[]{datamap, pmap}, new int[]{2, 1}, this, "report");

        Reporter.encounter(this, "Exit saveAnPrintSimpleReport()");
    }


    public void setMedView(int frame, int visibility) {
        FrameLayout medFrame = (FrameLayout) findViewById(frame);
        medFrame.setVisibility(visibility);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
