package com.smarking.mhealthsyria.app.view.patient.history;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.database.Models;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;
import com.smarking.mhealthsyria.app.print.DataForm;
import com.smarking.mhealthsyria.app.print.LifestyleForm;
import com.smarking.mhealthsyria.app.print.ViewPrinter;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;
import com.smarking.mhealthsyria.app.view.patient.order.PatientMakeOrderActivity;
import com.smarking.mhealthsyria.app.view.viewmodel.EncounterViewModel;


import net.sqlcipher.database.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PatientHistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnLongClickListener {
    private static final String TAG = PatientHistoryFragment.class.getSimpleName();
    private LinearLayout historyLinearLayout;
    private TextView tvLoadingWait;
    private Patient mPatient;
    private Visit mVisit;
    private List<Medication> activeMeds = new ArrayList<>();

    private List<Encounter> mEncounters = new ArrayList<>();

    private int historyLoaded = 0;
    private static int loading = 0;
    AsyncTask task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPatient = getArguments().getParcelable(Patient.UUID);
        historyLoaded = 0;
        loading = 0;
//        Handler handler = new Handler();
//        ContentObserver contentObserver = new ContentObserver(handler) {
//            @Override
//            public void onChange(boolean selfChange, Uri uri) {
//                super.onChange(selfChange, uri);
//                if(isAdded()){
//                    getLoaderManager().restartLoader(PatientHistoryActivity.QUERY_UUID, bundle,PatientHistoryFragment.this);
//                }
//            }
//        };
    }

    public void setCurrentVisit(Visit visit) {
        mVisit = visit;
    }

    public void refresh() {
        tvLoadingWait.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Patient.UUID, mPatient);
        if (historyLoaded == 0) {
            getLoaderManager().restartLoader(PatientHistoryActivity.QUERY_UUID, bundle, PatientHistoryFragment.this);
        } else {
            List<Visit> visit = new ArrayList<>();
            visit.add(mVisit);
            loadHistoryViews(visit);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_history, container, false);
        historyLinearLayout= (LinearLayout) rootView.findViewById(R.id.historyLinearLayout);
        tvLoadingWait = (TextView) rootView.findViewById(R.id.tvLoading);
//        populateTests();
//        populateHistory();
        historyLoaded = 0;
        refresh();
        return rootView;
    }

    private void createView(Visit visit) {

    }

    private void loadHistoryViews(List<Visit> visits) {
        loading = 1;
        if (historyLoaded == 0) {
            Recommender.getInstance().reset_main_lists();
        }
        mEncounters = new ArrayList<>();
        List<View> childViews = new ArrayList<>();

        for(Visit visit : visits){

            String whereClause = " %s = '%s' ";
            if (visit == null) {
                break;
            }
            Cursor encounterCursor = getActivity().getContentResolver().query(Encounter.URI, null,
                    String.format(whereClause, Encounter.VISIT_UUID, visit.uuid), null, Model.CREATED);

            List<EncounterViewModel> encViewModels = new ArrayList<>();


            while(encounterCursor.moveToNext()){
                Encounter encounter = new Encounter(encounterCursor);
                mEncounters.add(encounter);


                Cursor recordCursor = getActivity().getContentResolver().query(Record.URI, null, String.format(whereClause, Record.ENCOUNTER_UUID, encounter.uuid), null, null);
                while(recordCursor.moveToNext()){
                    Record record = new Record(recordCursor);
                    if (historyLoaded == 0)
                        Recommender.getInstance().addRec(record);
                    encViewModels.add(new EncounterViewModel(getActivity(), encounter, record));
                }
                recordCursor.close();

                Cursor testCursor = getActivity().getContentResolver().query(Test.URI, null, String.format(whereClause, Test.ENCOUNTER_UUID, encounter.uuid), null, null);
                while(testCursor.moveToNext()){
                    Test test = new Test(testCursor);
                    if (historyLoaded == 0)
                        Recommender.getInstance().addTest(test);
                    encViewModels.add(new EncounterViewModel(getActivity(), encounter, test));
                }
                testCursor.close();

                Cursor medicationCursor = getActivity().getContentResolver().query(Medication.URI, null,String.format(whereClause, Medication.ENCOUNTER_UUID, encounter.uuid), null, null);
                while(medicationCursor.moveToNext()){
                    Medication med = new Medication(medicationCursor);
                    if (historyLoaded == 0)
                        Recommender.getInstance().addMed(med);
                    encViewModels.add(new EncounterViewModel(getActivity(), encounter, med));
                }
                medicationCursor.close();

            }
            encounterCursor.close();
            if (!encViewModels.isEmpty())
                childViews.add(getChildView(encViewModels));
        }
        if (historyLoaded == 0) {
            PatientHistoryActivity parent = (PatientHistoryActivity) getActivity();
            parent.passEncountersToProfileFrag(mEncounters);
        }
        addChildViewsToLayout(childViews, historyLinearLayout);
        if (historyLoaded == 1 && childViews.size() == 1) {
            historyLoaded = 2;
        }
        if (historyLoaded == 0) {
            historyLoaded = 1;
            try {
                PatientHistoryActivity.mDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Exception Shutting Down loading dialog: " + e.getMessage());
            }
        }
        loading = 0;
    }

    private void addChildViewsToLayout(final List<View> childViews, final LinearLayout layout){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLoadingWait.setVisibility(View.GONE);
                if (!childViews.isEmpty()) {
                    if (historyLoaded == 0) {
                        for (View child : childViews)
                            layout.addView(child);
                    } else if (historyLoaded == 1) {
                        if (childViews.size() > 1) {
                            Log.e(TAG, "HL=1 MULTIPLe CHILD VIEWS!");
                            layout.removeAllViews();
                            for (View child : childViews)
                                layout.addView(child);
                        } else {
                            layout.addView(childViews.get(0), 0);
                        }

                    } else {
                        if (childViews.size() > 1)
                            Log.e(TAG, "HL=1 MULTIPLe CHILD VIEWS!");
                        layout.removeViewAt(0);
                        layout.addView(childViews.get(0), 0);
                    }
                }
            }
        });
    }

    private View getChildView(List<EncounterViewModel> encViewModels){
        View childView = View.inflate(getActivity(), R.layout.fragment_patient_history_child, null);
        TextView docTv = (TextView) childView.findViewById(R.id.tvDoctorName);
        TextView dateTv = (TextView) childView.findViewById(R.id.tvDate);
        final LinearLayout encLinearLayout = (LinearLayout) childView.findViewById(R.id.testsLinearLayout);

        Collections.sort(encViewModels);
        int idIndex = 0;
        for(EncounterViewModel encounterViewModel:encViewModels){
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View convertView = inflater.inflate(R.layout.encounter_list_item, historyLinearLayout, false);


            TextView tvValueResult = (TextView) convertView.findViewById(R.id.tvValueResult);
            TextView tvCategory = (TextView) convertView.findViewById(R.id.tvCategory);
            TextView tvComment = (TextView) convertView.findViewById(R.id.tvComment);


            tvCategory.setText(encounterViewModel.category);
            if (encounterViewModel.physician != null && !encounterViewModel.physician.isEmpty())
                docTv.setText(encounterViewModel.physician);

            ///String sDateLong = encounterViewModel.lastModified.toString();
            //String[] sDateSplit = sDateLong.split(" ");
            //String sDateShort = sDateSplit[1] + " " + sDateSplit[2] + " " + sDateSplit[5];

            dateTv.setText(Model.LEAST_DATE.format(encounterViewModel.lastModified));

            if (encounterViewModel.comment == null || encounterViewModel.comment.isEmpty())
                tvComment.setVisibility(View.GONE);
            else
                tvComment.setText(encounterViewModel.comment);

            if (encounterViewModel.valueResult == null || encounterViewModel.valueResult.isEmpty())
                tvValueResult.setVisibility(View.GONE);
            else
                tvValueResult.setText(encounterViewModel.valueResult);

            if (encounterViewModel.level > -1) {
                if (encounterViewModel.level == 0) {
                    tvValueResult.setTextColor(getResources().getColor(R.color.gr_good));
                } else if (encounterViewModel.level == 1) {
                    tvValueResult.setTextColor(getResources().getColor(R.color.or_med));
                } else {
                    tvValueResult.setTextColor(getResources().getColor(R.color.red_bad));
                }
            }
            // Set the 0th tag to the encounter uuid
            convertView.setTag(encounterViewModel.uuid);
            // Set the long click listener to this
            convertView.setOnLongClickListener(this);
            convertView.setId(++idIndex);
            encLinearLayout.addView(convertView);
        }
        childView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return childView;

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.e(TAG, "create loader");
        if (loading == 1)
            return null;
        switch(id){
            case PatientHistoryActivity.QUERY_UUID:
                final Patient patient = args.getParcelable(Patient.UUID);
                return new com.smarking.mhealthsyria.app.api.sync.CursorLoader(getActivity()) {

                    @Override
                    public Cursor loadInBackground() {
                        DatabaseHandler dbHandler = DatabaseHandler.get(getContext());
                        SQLiteDatabase db = dbHandler.openDatabase();

                        String query = String.format(" SELECT %s AS %s, %s.*",
                                Model.getTableColumnName(VisitCategory.TABLE, VisitCategory.DISPLAYNAME), VisitCategory.DISPLAYNAME,
                                Visit.TABLE)
                                + String.format(" FROM %s ", Visit.TABLE)
                                + String.format(" JOIN %s ON %s = %s ", VisitCategory.TABLE, Model.getTableColumnName(Visit.TABLE, Visit.CATEGORY_UUID), Model.getTableColumnName(VisitCategory.TABLE, VisitCategory.UUID))
                                + String.format(" WHERE %s = '%s' ", Model.getTableColumnName(Visit.TABLE, Visit.PATIENT_UUID), patient.uuid)
                                + String.format(" ORDER BY %S DESC ", Model.getTableColumnName(Visit.TABLE, Visit.CREATED));

                        Cursor cursor =  db.rawQuery(query, null);
                        return cursor;
                    }
                };
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final List<Visit> visits = new ArrayList<>();
        while(data.moveToNext()){
            Visit visit = new Visit(data);
            visits.add(visit);
        }

        Collections.sort(visits, Recommender.compCreate);

        getLoaderManager().destroyLoader(PatientHistoryActivity.QUERY_UUID);
        if (historyLoaded == 0) {
            historyLinearLayout.removeAllViews();
        }
        task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                loadHistoryViews(visits);
                return null;
            }
        };
        task.execute();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (task != null) {
            if (!task.isCancelled()) {
                task.cancel(true);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        historyLinearLayout.removeAllViews();
    }

    @Override
    public boolean onLongClick(final View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.title_activity_print_dialog);

        // set dialog message
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        createPrintout(v);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.create().show();
        return true;
    }

    public boolean createPrintout(View v) {
        Log.i(TAG, "onLongClick(View) id=" + v.getId() + ", tag=" + v.getTag());
        // this should only respond to long clicks on the encounter items
        Object uuid = v.getTag();
        // Only do something here if the uuid was set as the 0th tag
        if(uuid != null){
            try {
                String uuidStr = String.valueOf(uuid);
                Log.d(TAG, "Fetching Encounter: uuid="+uuidStr);
                Encounter mEncounter = Encounter.get(getActivity(), uuidStr);
                Log.d(TAG, "Got encounter: " + mEncounter );
                // TODO add sort parameter or manual sort if necessary
                List<Test> tests = Models.TESTS.all(getActivity(), uuidStr, Test.ENCOUNTER_UUID);
                List<Record> records = Models.RECORDS.all(getActivity(), uuidStr, Record.ENCOUNTER_UUID);
                // TODO get the meds
                View childView = View.inflate(getActivity(), R.layout.fragment_patient_history_child, null);
                final LinearLayout encLinearLayout = (LinearLayout) childView.findViewById(R.id.testsLinearLayout);
 /*               List<String> encounterUuids = new ArrayList<>();
                for(int i = 0; i < encLinearLayout.getChildCount(); i++){
                    View view = encLinearLayout.getChildAt(i);
                    Object tag = view.getTag();
                    String instanceUuid = String.valueOf(tag);
                    if(tag != null && !TextUtils.isEmpty(instanceUuid)) {
                        encounterUuids.add(instanceUuid);
                    }
                }
                // reload Active Medications*/
                List<String> curEncounters = new ArrayList<>();
                for (Encounter encounter : mEncounters) {
                    if (!encounter.getCreatedDate().after(mEncounter.getCreatedDate())) {
                        curEncounters.add(encounter.uuid);
                    }
                }
                loadActiveMedications(curEncounters, mEncounter);
                // load forms and print

                Category.Language lang = Category.language;

                ((AuthenticatedActivity) getActivity()).updateLanguage("ar");
                LifestyleForm pform = new LifestyleForm(mEncounter, tests, records, activeMeds, getActivity(), lang);
                Bitmap pmap = ViewPrinter.printForm(getActivity(), pform.setUpLifestyleForm(getActivity()));

                ((AuthenticatedActivity) getActivity()).updateLanguage(SessionManager.get(getActivity()).getDeviceClinicLanguage());
                DataForm dataform = new DataForm(mEncounter, tests, records, activeMeds, getActivity());
                Bitmap datamap = ViewPrinter.printForm(getActivity(), dataform.setUpDataForm(getActivity()));
                ViewPrinter.printBitmaps(new Bitmap[]{datamap, pmap}, new int[]{2, 1}, getActivity(), "report");
            } catch(Exception e){
                Log.e(TAG, "Error printing");
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }


    public void loadActiveMedications(List<String> encounterUuids, Encounter enc) {
        String whereClause = " %s = '%s' ";
        activeMeds.clear();
        for (String encounter : encounterUuids) {
            Cursor medCursor = null;
            try {
                medCursor = getActivity().getContentResolver().query(
                        Medication.URI, null,
                        String.format(whereClause, Medication.ENCOUNTER_UUID, encounter),
                        null, null);
                while (medCursor.moveToNext()) {
                    Medication medication = new Medication(medCursor);
                    String sEndDate = medication.getEndDate();
                    if (sEndDate == null || sEndDate.isEmpty() || sEndDate.equals("null"))
                        activeMeds.add(medication);
                    else {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date endDate = formatter.parse(sEndDate);
                            Date now = enc.getCreatedDate();
                            int compare = now.compareTo(endDate);
                            if (compare <= 0) {
                                Recommender.getInstance().addCurMed(medication);
                                activeMeds.add(medication);
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, e.toString() + " " + sEndDate);
                        }
                    }
                }
            } catch (Exception e){
                Log.e(TAG, "Error loading meds");
                e.printStackTrace();
            } finally {
                if(medCursor != null) medCursor.close();
            }
        }
    }
}
