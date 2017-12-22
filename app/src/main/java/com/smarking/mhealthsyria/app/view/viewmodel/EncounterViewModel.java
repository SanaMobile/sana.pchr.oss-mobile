package com.smarking.mhealthsyria.app.view.viewmodel;

import android.content.Context;
import android.util.Log;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.category.TestCategory;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-25.
 */
public class EncounterViewModel implements Comparable<EncounterViewModel> {

    public enum TYPE {
        TEST, RECORD
    }
    public final String uuid;
    public final String physician;
    public final String clinic;
//    public final String device;
    public final String valueResult;
    public final String category;
    public final String type;
    public final String comment;
    public final int level;  //For setting color of tests.
    public final TYPE encounterType;
    public final int priority;

    public final Date lastModified;

    public EncounterViewModel(Context context, Encounter encounter, Record record){
        String name;
        try{
            name=  Physician.get(context, encounter.physician_uuid).getFullName();
        }catch (Exception e){
            name = "";
        }
        physician = name;

        clinic = Clinic.get(context, encounter.clinic_uuid).name;
//        device = Device.get(context, encounter.device_uuid).name; Log.e("device", device);
        valueResult = record.getValue();
        category = RecordCategory.get(context, record.getCategory_uuid()).getDisplayName();
        type = "R";
        comment = record.getComment();
        encounterType = TYPE.RECORD;
        level = -1;

        Date date;
        try {
            date = Model.ISO8601_FORMAT.parse(record.getCreated());
        } catch (ParseException e) {
            Log.e("EncounterViewModel", "last Modified Parse Error");
            date = new Date();
        }

        lastModified = date;
        priority = 1000;
        uuid = encounter.uuid;
    }

    public EncounterViewModel(Context context, Encounter encounter, Medication med) {
        String name;
        try{
            name=  Physician.get(context, encounter.physician_uuid).getFullName();
        }catch (Exception e){
            name = "";
        }
        physician = name;
        clinic = Clinic.get(context, encounter.clinic_uuid).name;
//        device = Device.get(context, encounter.device_uuid).name; Log.e("device", device);
        valueResult = med.toString(context);
        category = med.getName(context);
        comment = "";
        type = "R";
        encounterType = TYPE.RECORD;
        level = -1;
        priority = 99;
        Date date;
        try {
            date = Model.ISO8601_FORMAT.parse(med.getCreated());
        } catch (ParseException e) {
            Log.e("EncounterViewModel", "last Modified Parse Error");
            date = new Date();
        }

        lastModified = date;
        uuid = encounter.uuid;
    }

    public EncounterViewModel(Context context, Encounter encounter, Test test){
        String name;
        try{
            name=  Physician.get(context, encounter.physician_uuid).getFullName();
        }catch (Exception e){
            name = "";
        }
        physician = name;
        clinic = Clinic.get(context, encounter.clinic_uuid).name;
//        device = Device.get(context, encounter.device_uuid).name;

        TestCategory cat = TestCategory.get(context, test.getCategory_uuid());

        if (cat.getResultType() == 2) {
            if (test.getResult().equals("0")) {
                valueResult = context.getString(R.string.no);
            } else {
                valueResult = context.getString(R.string.yes);
            }
        } else if (cat.getResultType() == 3) {
            String[] parts = cat.getResultUnits().split("\\|");
            String res;
            try {
                res = parts[Integer.parseInt(test.getResult())];
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Invalid value: " + cat.getDisplayName() + " " + test.getResult());
                res = test.getResult();
            }
            valueResult = res;
//        }else if(cat.getResultType() == 3){
//            valueResult = test.getResult();
        } else {
            valueResult = test.getResult() + " " + cat.getResultUnits();
        }
        category = cat.getDisplayName();
        int prepriority = cat.getPriority();
        if (prepriority >= 601 && prepriority <= 604) {
            prepriority = prepriority - 560;
        } else if (prepriority <= 614 && prepriority >= 611) {
            prepriority = prepriority - 580;
        } else if (prepriority >= 411 && prepriority < 414) {
            prepriority = prepriority - 390;
        } //BMI
        else if (prepriority == 403) {
            prepriority = 90;
        }
        priority = prepriority;


        type = "T";
        comment = "";
        encounterType = TYPE.RECORD;
        level = Recommender.checkTestLevel(test);
        Date date;
        try {
            date = Model.ISO8601_FORMAT.parse(test.getCreated());
        } catch (ParseException e) {
            Log.e("EncounterViewModel", "last Modified Parse Error");
            date = new Date();
        }

        lastModified = date;
        uuid = encounter.uuid;
    }

    @Override
    public int compareTo(EncounterViewModel another) {
        return priority > another.priority ? 1 : -1;
    }

}
