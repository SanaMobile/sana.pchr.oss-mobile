package com.smarking.mhealthsyria.app.view.patient.order;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.Patient;
;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.category.TestCategory;
import com.smarking.mhealthsyria.app.view.custom.CategoryAdapter;
import com.smarking.mhealthsyria.app.view.custom.MedicationEdit;
import com.smarking.mhealthsyria.app.view.loader.CategoryLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientWorkFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Category>> {
    private ListView expListView;
    private WorkOrderListAdapter expListAdapter;
    private PatientMakeOrderActivity mActivity;
    private List<RecordCategory> recordCategories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (PatientMakeOrderActivity) getActivity();

        Patient patient = getArguments().getParcelable(Patient.UUID);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Patient.TABLE, patient);

        getLoaderManager().initLoader(PatientMakeOrderActivity.QUERY_UUID, bundle, this);


        Handler handler = new Handler();
        ContentObserver contentObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                if(isAdded()){
                    getLoaderManager().restartLoader(PatientMakeOrderActivity.QUERY_UUID, bundle, PatientWorkFragment.this);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_work, container, false);
        expListView = (ListView) rootView.findViewById(R.id.workExpListView);
        setItemClickListener();
        return rootView;
    }

    private void setItemClickListener(){
        expListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecordCategory recordCategory = expListAdapter.getItem(position);
                if (recordCategory.uuid.equals(Constants.RECORD_CATEGORY_TEST_UUID))
                    openTestPickerDialog(recordCategory, mActivity);
                else if (recordCategory.uuid.equals(Constants.RECORD_CATEGORY_FOLLOWUP_UUID))
                    openDatePickerDialog(recordCategory);
                else if (recordCategory.uuid.equals(Constants.RECORD_CATEGORY_MEDICATION_UUID))
                    MedicationEdit.openEmptyEditDialog((PatientMakeOrderActivity) getActivity());
                else openCategoryAddDialog(recordCategory);
            }
        });
    }

    private void openTestPickerDialog(final RecordCategory category, final PatientMakeOrderActivity mActivity) {
        Cursor testCursor = getActivity().getContentResolver().query(TestCategory.URI, null,
                "priority > 600 AND priority < 700", null, null);
        final List<String> names = new ArrayList<>();
        final List<String> arnames = new ArrayList<>();
        final List<String> tests = ((PatientMakeOrderActivity) getActivity()).getTests();
        final Set<Integer> selected = new HashSet<>();
        String curname;
        while (testCursor.moveToNext()) {
            TestCategory tc = new TestCategory(testCursor);
            curname = tc.getDisplayName();
            if (!tests.contains(curname)) {
                names.add(curname);
                arnames.add(tc.getDisplayNameAr());
            }
        }


        testCursor.close();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.recommend_test)
                .setMultiChoiceItems(names.toArray(new String[names.size()]),
                        null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    selected.add(which);
                                } else if (selected.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    selected.remove(which);
                                }
                            }
                        })

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dd, int which) {
                        dd.cancel();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dd, int which) {
                        for (Integer i : selected) {
                            mActivity.addRecommendation(category, names.get(i), "", arnames.get(i), "");
                        }
                        dd.dismiss();
                    }
                });

        builder.create().show();
    }

    private void openDatePickerDialog(final RecordCategory recordCategory){
        assert recordCategory.uuid.equals(Constants.RECORD_CATEGORY_FOLLOWUP_UUID);
        Calendar mCurrentTime = Calendar.getInstance();
        int currYear = mCurrentTime.get(Calendar.YEAR);
        int currMonth = mCurrentTime.get(Calendar.MONTH);
        int currDay = mCurrentTime.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(),new DatePickerDialog.OnDateSetListener(){

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mActivity.addRecomendation(recordCategory,year+"-"+monthOfYear+"-"+dayOfMonth,null);
            }
        }, currYear, currMonth, currDay);
        mDatePicker.show();
    }

    private void openCategoryAddDialog(final RecordCategory recordCategory){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder.setTitle(getString(R.string.add_record));
        alertDialogBuilder.setMessage(recordCategory.getDisplayName());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText comment = new EditText(getActivity());
        final EditText value = new EditText(getActivity());

        value.setHint(getString(R.string.value));
        comment.setHint(getString(R.string.comment));

        layout.addView(value);
        layout.addView(comment);

        alertDialogBuilder.setView(layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mActivity.addRecomendation(recordCategory, value.getText().toString(), comment.getText().toString());
                        if(Constants.LIFE_STYLE_RECORDS.contains(recordCategory.uuid)){
                            recordCategories.remove(recordCategory);
                            expListAdapter.notifyDataSetChanged();
                        }
                        dialog.dismiss();

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void openSubCategoryAddDialog(final Category category, final RecordCategory recordCategory){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder.setTitle(getString(R.string.add_record));
        alertDialogBuilder.setMessage(category.getDisplayName());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText comment = new EditText(getActivity());
        comment.setHint(getString(R.string.comment));
        layout.addView(comment);

        alertDialogBuilder.setView(layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mActivity.addRecomendation(recordCategory, category.getDisplayName(), comment.getText().toString());
                        dialog.dismiss();

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public Loader<List<Category>> onCreateLoader(int id, Bundle args) {
        return new CategoryLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Category>> loader, List<Category> categories) {
        recordCategories = new ArrayList<>();
        for(Category category: categories){
            if(category.getClass().equals(RecordCategory.class))
                recordCategories.add((RecordCategory) category);
        }
        expListAdapter = new WorkOrderListAdapter(getActivity(), recordCategories);
        expListView.setAdapter(expListAdapter);
    }

    @Override
    public void onLoaderReset(Loader<List<Category>> loader) {

    }



}
