package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.category.DoseUnitCategory;
import com.smarking.mhealthsyria.app.model.category.IntervalUnitCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.MalformedInputException;

/**
 * Created by abe707 on 1/10/16.
 */
@SuppressWarnings("SizeReplaceableByIsEmpty")
public class Medication extends Model  {
    public static String TABLE = "Medication";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static final String CATEGORY_UUID = "category_uuid";
    public static final String ENCOUNTER_UUID = "encounter_uuid";
    public static final String DOSE = "dose";
    public static final String INTERVAL = "interval";
    public static final String TIMES = "times";
    public static final String DOSE_UNIT = "dose_unit_uuid";
    public static final String INTERVAL_UNIT = "interval_unit_uuid";
    public static final String END_DATE = "end_date";
    public static final String COMMENT = "comment";

    private String mDose;
    private String mInterval;
    private String mTimes;
    private String mDoseUnit;
    private String mIntervalUnit;
    private String mEndDate;
    private String mEncounterUUID;
    private String mCategory;
    private String mComment;

    public Medication(MedicationCategory category, String endDate, String encounterUUID, String comment) {
        super("", "", "", "");
        mDose = category.getDose_Default();
        mInterval = category.getInterval_Default();
        mTimes = category.getTimes_Default();
        mDoseUnit = category.getDose_Unit_Uuid();
        mIntervalUnit = category.getInterval_Unit_Uuid();
        mEndDate = endDate;
        mEncounterUUID = encounterUUID;
        mCategory = category.getUUID();
        mComment = comment;
    }

    public Medication(String uuid, String created, String updated, String synchronized_) {
        super(uuid, created, updated, synchronized_);
        this.mDose = "";
        this.mInterval = "";
        this.mDoseUnit = "";
        this.mIntervalUnit = "";
        this.mTimes = "";
        this.mEndDate = "";
        this.mEncounterUUID="";
        this.mCategory = "";
        this.mComment = "";
    }

    public Medication(Parcel in) {
        super(in);
        this.mDose = in.readString();
        this.mInterval = in.readString();
        this.mDoseUnit = in.readString();
        this.mIntervalUnit = in.readString();
        this.mTimes = in.readString();
        this.mEndDate = in.readString();
        this.mEncounterUUID = in.readString();
        this.mCategory = in.readString();
        this.mComment = in.readString();
    }

    public Medication(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.mDose = jsonObject.getString(Medication.DOSE);
        this.mInterval = jsonObject.getString(Medication.INTERVAL);
        this.mDoseUnit = jsonObject.getString(Medication.DOSE_UNIT);
        this.mIntervalUnit = jsonObject.getString(Medication.INTERVAL_UNIT);
        this.mTimes = jsonObject.getString(Medication.TIMES);
        this.mEndDate = jsonObject.getString(Medication.END_DATE);
        this.mEncounterUUID = jsonObject.getString(Medication.ENCOUNTER_UUID);
        this.mCategory = jsonObject.getString(Medication.CATEGORY_UUID);
        this.mComment = jsonObject.getString(Medication.COMMENT);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(mDose);
        parcel.writeString(mInterval);
        parcel.writeString(mDoseUnit);
        parcel.writeString(mIntervalUnit);
        parcel.writeString(mTimes);
        parcel.writeString(mEndDate);
        parcel.writeString(mEncounterUUID);
        parcel.writeString(mCategory);
        parcel.writeString(mComment);
    }

    public static final Parcelable.Creator<Medication> CREATOR = new Parcelable.Creator<Medication>() {
        public Medication createFromParcel(Parcel in) {
            return new Medication(in);
        }

        public Medication[] newArray(int size) {
            return new Medication[size];
        }
    };

    public Medication(Cursor c) {
        super(c);
        this.mDose = getString(c, DOSE);
        this.mDoseUnit = getString(c, DOSE_UNIT);
        this.mInterval = getString(c, INTERVAL);
        this.mIntervalUnit = getString(c, INTERVAL_UNIT);
        this.mTimes = getString(c, TIMES);
        this.mEndDate = getString(c, END_DATE);
        this.mEncounterUUID = getString(c, ENCOUNTER_UUID);
        this.mCategory = getString(c, CATEGORY_UUID);
        this.mComment = getString(c, COMMENT);
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for(int i = 0; i<length; i++){
            Medication medication = new Medication(jsonArray.getJSONObject(i));
            contentValueses[i] = medication.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    public String getName(Context context){
        String whereClause = " %s = '%s' ";
        Cursor catCursor = context.getContentResolver().query(MedicationCategory.URI, null, String.format(whereClause, UUID, this.mCategory),null,null);
        if(catCursor.moveToNext()) {
            MedicationCategory mc = new MedicationCategory(catCursor);
            catCursor.close();
            return mc.getDisplayName();
        }
        catCursor.close();
        return this.mCategory;
    }

    public MedicationCategory getMedication(Context context) {
        String whereClause = " %s = '%s' ";
        Cursor duCursor = context.getContentResolver().query(MedicationCategory.URI, null, String.format(whereClause, UUID, this.mCategory), null, null);
        if (duCursor.moveToNext()) {
            MedicationCategory medc = new MedicationCategory(duCursor);
            duCursor.close();
            return medc;
        }
        duCursor.close();
        return null;
    }

    public String getMedicationName(Context context) {
        String whereClause = " %s = '%s' ";
        Cursor duCursor = context.getContentResolver().query(MedicationCategory.URI, null, String.format(whereClause, UUID, this.mCategory), null, null);
        if (duCursor.moveToNext()) {
            MedicationCategory medc = new MedicationCategory(duCursor);
            duCursor.close();
            return medc.getDisplayName();
        }
        duCursor.close();
        return this.mCategory;
    }

    public String getComment() {
        return Model.blankIfNull(mComment);
    }

    public void setComment(String mComment) {
        this.mComment = mComment;
    }

    public String getEndDate(){return this.mEndDate;}

    public String getDose(){ return this.mDose;}

    public String getDoseUnit(Context context){
        String whereClause = " %s = '%s' ";
        Cursor duCursor = context.getContentResolver().query(DoseUnitCategory.URI, null, String.format(whereClause, UUID, this.mDoseUnit),null,null);
        if(duCursor.moveToNext()) {
            DoseUnitCategory duc = new DoseUnitCategory(duCursor);
            duCursor.close();
            return duc.getDisplayName();
        }
        duCursor.close();
        return this.mDoseUnit;
    }

    public String getInterval(){return this.mInterval;}

    public String getIntervalUnit(Context context){
        String whereClause = " %s = '%s' ";
        Cursor iuCursor = context.getContentResolver().query(IntervalUnitCategory.URI, null, String.format(whereClause, UUID, this.mIntervalUnit),null,null);
        if(iuCursor.moveToNext()) {
            IntervalUnitCategory iuc = new IntervalUnitCategory(iuCursor);
            iuCursor.close();
            return iuc.getDisplayName();
        }
        iuCursor.close();
        return this.mIntervalUnit;
    }

    public String getDoseWithUnit(Context context){
        return String.format("%s %s", mDose, getDoseUnit(context));
    }

    public String getIntervalWithUnit(Context context){
        return String.format("%s %s", mInterval, getIntervalUnit(context));
    }
    public String getEncounterUuid(){return this.mEncounterUUID;}

    public void setDose(String dose){this.mDose = dose;}
    public void setDoseUnit(String unit){this.mDoseUnit = unit;}
    public void setInterval(String inter){this.mInterval = inter;}
    public void setIntervalUnit(String unit){this.mIntervalUnit = unit;}
    public void setEndDate(String endDate){this.mEndDate = endDate;}

    public static JSONArray up_sync(Context context){
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while(cursor.moveToNext()){
            jsonArray.put(new Medication(cursor).getJSONObject());
        }
        cursor.close();
        return jsonArray;
    }

    public String toString(Context context) {
        String init;
        if (mDose == null || mDose.equals("")) {
            init = "";
        }
        if (mIntervalUnit.equals(context.getString(R.string.asneeded_interval))) {
            init = String.format("%s, %s", getDoseWithUnit(context), getIntervalUnit(context));
        } else {
            init = String.format("%s, %s %s %s", getDoseWithUnit(context),
                    getTimes(), context.getString(R.string.edit_med_times), getIntervalUnit(context));
        }
        if (!mComment.isEmpty() && !mComment.equals("null")) {
            init = String.format("%s, %s", init, mComment);
        }
        if (!mEndDate.isEmpty() && !mEndDate.equals("null")) {
            init = String.format("%s, %s: %s", init, context.getString(R.string.endDate), mEndDate);
        }
        return init;
    }

    public JSONObject getJSONObject() {
        JSONObject values = super.getJSONObject();
        try {
            values.put(DOSE, mDose);
            values.put(DOSE_UNIT, mDoseUnit);
            values.put(INTERVAL, mInterval);
            values.put(INTERVAL_UNIT, mIntervalUnit);
            values.put(TIMES, mTimes);
            if (!mEndDate.isEmpty() && !mEndDate.equals("null")) {
                values.put(END_DATE, mEndDate);
            }
            values.put(COMMENT, mComment);
            values.put(ENCOUNTER_UUID, mEncounterUUID);
            values.put(CATEGORY_UUID, mCategory);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return values;
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(DOSE, mDose);
        contentValues.put(DOSE_UNIT, mDoseUnit);
        contentValues.put(INTERVAL, mInterval);
        contentValues.put(INTERVAL_UNIT, mIntervalUnit);
        contentValues.put(TIMES, mTimes);
        contentValues.put(END_DATE, mEndDate);
        contentValues.put(CATEGORY_UUID, mCategory);
        contentValues.put(ENCOUNTER_UUID, mEncounterUUID);
        contentValues.put(COMMENT, mComment);
        return contentValues;
    }

    public void setTimes(String times) {
        this.mTimes = times;
    }

    public void setMedicationCategory(String medicationCategory) {
        this.mCategory = medicationCategory;
    }

    public String getTimes() {
        return this.mTimes;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.mEncounterUUID = encounterUuid;
    }

    public static Model.Builder<Medication> BUILDER = new Model.Builder<Medication>() {

        @Override
        public Medication build(Cursor cursor) {
            return new Medication(cursor);
        }
    };
}
