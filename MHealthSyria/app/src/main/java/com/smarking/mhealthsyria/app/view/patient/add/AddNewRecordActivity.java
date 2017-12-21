package com.smarking.mhealthsyria.app.view.patient.add;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;
import com.smarking.mhealthsyria.app.view.custom.CategoryAdapter;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-24.
 */
public class AddNewRecordActivity extends AuthenticatedActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static final int LOADER_ENCOUNTER_CAT = 0;
    public static final int LOADER_RECORD_CAT = 1;

    public static String TAG = AddNewRecordActivity.class.getSimpleName();
    private Spinner sRecordCategory;
    private Spinner sEncounterCategory;
    private EditText etValue;
    private EditText etComment;
    private CategoryAdapter<RecordCategory> recordCategoryAdapter;
    private CategoryAdapter<EncounterCategory> encounterCategoryAdapter;
    private Visit mVisit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_record_activity);

        sEncounterCategory = (Spinner) findViewById(R.id.sEncounterCategory);
        encounterCategoryAdapter = new CategoryAdapter<>(this, R.layout.list_category_item, new ArrayList<EncounterCategory>());
        sEncounterCategory.setAdapter(encounterCategoryAdapter);

        sRecordCategory = (Spinner) findViewById(R.id.sRecordCategory);
        recordCategoryAdapter = new CategoryAdapter<>(this, R.layout.list_category_item, new ArrayList<RecordCategory>());
        sRecordCategory.setAdapter(recordCategoryAdapter);

        etValue = (EditText) findViewById(R.id.etValue);
        etComment = (EditText) findViewById(R.id.etComment);

        getLoaderManager().initLoader(LOADER_ENCOUNTER_CAT, null, this);
        getLoaderManager().initLoader(LOADER_RECORD_CAT, null, this);

        Button bAdd = (Button) findViewById(R.id.bAdd);
        Button bCancel = (Button) findViewById(R.id.bClear);

        mVisit = getIntent().getParcelableExtra(Visit.TABLE);

        bAdd.setOnClickListener(this);
        bCancel.setOnClickListener(this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ENCOUNTER_CAT: {
                return new CursorLoader(this, EncounterCategory.URI, null, null, null, null);
            }
            case LOADER_RECORD_CAT: {
                return new CursorLoader(this, RecordCategory.URI, null, null, null, null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ENCOUNTER_CAT: {
                encounterCategoryAdapter.clear();
                while (data.moveToNext()) {
                    encounterCategoryAdapter.add(new EncounterCategory(data));
                }
                break;
            }
            case LOADER_RECORD_CAT: {
                recordCategoryAdapter.clear();
                while (data.moveToNext()) {
                    recordCategoryAdapter.add(new RecordCategory(data));
                }
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        switch (loader.getId()) {
            case LOADER_ENCOUNTER_CAT: {
                encounterCategoryAdapter.clear();
                break;
            }
            case LOADER_RECORD_CAT: {
                recordCategoryAdapter.clear();
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bAdd: {
                final EncounterCategory encounterCategory = encounterCategoryAdapter.getItem(sEncounterCategory.getSelectedItemPosition());
                final RecordCategory recordCategory = recordCategoryAdapter.getItem(sRecordCategory.getSelectedItemPosition());
                final String value = etValue.getText().toString().trim();
                final String comment = etComment.getText().toString().trim();

                String message = null;
                if (value.isEmpty() || value.length() > Constants.VARCHAR_LIMIT) {
                    message = getString(R.string.value);
                }

                if (message != null) {
                    new AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.error)
                            .setMessage(getString(R.string.invalid) +" " + message)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                } else {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle(getString(R.string.loading));
                    progressDialog.setMessage(getString(R.string.wait_record_adding));
                    progressDialog.setCancelable(false);

                    Task.callInBackground(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            if (mVisit == null) {
                                VisitCategory visitCategory = getIntent().getParcelableExtra(VisitCategory.TABLE);
                                Patient patient = getIntent().getParcelableExtra(Patient.TABLE);

                                Visit visit = new Visit(patient.uuid, visitCategory);
                                mVisit = Visit.save(AddNewRecordActivity.this, visit);
                            }
                            Uri uri = Record.create(AddNewRecordActivity.this, mVisit, encounterCategory, value, comment, recordCategory);
                            return null;
                        }
                    }).continueWith(new Continuation<Void, Void>() {
                        @Override
                        public Void then(Task<Void> task) throws Exception {
                            if (task.isFaulted()) {
                                progressDialog.setTitle(getString(R.string.error));
                                progressDialog.setMessage(getString(R.string.error_creating_record));
                                progressDialog.setCancelable(true);
                                Log.e(TAG, "Error creating new record");
                            } else {
                                progressDialog.dismiss();
                                finish();
                            }
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
                }
                break;
            }
            case R.id.bClear: {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.dialog_text_clear_title)
                        .setMessage(R.string.confirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                etValue.setText("");
                                etComment.setText("");
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                break;
            }
        }
    }
}
