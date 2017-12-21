package com.smarking.mhealthsyria.app.print;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.Reporter;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.TestCategory;
import com.smarking.mhealthsyria.app.model.event.Event;
import com.smarking.mhealthsyria.app.view.viewmodel.EncounterViewModel;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by tamhok on 1/28/16.
 */
public class DataForm extends Form{
    private static final String TAG = DataForm.class.getSimpleName();
    private static final int MAX_ROWS = 45;
    LinearLayout mainLayout;
    ImageView qrCode;
    TextView tvDate;
    TextView tvPatientNum;
    TextView tvDoctor;
    TextView tvPatientName;
    TextView tvMedication;
    TextView tvTests;
    TextView tvRecords;
    TextView tvTests2;
    TextView tvClinic;

    Encounter mEncounter;
    Patient mPatient;
    List<Test> tests;
    List<Record> records;
    List<Medication> meds;

    public DataForm(Encounter mEncounter, List<Test> tests, List<Record> records, List<Medication> meds, Context c) {
        this.mEncounter = mEncounter;
        this.mPatient = Patient.getPatient(c, Visit.get(c, mEncounter.visit_uuid).getPatient_uuid());
        this.tests = tests;
        this.records = records;
        this.meds = meds;
    }

    public LinearLayout setUpDataForm(Context context) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mainLayout = new LinearLayout(context);
        mInflater.inflate(R.layout.print_layout, mainLayout, true);
        try {
            byte[] uuid = new BigInteger(mPatient.uuid, 16).toByteArray();
            qrCode = setQRImage(R.id.qrCode, mainLayout, uuid);
        } catch (Exception e){
            Reporter.err(context, Event.Code.RUNTIME, e, TAG);
            Log.e(this.getClass().getSimpleName(), " ZXING ERRROR " + e);
            e.printStackTrace();
        }
        // TextViews we will not super getText method on
        tvTests = (TextView) mainLayout.findViewById(R.id.tvTests);
        tvRecords = (TextView) mainLayout.findViewById(R.id.tvRecords);
        tvMedication = (TextView) mainLayout.findViewById(R.id.tvMedication);
        tvTests2 = (TextView) mainLayout.findViewById(R.id.tvTests2);
        // Unicode wrapped version
        tvDate = setText(R.id.tvDate, mainLayout, "%s: %s",
                context.getString(R.string.date), getEncounterDate(context));
        tvPatientName = setText(R.id.tvPatientName,mainLayout,"%s: %s",
                context.getString(R.string.name), mPatient.getFullName());
        tvPatientNum = setText(R.id.tvPatientNum, mainLayout,"%s: %s",
                context.getString(R.string.provider_id), mPatient.getProviderID() );
        tvDoctor = setText(R.id.tvDoctor, mainLayout,"%s: %s",
                context.getString(R.string.doctor_name),
                Physician.get(context, mEncounter.physician_uuid).getFullName());
        tvClinic = setText(R.id.tvClinic, mainLayout,"%s: %s", context.getString(R.string.clinic),
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

        if (tests != null) {
            String[] titles = context.getResources().getStringArray(R.array.test_cat_dividers);
            StringBuilder displayBuilder = getStringBuilder();
            StringBuilder secondDisplayBuilder = getStringBuilder();
            Collections.sort(tests);

            int new_priority;
            int prev_priority = 0;
            int ctr = 0;
            for (Test test : tests) {
                TestCategory curCat = TestCategory.get(context, test.getCategory_uuid());
                new_priority = curCat.getPriority() / 100;
                if (ctr < MAX_ROWS) {
                    if (new_priority > prev_priority) {
                        displayBuilder.append(String.format("%n%n%s",
                                unicodeWrap(titles[new_priority - 1])));
                        ctr = ctr + 2;
                    }
                    EncounterViewModel enc = new EncounterViewModel(context, mEncounter, test);
                    displayBuilder.append(String.format("%n  %s: %s",
                            unicodeWrap(enc.category), unicodeWrap(enc.valueResult)));
                } else {
                    if (new_priority > prev_priority) {
                        secondDisplayBuilder.append(String.format("%n%n%s",
                                unicodeWrap(titles[new_priority - 1])));
                        ctr = ctr + 2;
                    }
                    EncounterViewModel enc = new EncounterViewModel(context, mEncounter, test);
                    secondDisplayBuilder.append(String.format("%n  %s: %s",
                            unicodeWrap(enc.category),
                            unicodeWrap(enc.valueResult)));
                }
                prev_priority = new_priority;
                ctr++;
            }
            setText(tvTests,displayBuilder.toString());
            if (ctr > MAX_ROWS) {
                setText(tvTests2, secondDisplayBuilder.toString());
                tvTests2.setVisibility(View.VISIBLE);
            }
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
            bannedUUIDs.add(Recommender.getInstance().getUUID(Recommender.recordUUIDLookup, R.integer.followup));
            for (Record record : records) {
                if (!bannedUUIDs.contains(record.getCategory_uuid())) {
                    EncounterViewModel res = new EncounterViewModel(context, mEncounter, record);
                    displayBuilder.append(String.format("%n%s: %s   %s",
                            unicodeWrap(Model.blankIfNull(res.category)),
                            unicodeWrap(Model.blankIfNull(res.valueResult)),
                            unicodeWrap(Model.blankIfNull(res.comment))));

                }
            }
            setText(tvRecords, displayBuilder.toString());
        }
        // resize fonts
        List<TextView> adjustViews = new ArrayList<>();
        adjustViews.add(tvMedication);
        adjustViews.add(tvTests);
        adjustViews.add(tvRecords);
        adjustViews.add(tvTests2);

        // Get the fixed heights of layouts that we do not need to shrink
        View infoView = mainLayout.findViewById(R.id.infoLLayout);
        // Calculate the height we need to shrink the dynamic content into
        int height = A4.HEIGHT - infoView.getHeight();

        // Adjust the font size in the dynamic content so that we fit onto an A4
        View adjustLayout = mainLayout.findViewById(R.id.layoutDynamicContent);
        ViewFontResizer.shrinkFont(adjustLayout, height, adjustViews);
        return mainLayout;
    }

    public String getEncounterDate(Context context){
        String defaultDateStr = Model.LEAST_DATE.format(Calendar.getInstance().getTime());
        String encounterDateStr = defaultDateStr;
        // TODO Get the date from the db. Remove try/catch if we are certain it will be fetched
        try {
            // TODO Set encounterDateStr
            // TODO Is this fetched by record uuid? If so which?
        } catch (Exception e){

        }
        return encounterDateStr;
    }
}
