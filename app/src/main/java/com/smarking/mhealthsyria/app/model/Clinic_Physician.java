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
public class Clinic_Physician extends Model {
    public static final String TABLE = "Clinic_Physician";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static final String CLINIC = "clinic_uuid";
    public static final String PHYSICIAN = "physician_uuid";

    private String clinic;
    private String physician;

    public Clinic_Physician(String uuid, String created, String updated, String synchronized_, String clinic, String physician) {
        super(uuid, created, updated, synchronized_);
        this.clinic = clinic;
        this.physician = physician;
    }

    public Clinic_Physician(Parcel in) {
        super(in);
        this.clinic = in.readString();
        this.physician = in.readString();
    }

    public Clinic_Physician(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.clinic = jsonObject.getString(CLINIC);
        this.physician = jsonObject.getString(PHYSICIAN);
    }

    public Clinic_Physician(Cursor c) {
        super(c);
        this.clinic = getString(c, CLINIC);
        this.physician = getString(c, PHYSICIAN);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(clinic);
        parcel.writeString(physician);
    }

    public static final Parcelable.Creator<Clinic_Physician> CREATOR = new Parcelable.Creator<Clinic_Physician>() {
        public Clinic_Physician createFromParcel(Parcel in) {
            return new Clinic_Physician(in);
        }

        public Clinic_Physician[] newArray(int size) {
            return new Clinic_Physician[size];
        }
    };


    public ContentValues putContentValues() {
        ContentValues contentValues = super.putContentValues();
        contentValues.put(CLINIC, clinic);
        contentValues.put(PHYSICIAN, physician);
        return contentValues;
    }

    public JSONObject jsonObject() {
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(CLINIC, clinic);
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
            jsonArray.put(new Clinic_Physician(cursor).jsonObject());
        }
        cursor.close();
        return jsonArray;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValues = new ContentValues[length];
        for (int i = 0; i < length; i++) {
            Clinic_Physician link = new Clinic_Physician(jsonArray.getJSONObject(i));
            contentValues[i] = link.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValues);
    }

    public static Clinic_Physician getClinicPhysician(Context c, String uuid) throws NoSuchElementException {
        Uri uri = Uri.withAppendedPath(Clinic.URI, uuid.toLowerCase());
        Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);

        Clinic_Physician clinic_physician = null;
        if (cursor.moveToFirst()) {
            clinic_physician = new Clinic_Physician(cursor);
        }
        cursor.close();
        DatabaseHandler.get(c).closeDatabase();

        return clinic_physician;
    }
}
