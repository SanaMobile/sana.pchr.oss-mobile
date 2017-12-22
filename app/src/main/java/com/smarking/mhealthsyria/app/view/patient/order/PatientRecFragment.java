package com.smarking.mhealthsyria.app.view.patient.order;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.view.login.RecoveryDialog;

import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {} interface
 * to handle interaction events.
 * Use the {} factory method to
 * create an instance of this fragment.
 */
public class PatientRecFragment extends Fragment{
    private String TAG = PatientRecFragment.class.getSimpleName();
    private ExpandableListView recListView;
    private RecListAdapter recListAdapter;
    private ArrayList<RecordCategory> recommendedRecords;
    private ArrayList<String> comments;
    private ArrayList<String> values;
    private ArrayList<Medication> medications;
    private ArrayList<String> comments_ar;
    private ArrayList<String> values_ar;

    public List<String> getTests() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < recommendedRecords.size(); i++) {
            if (recommendedRecords.get(i).uuid.equals(Constants.RECORD_CATEGORY_TEST_UUID)) {
                names.add(values.get(i));
            }
        }
        return names;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Patient patient = getArguments().getParcelable(Patient.UUID);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Patient.UUID, patient);

        //Parse recommendations
        List<Record> recommend = Recommender.getInstance().getRecommendation();
        recommendedRecords = new ArrayList<>();
        comments = new ArrayList<>();
        values = new ArrayList<>();
        medications = new ArrayList<>();
        comments_ar = new ArrayList<>();
        values_ar = new ArrayList<>();
        for (Record record : recommend) {
            recommendedRecords.add(RecordCategory.get(getActivity(), record.getCategory_uuid()));
            comments.add(record.getComment());
            values.add(record.getValue());
            comments_ar.add(record.getCommentAr());
            values_ar.add(record.getValueAr());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_rec, container, false);
        recListView = (ExpandableListView) rootView.findViewById(R.id.recExpListView);
        recListAdapter = new RecListAdapter(getActivity(), recommendedRecords, values, comments, medications);
        recListView.setAdapter(recListAdapter);
        recListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int groupPos = ExpandableListView.getPackedPositionGroup(id);

                Log.e(TAG, "GPOS: " + groupPos + "SIZEZ: " + recommendedRecords.size());
                if (groupPos < recommendedRecords.size()) {
                    openModifyRecordDialog(groupPos);
                } else {
                    openRemoveMedicationDialog(position);
                }
                return true;
            }
        });


        return rootView;
    }


    private void openModifyRecordDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder.setTitle(R.string.modify);
        alertDialogBuilder.setMessage(recommendedRecords.get(position).getDisplayName());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText comment = new EditText(getActivity());
        final EditText value = new EditText(getActivity());

        value.setText(values.get(position));
        comment.setText(comments.get(position));
        layout.addView(value);
        layout.addView(comment);

        alertDialogBuilder.setView(layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        comments.set(position, comment.getText().toString());
                        values.set(position, value.getText().toString());
                        recListAdapter.notifyDataSetChanged();

                        if (recommendedRecords.get(position).uuid.equals(Constants.RECORD_CATEGORY_FOLLOWUP_UUID)) {
                            try {
                                Date d = Model.LEAST_DATE.parse(values.get(position));
                                dialog.dismiss();
                            } catch (ParseException e) {
                                Toast.makeText(getActivity(), String.format("%s: %s, %s", getString(R.string.invalid), getString(R.string.date), "YYYY-MM-DD"), Toast.LENGTH_SHORT);
                            }
                        } else {
                            dialog.dismiss();
                        }
                    }
                })
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        recommendedRecords.remove(position);
                        values.remove(position);
                        comments.remove(position);
                        values_ar.remove(position);
                        comments_ar.remove(position);
                        recListAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

    private void openRemoveMedicationDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder.setTitle(R.string.delete_meds);
//        alertDialogBuilder.setMessage(recomendations.get(position).toString());

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        medications.remove(position - recommendedRecords.size() - 1);
                        recListAdapter.notifyDataSetChanged();
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

    public void addRecord(RecordCategory category, String value, String comment, String values_ar, String comments_ar) {
        recommendedRecords.add(category);
        values.add(value);
        comments.add(comment);
        this.comments_ar.add(values_ar);
        this.values_ar.add(comments_ar);
        recListAdapter.notifyDataSetChanged();
    }

    public void addRecord(RecordCategory category,String value, String comment){
        recommendedRecords.add(category);
        values.add(value);
        comments.add(comment);
        comments_ar.add("");
        values_ar.add("");
        recListAdapter.notifyDataSetChanged();
    }

    public void addMedication(Medication medication){
        medications.add(medication);
        recListAdapter.notifyDataSetChanged();
    }

    public Pair<ArrayList<Record>, ArrayList<Medication>> saveRecords(Encounter encounter) throws JSONException {
        assert recommendedRecords.size() == comments.size() && recommendedRecords.size() == values.size()
                && values.size() == values_ar.size() && comments.size() == comments_ar.size();

        List<Record> records = new ArrayList<>(recommendedRecords.size());

        for (int i = 0; i < recommendedRecords.size(); i++) {
            Record record = new Record(values.get(i), comments.get(i), recommendedRecords.get(i), encounter);
            Uri uri_record = getActivity().getContentResolver().insert(Record.URI, record.putContentValues());
            Cursor rCursor = getActivity().getContentResolver().query(uri_record, null,null,null,null);
            Log.e(TAG, "rCursor:" + rCursor.getCount());
            while(rCursor.moveToNext()){
                record = new Record(rCursor);
                record.setCommentAr(comments_ar.get(i));
                record.setValueAr(values_ar.get(i));
                records.add(record);
            }
            rCursor.close();
        }

        for(Medication medication: medications){
            medication.setEncounterUuid(encounter.uuid);
            Uri uri_med = getActivity().getContentResolver().insert(Medication.URI, medication.putContentValues());
        }
        return new Pair(records, medications);
    }




}
