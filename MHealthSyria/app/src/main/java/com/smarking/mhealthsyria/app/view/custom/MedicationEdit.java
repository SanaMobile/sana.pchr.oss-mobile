package com.smarking.mhealthsyria.app.view.custom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.category.DoseUnitCategory;
import com.smarking.mhealthsyria.app.model.category.IntervalUnitCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationGroupCategory;
import com.smarking.mhealthsyria.app.view.patient.order.PatientMakeOrderActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by abe707 on 1/13/16.
 */
public class MedicationEdit {

    private static final Map<String, MedicationGroupCategory> mapNameToGroupCategory = new HashMap<>();
    private static final Map<String, MedicationCategory> mapNameToCategory = new HashMap<>();
    private static final String TAG  = MedicationEdit.class.getSimpleName();
    private static MedicationGroupCategory mMedicationGroupCategory = null;
    private static MedicationCategory mMedicationCategory = null;

    public static void openActiveEditDialog(final Activity activity, final int position, final ArrayList<Medication> activeMeds, final ActiveMedListAdapter adapter){
        resetVars();
        final Medication med = activeMeds.get(position);
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.medication_edit);
        dialog.setTitle(R.string.medication);

        final EditText etDose = (EditText) dialog.findViewById(R.id.etDose);
        final Spinner spDoseUnit = (Spinner) dialog.findViewById(R.id.spinDoseUnit);
        final EditText etTimes = (EditText) dialog.findViewById(R.id.etTimes);
        final Spinner spIntervalUnit = (Spinner) dialog.findViewById(R.id.spinIntervalUnit);
        final Button bPickDate = (Button) dialog.findViewById(R.id.bPickDate);
        final Button bStopMed = (Button) dialog.findViewById(R.id.bStop);
        final TextView tvEndDate = (TextView) dialog.findViewById(R.id.tvEndDate);
        final TextView tvTimes = (TextView) dialog.findViewById(R.id.tvInterval);

        final TextView tvInteraction = (TextView) dialog.findViewById(R.id.tvInteraction);
        final EditText etComment = (EditText) dialog.findViewById(R.id.etComment);
        final LinearLayout llDose = (LinearLayout) dialog.findViewById(R.id.llDose);

        setStopMedClickListener(activity, dialog, bStopMed, med, activeMeds, adapter);

        setCommonListeners(activity, bPickDate, tvEndDate, dialog);

        etDose.setText(med.getDose());
        etTimes.setText(med.getTimes());
        etComment.setText(med.getComment());

        loadDoseUnitIntoSpinner(activity, spDoseUnit, med);
        loadIntervalUnitIntoSpinner(activity, spIntervalUnit, med, tvTimes, etTimes);


        setActiveOkButtonListener(activity, dialog, etDose, etTimes, spDoseUnit, spIntervalUnit,
                tvEndDate, etComment, adapter, med, llDose);

        dialog.show();
    }

    public static void openEmptyEditDialog(final PatientMakeOrderActivity activity) {
        resetVars();
        final Dialog dialog = new Dialog(activity);
        dialog.setTitle(R.string.medication);
        dialog.setContentView(R.layout.medication_edit);

//        final Spinner spMedGroup = (Spinner) dialog.findViewById(R.id.spinMedGroup);
//        final Spinner spMedCat = (Spinner) dialog.findViewById(R.id.spinMedCat);

        LinearLayout llMedGroups = (LinearLayout) dialog.findViewById(R.id.llMedGroup);
        llMedGroups.setVisibility(View.VISIBLE);

        final AutoCompleteTextView actvMedGroup = (AutoCompleteTextView) dialog.findViewById(R.id.actvMedGroup);
        final AutoCompleteTextView actvMedCat = (AutoCompleteTextView) dialog.findViewById(R.id.actvMedCat);

        final EditText etDose = (EditText) dialog.findViewById(R.id.etDose);
        final Spinner spDoseUnit = (Spinner) dialog.findViewById(R.id.spinDoseUnit);

        final EditText etTimes = (EditText) dialog.findViewById(R.id.etTimes);
        final Spinner spIntervalUnit = (Spinner) dialog.findViewById(R.id.spinIntervalUnit);
        final Button bPickDate = (Button) dialog.findViewById(R.id.bPickDate);
        final TextView tvEndDate = (TextView) dialog.findViewById(R.id.tvEndDate);
        final TextView tvTimes = (TextView) dialog.findViewById(R.id.tvInterval);
        final LinearLayout llDose = (LinearLayout) dialog.findViewById(R.id.llDose);

        final TextView tvInteraction = (TextView) dialog.findViewById(R.id.tvInteraction);
        final EditText etComment = (EditText) dialog.findViewById(R.id.etComment);
        final LinearLayout llMedCats = (LinearLayout) dialog.findViewById(R.id.llMedCat);
        llMedCats.setVisibility(View.VISIBLE);

        setCommonListeners(activity, bPickDate, tvEndDate, dialog);

        setupAutoMedGroup(activity, actvMedGroup, actvMedCat, tvInteraction, dialog, llDose);
        setupAutoMedCat(activity, actvMedCat, tvInteraction, llDose);
//        loadMedicationGroupsIntoSpinner(activity, spMedGroup);
        loadDoseUnitIntoSpinner(activity, spDoseUnit);
        loadIntervalUnitIntoSpinner(activity, spIntervalUnit, etTimes, tvTimes);

//        setMedCategorySelectedListener(activity, spMedCat, tvInteraction);


        setMakeOrderOkButtonListener(activity, dialog, etDose, spDoseUnit, spIntervalUnit, etTimes,
                etComment, tvEndDate, llDose);
        dialog.show();

        setAutoClickListener(actvMedGroup);
        setAutoClickListener(actvMedCat);
    }

    public static void openSuggestedEditDialog(final PatientMakeOrderActivity activity, MedicationGroupCategory mgc){
        resetVars();
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.medication_edit);
        dialog.setTitle(R.string.medication);

//        final Spinner spMedGroup = (Spinner) dialog.findViewById(R.id.spinMedGroup);
//        final Spinner spMedCat = (Spinner) dialog.findViewById(R.id.spinMedCat);

        LinearLayout llMedGroups = (LinearLayout) dialog.findViewById(R.id.llMedGroup);
        llMedGroups.setVisibility(View.VISIBLE);



        final AutoCompleteTextView actvMedGroup = (AutoCompleteTextView) dialog.findViewById(R.id.actvMedGroup);
        final AutoCompleteTextView actvMedCat = (AutoCompleteTextView) dialog.findViewById(R.id.actvMedCat);

        final EditText etDose = (EditText) dialog.findViewById(R.id.etDose);
        final Spinner spDoseUnit = (Spinner) dialog.findViewById(R.id.spinDoseUnit);

        final EditText etTimes = (EditText) dialog.findViewById(R.id.etTimes);
        final Spinner spIntervalUnit = (Spinner) dialog.findViewById(R.id.spinIntervalUnit);
        final Button bPickDate = (Button) dialog.findViewById(R.id.bPickDate);
        final TextView tvEndDate = (TextView) dialog.findViewById(R.id.tvEndDate);
        final TextView tvTimes = (TextView) dialog.findViewById(R.id.tvInterval);

        final TextView tvInteraction = (TextView) dialog.findViewById(R.id.tvInteraction);
        final EditText etComment = (EditText) dialog.findViewById(R.id.etComment);
        final LinearLayout llDose = (LinearLayout) dialog.findViewById(R.id.llDose);

        setCommonListeners(activity, bPickDate, tvEndDate, dialog);

        setupAutoMedGroup(activity, actvMedGroup, actvMedCat, tvInteraction, mgc, dialog, llDose);
//        loadMedicationGroupsIntoSpinner(activity, spMedGroup);
        loadDoseUnitIntoSpinner(activity, spDoseUnit);
        loadIntervalUnitIntoSpinner(activity, spIntervalUnit, etTimes, tvTimes);

//        setMedGroupSelectedListener(activity, spMedGroup, spMedCat);
//        setMedCategorySelectedListener(activity, spMedCat, tvInteraction);


        setMakeOrderOkButtonListener(activity, dialog, etDose, spDoseUnit, spIntervalUnit, etTimes,
                etComment, tvEndDate, llDose);
        dialog.show();
        setAutoClickListener(actvMedGroup);
        setAutoClickListener(actvMedCat);
    }

    private static void setAutoClickListener(final AutoCompleteTextView actv){
        actv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "clicked actv: " + actv.getId());
                actv.showDropDown();
            }
        });
    }


    private static void setupAutoMedGroup(final Activity activity, final AutoCompleteTextView actvMedGroup, final AutoCompleteTextView actvMedCat,
                                          final TextView tvInteraction, MedicationGroupCategory mgc, final Dialog dialog, final LinearLayout llDose) {
        final ArrayList<String> names = getMedGroupCatNames(activity);
        setAutoCompleteAdapter(activity, names, actvMedGroup);

        setValidator(activity, actvMedGroup, names, tvInteraction);

        actvMedGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvMedGroup.performValidation();
                String name = actvMedGroup.getText().toString();
                Log.e(TAG, "chose: " + name);
                if (name.length() > 0) {
                    mMedicationGroupCategory = mapNameToGroupCategory.get(name);
                    Log.e(TAG, "group cat: " + mMedicationGroupCategory.uuid);
                    setupAutoMedCat(activity, actvMedCat, tvInteraction, llDose);
                }
            }
        });

        actvMedGroup.setText(mgc.getDisplayName());
        mMedicationGroupCategory = mgc;
        LinearLayout llMedCats = (LinearLayout) dialog.findViewById(R.id.llMedCat);
        llMedCats.setVisibility(View.VISIBLE);
        setupAutoMedCat(activity, actvMedCat, tvInteraction, llDose);
    }

    private static void setupAutoMedGroup(final Activity activity, final AutoCompleteTextView actvMedGroup,
                                          final AutoCompleteTextView actvMedCat, final TextView tvInteraction,
                                          final Dialog dialog, final LinearLayout llDose) {
        final ArrayList<String> names = getMedGroupCatNames(activity);
        setAutoCompleteAdapter(activity, names, actvMedGroup);

        setValidator(activity, actvMedGroup, names, tvInteraction);

        actvMedGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvMedGroup.performValidation();
                String name = actvMedGroup.getText().toString();
                Log.e(TAG, "chose: " + name);
                mMedicationGroupCategory = mapNameToGroupCategory.get(name);
                Log.e(TAG, "group cat: " + mMedicationGroupCategory.uuid);
                LinearLayout llMedCats = (LinearLayout) dialog.findViewById(R.id.llMedCat);
                llMedCats.setVisibility(View.VISIBLE);
                setupAutoMedCat(activity, actvMedCat, tvInteraction, llDose);
            }
        });

    }

    private static void setupAutoMedCat(final Activity activity, final AutoCompleteTextView actvMedCat, final TextView tvInteraction,
                                        final LinearLayout llDose) {
        final ArrayList<String> names = getMedCatNames(activity);
        Log.e(TAG, "cat names: " + names.size());
        setAutoCompleteAdapter(activity, names, actvMedCat);
        setValidator(activity, actvMedCat, names, tvInteraction);

        Log.e(TAG, "seting cat item click listener");
        actvMedCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvMedCat.performValidation();
                String selected = actvMedCat.getText().toString();
                Log.e(TAG, "chose: " + selected);
                mMedicationCategory = mapNameToCategory.get(selected);
                Log.e(TAG, "cat: " + mMedicationCategory.getGroup_Uuid());

                if (mMedicationCategory.uuid.equals(activity.getString(R.string.insulin_med))) {
                    llDose.setVisibility(View.GONE);
                } else {
                    llDose.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private static void setValidator(final Activity activity, AutoCompleteTextView actv, final ArrayList<String> names, final TextView tvInteraction){
        Log.e(TAG, "setting validator");
        actv.setValidator(new AutoCompleteTextView.Validator() {
            @Override
            public boolean isValid(CharSequence text) {
                String sText = String.valueOf(text);
                Log.e(TAG, "validating: " + sText);
                if (names.contains(sText)) {
                    tvInteraction.setVisibility(View.INVISIBLE);
                    return true;
                }
                tvInteraction.setVisibility(View.VISIBLE);
                tvInteraction.setText(activity.getString(R.string.invalid));
                return false;
            }

            @Override
            public CharSequence fixText(CharSequence invalidText) {
                return null;
            }
        });
    }



    private static void setAutoCompleteAdapter(Activity activity, ArrayList<String> categories, AutoCompleteTextView textView){
        Log.e(TAG, "setting auto complete adapter");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                R.layout.spinner_item, categories);
        textView.setAdapter(adapter);
    }

    private static ArrayList<String> getMedGroupCatNames(Activity activity){
        Cursor cursor = activity.getContentResolver().query(MedicationGroupCategory.URI, null, null, null, MedicationGroupCategory.PRIORITY);
        mapNameToGroupCategory.clear();
        ArrayList<String> names = new ArrayList<>();
        while(cursor.moveToNext()){
            MedicationGroupCategory mgc = new MedicationGroupCategory(cursor);
            String name = mgc.getDisplayName();
            names.add(name);
            mapNameToGroupCategory.put(name, mgc);
        }
        cursor.close();
        return names;
    }

    private static ArrayList<String> getMedCatNames(Activity activity){
        Cursor cursor;
        if (mMedicationGroupCategory == null) {
            cursor = activity.getContentResolver().query(MedicationCategory.URI, null, null, null, MedicationCategory.DISPLAYNAME);
        } else {
            cursor = activity.getContentResolver().query(MedicationCategory.URI, null, MedicationCategory.GROUP_UUID + "='" + mMedicationGroupCategory.uuid + "'", null, MedicationCategory.DISPLAYNAME);
        }
        ArrayList<String> names = new ArrayList<>();
        mapNameToCategory.clear();
        while(cursor.moveToNext()){
            MedicationCategory mc = new MedicationCategory(cursor);
            String name = mc.getDisplayName();
            String other = mc.getOthername();
            Log.e(TAG, "med cat:  " + name + " " + other);
            if(name.length() > 0){
                names.add(name);
                mapNameToCategory.put(name, mc);
            }
            if(other.length() > 0){
                names.add(other);
                mapNameToCategory.put(other, mc);
            }

        }
        cursor.close();
        return names;
    }

    private static void loadMedicationGroupsIntoSpinner(Activity activity , final Spinner spMedGroup){
        Cursor mgcCursor = activity.getContentResolver().query(MedicationGroupCategory.URI, null, null, null, null);
        ArrayList<MedicationGroupCategory> mgCategories = new ArrayList<>();
        while(mgcCursor.moveToNext()){
            MedicationGroupCategory mgc = new MedicationGroupCategory(mgcCursor);
            mgCategories.add(mgc);
        }
        mgcCursor.close();
        final CategoryAdapter<MedicationGroupCategory> mgcAdapter = new CategoryAdapter<>(activity, R.layout.list_category_item, mgCategories);
        spMedGroup.setAdapter(mgcAdapter);
    }

    private static void loadMedicationGroupsIntoSpinner(Activity activity , final Spinner spMedGroup, MedicationGroupCategory currMgc){
        Cursor mgcCursor = activity.getContentResolver().query(MedicationGroupCategory.URI, null, null, null, MedicationGroupCategory.PRIORITY);
        ArrayList<MedicationGroupCategory> mgCategories = new ArrayList<>();
        String displayName = currMgc.getDisplayName();
        int currPosition = 0;
        while(mgcCursor.moveToNext()){
            MedicationGroupCategory mgc = new MedicationGroupCategory(mgcCursor);
            if(mgc.getDisplayName().equals(displayName))
                currPosition = mgCategories.size();
            mgCategories.add(mgc);
        }
        mgcCursor.close();
        final CategoryAdapter<MedicationGroupCategory> mgcAdapter = new CategoryAdapter<>(activity, R.layout.list_category_item, mgCategories);
        spMedGroup.setAdapter(mgcAdapter);
        spMedGroup.setSelection(currPosition);
    }

    private static void loadMedicationCategoriesIntoSpinner(Activity activity, final Spinner spMedCat, MedicationGroupCategory mMedicationGroup){
        Cursor mcCursor = activity.getContentResolver().query(MedicationCategory.URI, null, MedicationCategory.GROUP_UUID + "='" + mMedicationGroup.uuid + "'", null, MedicationCategory.DISPLAYNAME);
        ArrayList<MedicationCategory> mCategories = new ArrayList<>();
        while(mcCursor.moveToNext()){
            MedicationCategory mc = new MedicationCategory(mcCursor);
            mCategories.add(mc);
        }
        mcCursor.close();
        final CategoryAdapter<MedicationCategory> mcAdapter = new CategoryAdapter<>(activity, R.layout.list_category_item, mCategories);
        spMedCat.setAdapter(mcAdapter);
    }

    private static void loadDoseUnitIntoSpinner(Activity activity, final Spinner spDoseUnit){
        android.content.CursorLoader duLoader = new android.content.CursorLoader(activity, DoseUnitCategory.URI, null, null, null, DoseUnitCategory.PRIORITY);
        android.database.Cursor duCursor = duLoader.loadInBackground();
        ArrayList<DoseUnitCategory> duCategories = new ArrayList<>();
        while(duCursor.moveToNext()) {
            DoseUnitCategory duc = new DoseUnitCategory(duCursor);
            duCategories.add(duc);
        }
        duCursor.close();
        final CategoryAdapter<DoseUnitCategory> duAdapter = new CategoryAdapter<>(activity,R.layout.list_category_item,duCategories);
        spDoseUnit.setAdapter(duAdapter);
    }

    private static void loadDoseUnitIntoSpinner(Activity activity, final Spinner spDoseUnit, Medication med){
        android.content.CursorLoader duLoader = new android.content.CursorLoader(activity, DoseUnitCategory.URI, null, null, null, DoseUnitCategory.PRIORITY);
        android.database.Cursor duCursor = duLoader.loadInBackground();

        ArrayList<DoseUnitCategory> duCategories = new ArrayList<>();
        String displayName = med.getDoseUnit(activity);
        int currentDuPosition = 0;
        while(duCursor.moveToNext()) {
            DoseUnitCategory duc = new DoseUnitCategory(duCursor);
            if(duc.getDisplayName().equals(displayName))
                currentDuPosition = duCategories.size();
            duCategories.add(duc);
        }
        duCursor.close();

        final CategoryAdapter<DoseUnitCategory> duAdapter = new CategoryAdapter<>(activity,R.layout.list_category_item,duCategories);
        spDoseUnit.setAdapter(duAdapter);
        spDoseUnit.setSelection(currentDuPosition);
    }

    private static void loadIntervalUnitIntoSpinner(final Activity activity, final Spinner spIntervalUnit, final EditText etTimes, final TextView tvTimes) {
        android.content.CursorLoader iuLoader = new android.content.CursorLoader(activity, IntervalUnitCategory.URI, null, null, null, IntervalUnitCategory.PRIORITY);
        android.database.Cursor iuCursor = iuLoader.loadInBackground();

        ArrayList<IntervalUnitCategory> iuCategories = new ArrayList<>();
        while(iuCursor.moveToNext()) {
            IntervalUnitCategory iuc = new IntervalUnitCategory(iuCursor);
            iuCategories.add(iuc);
        }
        iuCursor.close();
        final CategoryAdapter<IntervalUnitCategory> iuAdapter = new CategoryAdapter<>(activity,R.layout.list_category_item,iuCategories);
        spIntervalUnit.setAdapter(iuAdapter);

        spIntervalUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                IntervalUnitCategory sel = (IntervalUnitCategory) adapterView.getSelectedItem();
                if (sel.uuid.equals(activity.getString(R.string.asneeded_interval))) {
                    etTimes.setVisibility(View.GONE);
                    tvTimes.setVisibility(View.GONE);
                } else {
                    etTimes.setVisibility(View.VISIBLE);
                    tvTimes.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private static void loadIntervalUnitIntoSpinner(final Activity activity, final Spinner spIntervalUnit, Medication med, final TextView tvTimes, final EditText etTimes) {
        android.content.CursorLoader iuLoader = new android.content.CursorLoader(activity, IntervalUnitCategory.URI, null, null, null, Category.PRIORITY);
        android.database.Cursor iuCursor = iuLoader.loadInBackground();

        ArrayList<IntervalUnitCategory> iuCategories = new ArrayList<>();
        String iDisplayName = med.getIntervalUnit(activity);
        int currentIuPosition = 0;
        while(iuCursor.moveToNext()) {
            IntervalUnitCategory iuc = new IntervalUnitCategory(iuCursor);
            if(iuc.getDisplayName().equals(iDisplayName))
                currentIuPosition = iuCategories.size();
            iuCategories.add(iuc);
        }

        iuCursor.close();

        final CategoryAdapter<IntervalUnitCategory> iuAdapter = new CategoryAdapter<>(activity,R.layout.list_category_item,iuCategories);
        spIntervalUnit.setAdapter(iuAdapter);
        spIntervalUnit.setSelection(currentIuPosition);

        spIntervalUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                IntervalUnitCategory sel = (IntervalUnitCategory) adapterView.getSelectedItem();
                if (sel.uuid.equals(activity.getString(R.string.asneeded_interval))) {
                    etTimes.setVisibility(View.GONE);
                    tvTimes.setVisibility(View.GONE);
                } else {
                    etTimes.setVisibility(View.VISIBLE);
                    tvTimes.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private static void setMedCategorySelectedListener(final Activity activity, final Spinner spMedCat, final TextView tvInteraction) {
        spMedCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MedicationCategory mgc = (MedicationCategory) spMedCat.getItemAtPosition(position);
                if (mgc.getInteractionWarning() != null && !mgc.getInteractionWarning().equals("null")) {
                    tvInteraction.setVisibility(View.VISIBLE);
                    tvInteraction.setText(mgc.getInteractionWarning());
                } else {
                    tvInteraction.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private static void setMedGroupSelectedListener(final Activity activity, final Spinner spMedGroup, final Spinner spMedCat){
        spMedGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MedicationGroupCategory mgc = (MedicationGroupCategory) spMedGroup.getItemAtPosition(position);
                loadMedicationCategoriesIntoSpinner(activity, spMedCat, mgc);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private static void setPickDateListener(final Activity activity, Button bPickDate, final TextView tvEndDate){
        final Calendar cal = Calendar.getInstance();
        bPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear += 1;
                        tvEndDate.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dpDialog.show();
            }
        });
    }

    private static void setCancelButtonListener(final Dialog dialog){
        Button bCancel = (Button) dialog.findViewById(R.id.bCancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private static void setMakeOrderOkButtonListener(final PatientMakeOrderActivity activity, final Dialog dialog, final EditText etDose,
                                                     final Spinner spDoseUnit, final Spinner spIntervalUnit, final EditText etTimes,
                                                     final EditText etComment, final TextView tvEndDate, final LinearLayout llDose) {
        Button bOk = (Button) dialog.findViewById(R.id.bOk);
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dose = etDose.getText().toString();
                String doseUnit = ((DoseUnitCategory)spDoseUnit.getSelectedItem()).uuid;
                String intervalUnit = ((IntervalUnitCategory)spIntervalUnit.getSelectedItem()).uuid;
                String times =  etTimes.getText().toString();
                if(mMedicationCategory != null){
                    String medCat = mMedicationCategory.uuid;
                    String comment = etComment.getText().toString();

                    Medication medication = new Medication("","","","");

                    if ((!dose.isEmpty() && !times.isEmpty()) || (etTimes.getVisibility() != View.VISIBLE)) {
                        medication.setDose(dose);
                        medication.setDoseUnit(doseUnit);
                        medication.setIntervalUnit(intervalUnit);
                        medication.setTimes(times);
                        medication.setMedicationCategory(medCat);
                        medication.setComment(comment);

                        String endDate = tvEndDate.getText().toString();
                        if (!endDate.equals(activity.getString(R.string.never)))
                            medication.setEndDate(endDate);

                        activity.addMedication(medication);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.must_set_dose), Toast.LENGTH_SHORT).show();
                        return;
                    }

                }else{
                    Toast.makeText(activity,activity.getString(R.string.must_chose_med_cat),Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialog.show();
    }

    private static void setActiveOkButtonListener(final Activity activity, final Dialog dialog, final EditText etDose,
                                                  final EditText etTimes, final Spinner spDoseUnit,
                                                  final Spinner spIntervalUnit, final TextView tvEndDate,
                                                  final EditText etComment, final ActiveMedListAdapter adapter,
                                                  final Medication med, final LinearLayout llDose) {
        Button bOk = (Button) dialog.findViewById(R.id.bOk);
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dose = etDose.getText().toString();
                String times = etTimes.getText().toString();

                String doseUnit = ((DoseUnitCategory) spDoseUnit.getSelectedItem()).uuid;
                med.setDoseUnit(doseUnit);

                String intervalUnit = ((IntervalUnitCategory) spIntervalUnit.getSelectedItem()).uuid;

                String endDate = tvEndDate.getText().toString();
                if (!endDate.equals(activity.getString(R.string.never)))
                    med.setEndDate(endDate);

                String comment = etComment.getText().toString();

                if ((!dose.isEmpty() && !times.isEmpty()) || (etTimes.getVisibility() != View.VISIBLE)) {
                    med.setDose(dose);
                    med.setTimes(times);
                    med.setComment(comment);
                    int uri_med = activity.getContentResolver().update(Medication.URI, med.putContentValues(), "UUID = ?", new String[]{med.uuid});

                    adapter.notifyDataSetChanged();
//                PatientProfileInfoFragment.ActiveMedListAdapter activeMedListAdapter = new PatientProfileInfoFragment.ActiveMedListAdapter(activity, activeMeds);
//                lvActiveMeds.setAdapter(activeMedListAdapter);
                    dialog.dismiss();
                } else {
                    Toast.makeText(activity, activity.getString(R.string.must_set_dose), Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private static void setStopMedClickListener(final Activity activity, final Dialog dialog, final Button bStopMed, final Medication med, final ArrayList<Medication> activeMeds, final ActiveMedListAdapter adapter){
        bStopMed.setVisibility(View.VISIBLE);

        bStopMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(activity.getString(R.string.stop_medication));
                builder.setMessage(med.getName(activity));
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH) + 1;
                        int day = cal.get(Calendar.DAY_OF_MONTH);
                        med.setEndDate(year + "-" + month + "-" + day);
                        med.markForUpdate();

                        int uri_med = activity.getContentResolver().update(Medication.URI, med.putContentValues(), "uuid = \'" + med.uuid + "\'", null);


                        activeMeds.remove(med);

                        adapter.notifyDataSetChanged();
//                        PatientProfileInfoFragment.ActiveMedListAdapter activeMedListAdapter = new PatientProfileInfoFragment.ActiveMedListAdapter(activity, activeMeds);
//                        lvActiveMeds.setAdapter(activeMedListAdapter);
                        dialogInterface.dismiss();
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });
    }

    private static void setCommonListeners(final Activity activity, final Button bPickDate, final TextView tvEndDate, final Dialog dialog){
        setPickDateListener(activity, bPickDate, tvEndDate);
        setCancelButtonListener(dialog);
    }

    private static void resetVars(){
        mMedicationGroupCategory = null;
        mMedicationCategory= null;
    }
}
