package com.smarking.mhealthsyria.app.print;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.view.viewmodel.EncounterViewModel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by tamhok on 1/28/16.
 */
public class LifestyleForm extends Form{
    private static final String TAG = LifestyleForm.class.getSimpleName();
    private static final int MAX_ROWS = 40;
    LinearLayout mainLayout;
    ImageView qrCode;
    TextView tvDate;
    TextView tvDoctor;
    TextView tvPatientName;
    TextView tvMedication;
    TextView tvClinic;
    TextView tvNextVisitValue;
    TextView tvRecords;

    LinearLayout llAlcohol;
    LinearLayout llSmoking;
    LinearLayout llDiet;
    LinearLayout llExercise;

    ImageView ivDiet;
    ImageView ivSmoking;
    ImageView ivExercise;
    ImageView ivAlcohol;

    TextView tvDiet;
    TextView tvSmoking;
    TextView tvExercise;
    TextView tvAlcohol;

    Encounter mEncounter;
    Patient mPatient;
    List<Test> tests;
    List<Record> records;
    List<Medication> meds;

    public LifestyleForm(Encounter mEncounter, List<Test> tests, List<Record> records, List<Medication> meds, Context c, Category.Language lang) {
        this.mEncounter = mEncounter;
        this.mPatient = Patient.getPatient(c, Visit.get(c, mEncounter.visit_uuid).getPatient_uuid());
        this.tests = tests;
        this.records = records;
        this.meds = meds;
        this.lang = lang;
    }

    public LinearLayout setUpLifestyleForm(Context context) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mainLayout = new LinearLayout(context);
        mainLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        mInflater.inflate(R.layout.print_lifestyle, mainLayout, true);
        try {
            byte[] uuid = new BigInteger(mPatient.uuid, 16).toByteArray();
            qrCode = setQRImage(R.id.qrCode, mainLayout, uuid);
        } catch (Exception e){
            Log.e(this.getClass().getSimpleName(), " ZXING ERRROR " + e.getMessage());
            e.printStackTrace();
        }


        tvNextVisitValue = (TextView) mainLayout.findViewById(R.id.tvNextVisitValue);
        tvRecords = (TextView) mainLayout.findViewById(R.id.tvRecords);
        llAlcohol = (LinearLayout) mainLayout.findViewById(R.id.lifeStyleAlcohol);
        llDiet = (LinearLayout) mainLayout.findViewById(R.id.lifestyleDiet);
        llExercise = (LinearLayout) mainLayout.findViewById(R.id.lifeStyleExercise);
        llSmoking = (LinearLayout) mainLayout.findViewById(R.id.lifeStyleSmoking);

        ivAlcohol = (ImageView) mainLayout.findViewById(R.id.imgAlcoholUse);
        ivDiet = (ImageView) mainLayout.findViewById(R.id.imgHealthyDiet);
        ivExercise = (ImageView) mainLayout.findViewById(R.id.imgPhysicalActivity);
        ivSmoking = (ImageView) mainLayout.findViewById(R.id.imgTobaccoUse);

        tvMedication = (TextView) mainLayout.findViewById(R.id.tvMedication);
        tvAlcohol = (TextView) mainLayout.findViewById(R.id.tvTPAlcoholUse);
        tvDiet = (TextView) mainLayout.findViewById(R.id.tvTPHealthyDiet);
        tvExercise = (TextView) mainLayout.findViewById(R.id.tvTPPhysicalActivity);
        tvSmoking = (TextView) mainLayout.findViewById(R.id.tvTPTobaccoUse);

        // Set the text directionality
        setTextDirectionality(lang);

        // unicode wrapped version
        tvDate = setText(R.id.tvDate, mainLayout, "%s: %s", context.getString(R.string.date),
                Model.LEAST_DATE.format(Calendar.getInstance().getTime()));
        tvPatientName = setText(R.id.tvPatientName, mainLayout, "%s: %s",
                context.getString(R.string.name), mPatient.getFullName());
        tvDoctor = setText(R.id.tvDoctor, mainLayout,"%s: %s",
                context.getString(R.string.doctor_name),
                Physician.get(context, mEncounter.physician_uuid).getFullName());
        tvClinic = setText(R.id.tvClinic, mainLayout, "%s: %s",
                context.getString(R.string.clinic),
                Clinic.get(context, mEncounter.clinic_uuid).name);

        if (meds != null) {
            String display = "";
            StringBuilder displayBuilder = getStringBuilder();
            for (Medication med : meds) {
                displayBuilder.append(String.format("%n%s: %s",
                        unicodeWrap(med.getMedicationName(context)),
                        unicodeWrap(med.toString(context))));
            }
            setText(tvMedication, displayBuilder.toString());
        }


        if (records != null) {
            String display = "";
            StringBuilder displayBuilder = getStringBuilder();
            List<EncounterViewModel> encs = new ArrayList<>(records.size());

            List<String> bannedUUIDs = new ArrayList<>(5);
            bannedUUIDs.add(Recommender.getInstance().getUUID(Recommender.recordUUIDLookup, R.integer.alcoholmod));
            bannedUUIDs.add(Recommender.getInstance().getUUID(Recommender.recordUUIDLookup, R.integer.dietmod));
            bannedUUIDs.add(Recommender.getInstance().getUUID(Recommender.recordUUIDLookup, R.integer.exercisemod));
            bannedUUIDs.add(Recommender.getInstance().getUUID(Recommender.recordUUIDLookup, R.integer.quitsmoke));
            String followup = Recommender.getInstance().getUUID(Recommender.recordUUIDLookup, R.integer.followup);

            String recordgroup;

            for (Record record : records) {
                if (record.getCategory_uuid().equals(followup)) {
                    setText(tvNextVisitValue, unicodeWrap(record.getValue()));
                } else if (!bannedUUIDs.contains(record.getCategory_uuid())) {
                    String category = "";
                    String valueResult = "";
                    String comment = "";
                    EncounterViewModel res = new EncounterViewModel(context, mEncounter, record);
                    // TODO is this logic correct?
                    if (lang == Category.Language.ENGLISH) {
                        if (Model.blankIfNull(record.getValueAr()).equals("")) {
                            category = Model.blankIfNull(res.category);
                            valueResult = Model.blankIfNull(res.valueResult);
                            comment = Model.blankIfNull(res.comment);
                        } else {
                            category = Model.blankIfNull(res.category);
                            valueResult = Model.blankIfNull(record.getValueAr());
                            comment = Model.blankIfNull(record.getCommentAr());
                        }

                    } else {
                        category = Model.blankIfNull(res.category);
                        valueResult = Model.blankIfNull(res.valueResult);
                        comment = Model.blankIfNull(res.comment);
                    }

                    displayBuilder.append(String.format("%n%s: %s   %s",
                            unicodeWrap(category), unicodeWrap(valueResult),
                            unicodeWrap(comment)));
                }
            }
            tvRecords.setText(displayBuilder.toString());
        }

        if (tests != null) {

            String alcohol = Recommender.getInstance().getUUID(Recommender.testUUIDLookup, R.integer.Alcoholconsumption);
            String diet = Recommender.getInstance().getUUID(Recommender.testUUIDLookup, R.integer.Vegetableconsumption);
            String smoking = Recommender.getInstance().getUUID(Recommender.testUUIDLookup, R.integer.Smoking);
            String exercise = Recommender.getInstance().getUUID(Recommender.testUUIDLookup, R.integer.Exercise30minsday);

            TypedArray imgDiet = context.getResources().obtainTypedArray(R.array.img_diet);
            TypedArray imgAlchohol = context.getResources().obtainTypedArray(R.array.img_alcohol);
            TypedArray imgSmoking = context.getResources().obtainTypedArray(R.array.img_smoking);
            TypedArray imgExercise = context.getResources().obtainTypedArray(R.array.img_exercise);

            for (Test test : tests) {
                if (test.getCategory_uuid().equals(diet)) {

                    String[] vals = context.getResources().getStringArray(R.array.tp_healthy_diet);
                    try {
                        int lvl = Integer.parseInt(test.getResult());
                        setText(tvDiet, vals[lvl]);
                        int resId = imgDiet.getResourceId(lvl, 0);
                        loadImageToView(context, ivDiet, resId);
                        //ivDiet.setImageResource(imgDiet.getResourceId(lvl, 0));
                        Log.e(TAG, "Good Value, diet: " + lvl + " " + vals[0]);
                        llDiet.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "Bad value, diet: " + test.getResult());
                    }

                } else if (test.getCategory_uuid().equals(alcohol)) {
                    String[] vals = context.getResources().getStringArray(R.array.tp_alcohol_use);
                    try {
                        int lvl = Integer.parseInt(test.getResult());
                        setText(tvAlcohol, vals[lvl]);
                        int resId = imgAlchohol.getResourceId(lvl, 0);
                        loadImageToView(context, ivAlcohol, resId);
                        //ivAlcohol.setImageResource(imgAlchohol.getResourceId(lvl, 0));
                        Log.e(TAG, "Good Value, alc: " + lvl + " " + vals[0]);
                        llAlcohol.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "Bad value, alcohol: " + test.getResult() + " " + e.getMessage());
                    }

                } else if (test.getCategory_uuid().equals(smoking)) {
                    String[] vals = context.getResources().getStringArray(R.array.tp_tobacco_use);
                    try {
                        int lvl = Integer.parseInt(test.getResult());
                        setText(tvSmoking, vals[lvl]);
                        int resId = imgSmoking.getResourceId(lvl, 0);
                        loadImageToView(context,ivSmoking,resId);
                        //ivSmoking.setImageResource(imgSmoking.getResourceId(lvl, 0));
                        Log.e(TAG, "Good Value, smok: " + lvl + " " + vals[0]);
                        llSmoking.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "Bad value, smok: " + test.getResult());
                    }

                } else if (test.getCategory_uuid().equals(exercise)) {
                    String[] vals = context.getResources().getStringArray(R.array.tp_physical_activity);
                    try {
                        int lvl = Integer.parseInt(test.getResult());
                        setText(tvExercise, vals[lvl]);
                        int resId = imgExercise.getResourceId(lvl, 0);
                        loadImageToView(context,ivExercise,resId);
                        //ivExercise.setImageResource(imgExercise.getResourceId(lvl, 0));
                        Log.e(TAG, "Good Value: exer" + lvl + " " + vals[0]);
                        llExercise.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "Bad value, exer: " + test.getResult());
                    }

                }

            }
        }

        // resize fonts
        List<TextView> adjustViews = new ArrayList<>();
        adjustViews.add(tvDiet);
        adjustViews.add(tvSmoking);
        adjustViews.add(tvExercise);
        adjustViews.add(tvAlcohol);
        adjustViews.add(tvMedication);
        // Get the fixed heights of layouts that we do not need to shrink
        View infoView = mainLayout.findViewById(R.id.infoLLayout);
        View nextVisitView = mainLayout.findViewById(R.id.layoutVisit);

        // Calculate the height we need to shrink the dynamic content into
        int height = A4.HEIGHT - infoView.getHeight() - nextVisitView.getHeight();

        // Adjust the font size in the dynamic content so that we fit onto an A4
        View adjustLayout = mainLayout.findViewById(R.id.layoutDynamicContent);
        ViewFontResizer.shrinkFont(adjustLayout, height, adjustViews);
        return mainLayout;
    }
}
