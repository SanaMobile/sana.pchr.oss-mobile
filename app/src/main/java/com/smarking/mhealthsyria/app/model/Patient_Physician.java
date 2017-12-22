package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.database.DatabaseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.NoSuchElementException;

/**
 * Created by abe707 on 1/12/16.
 */
public class Patient_Physician extends Model {
    public static final String TABLE = "Patient_Physician";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static final String PATIENT = "patient_uuid";
    public static final String PHYSICIAN = "physician_uuid";

    private String patient;
    private String physician;

    public Patient_Physician(String uuid, String created, String updated, String synchronized_, String patient, String physician) {
        super(uuid, created, updated, synchronized_);
        this.patient = patient;
        this.physician = physician;
    }

    public Patient_Physician(Parcel in) {
        super(in);
        this.patient = in.readString();
        this.physician = in.readString();
    }

    public Patient_Physician(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.patient = jsonObject.getString(PATIENT);
        this.physician = jsonObject.getString(PHYSICIAN);
    }

    public Patient_Physician(Cursor c) {
        super(c);
        this.patient = getString(c, PATIENT);
        this.physician = getString(c, PHYSICIAN);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(patient);
        parcel.writeString(physician);
    }

    public static final Parcelable.Creator<Patient_Physician> CREATOR = new Parcelable.Creator<Patient_Physician>() {
        public Patient_Physician createFromParcel(Parcel in) {
            return new Patient_Physician(in);
        }

        public Patient_Physician[] newArray(int size) {
            return new Patient_Physician[size];
        }
    };


    public ContentValues putContentValues() {
        ContentValues contentValues = super.putContentValues();
        contentValues.put(PATIENT, patient);
        contentValues.put(PHYSICIAN, physician);
        return contentValues;
    }

    public JSONObject jsonObject() {
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(PATIENT, patient);
            jsonObject.put(PHYSICIAN, physician);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONArray up_sync(Context context) {
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while (cursor.moveToNext()) {
            jsonArray.put(new Patient_Physician(cursor).jsonObject());
        }
        cursor.close();
        return jsonArray;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValues = new ContentValues[length];
        for (int i = 0; i < length; i++) {
            Patient_Physician link = new Patient_Physician(jsonArray.getJSONObject(i));
            contentValues[i] = link.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValues);
    }

    public static Patient_Physician getPatientPhysician(Context c, String uuid) throws NoSuchElementException {
        Uri uri = Uri.withAppendedPath(Patient.URI, uuid.toLowerCase());
        Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);

        Patient_Physician patient_physician = null;
        if (cursor.moveToFirst()) {
            patient_physician = new Patient_Physician(cursor);
        }
        cursor.close();
        DatabaseHandler.get(c).closeDatabase();

        return patient_physician;
    }
}
